package com.neuromove.backend.domain.intent.service;

import com.neuromove.backend.domain.command.entity.Command;
import com.neuromove.backend.domain.command.entity.enums.CommandType;
import com.neuromove.backend.domain.command.repository.CommandRepository;
import com.neuromove.backend.domain.fsm.entity.enums.FsmStateType;
import com.neuromove.backend.domain.fsm.service.FsmService;
import com.neuromove.backend.domain.intent.dto.request.IntentReceiveRequest;
import com.neuromove.backend.domain.intent.dto.response.IntentReceiveResponse;
import com.neuromove.backend.domain.intent.entity.IntentLog;
import com.neuromove.backend.domain.intent.repository.IntentLogRepository;
import com.neuromove.backend.domain.session.entity.Session;
import com.neuromove.backend.domain.session.repository.SessionRepository;
import com.neuromove.backend.global.exception.CustomException;
import com.neuromove.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IntentService {

    private static final float MAX_DRIVING_MINUTES = 30.0f;
    private static final float EMERGENCY_STOP_THRESHOLD = 0.7f;
    private static final float FATIGUE_THRESHOLD = 0.5f;

    private final SessionRepository sessionRepository;
    private final IntentLogRepository intentLogRepository;
    private final CommandRepository commandRepository;
    private final FsmService fsmService;

    @Transactional
    public IntentReceiveResponse receiveIntent(IntentReceiveRequest request) {
        Session session = sessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new CustomException(ErrorCode.SESSION_NOT_FOUND));

        // 1. durationRatio 계산
        long elapsedMinutes = Duration.between(session.getStartedAt(), LocalDateTime.now()).toMinutes();
        float durationRatio = Math.min(elapsedMinutes / MAX_DRIVING_MINUTES, 1.0f);

        // 2. Risk Score 계산 (α·피로도 + β·신호안정성 + γ·지속시간)
        float fatigueComponent = 0.4f * request.getFatigueScore();
        float stabilityComponent = 0.4f * (1 - request.getSignalQuality());
        float durationComponent = 0.2f * durationRatio;
        float riskScore = fatigueComponent + stabilityComponent + durationComponent;

        // 3. FSM 상태 전이
        if (riskScore >= EMERGENCY_STOP_THRESHOLD) {
            fsmService.transition(session, FsmStateType.EMERGENCY_STOP, "RISK_SCORE_EXCEEDED");
        } else if (riskScore >= FATIGUE_THRESHOLD) {
            fsmService.transition(session, FsmStateType.FATIGUE_COMPENSATING, "FATIGUE");
        } else {
            fsmService.transition(session, FsmStateType.DRIVING, "NORMAL");
        }

        // 4. speedLevel 및 최종 command 결정
        boolean accepted = riskScore < EMERGENCY_STOP_THRESHOLD;
        int speedLevel = calculateSpeedLevel(riskScore);
        CommandType finalCommand = accepted
                ? CommandType.valueOf(request.getIntent().name())
                : CommandType.BLOCKED;

        // 5. max_risk_score 업데이트
        session.updateMaxRiskScore(riskScore);

        // 6. IntentLog 저장
        IntentLog intentLog = IntentLog.builder()
                .session(session)
                .intent(request.getIntent())
                .confidence(request.getConfidence())
                .fatigueScore(request.getFatigueScore())
                .signalQuality(request.getSignalQuality())
                .riskScore(riskScore)
                .fatigueComponent(fatigueComponent)
                .stabilityComponent(stabilityComponent)
                .durationComponent(durationComponent)
                .accepted(accepted)
                .emgTimestamp(request.getTimestamp())
                .build();

        IntentLog savedIntent = intentLogRepository.save(intentLog);

        // 7. Command 저장
        Command command = Command.builder()
                .session(session)
                .intentLog(savedIntent)
                .command(finalCommand)
                .speedLevel(speedLevel)
                .riskScore(riskScore)
                .build();

        Command savedCommand = commandRepository.save(command);

        return IntentReceiveResponse.of(savedIntent, savedCommand);
    }

    private int calculateSpeedLevel(float riskScore) {
        if (riskScore < 0.2f) return 5;
        if (riskScore < 0.3f) return 4;
        if (riskScore < 0.4f) return 3;
        if (riskScore < 0.5f) return 2;
        if (riskScore < 0.7f) return 1;
        return 0;
    }
}
