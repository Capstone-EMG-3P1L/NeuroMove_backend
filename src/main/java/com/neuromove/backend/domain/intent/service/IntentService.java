package com.neuromove.backend.domain.intent.service;

import com.neuromove.backend.domain.command.entity.Command;
import com.neuromove.backend.domain.command.entity.enums.CommandType;
import com.neuromove.backend.domain.command.repository.CommandRepository;
import com.neuromove.backend.domain.command.service.FailSafeStateManager;
import com.neuromove.backend.domain.command.service.MotorWebSocketService;
import com.neuromove.backend.domain.command.service.TurnControlManager;
import com.neuromove.backend.domain.fsm.entity.enums.FsmStateType;
import com.neuromove.backend.domain.fsm.service.FsmService;
import com.neuromove.backend.domain.intent.dto.request.IntentReceiveRequest;
import com.neuromove.backend.domain.intent.dto.response.IntentReceiveResponse;
import com.neuromove.backend.domain.intent.entity.IntentLog;
import com.neuromove.backend.domain.intent.repository.IntentLogRepository;
import com.neuromove.backend.domain.session.dto.websocket.SessionUpdateMessage;
import com.neuromove.backend.domain.session.entity.Session;
import com.neuromove.backend.domain.session.repository.SessionRepository;
import com.neuromove.backend.domain.session.service.SessionWebSocketService;
import com.neuromove.backend.global.exception.CustomException;
import com.neuromove.backend.global.exception.ErrorCode;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IntentService {

    private static final float MAX_DRIVING_MINUTES = 30.0f;
    private static final float EMERGENCY_STOP_THRESHOLD = 0.7f;
    private static final float FATIGUE_THRESHOLD = 0.5f;
    private static final float CONFIDENCE_THRESHOLD = 0.5f;
    private static final long STALE_COMMAND_MS = 30000L;

    // 연속 stale 3회 시 STOP
    private static final int MAX_CONSECUTIVE_STALE = 3;

    // 연속 이상 패턴 3회 시 EMERGENCY_STOP
    private static final int MAX_CONSECUTIVE_ABNORMAL = 3;

    // 이상 패턴 판별 기준값
    // signalQuality만 사용 (전극 탈락/노이즈 등 물리적 이상)
    // fatigueScore는 calibration 없이 raw 값이라 riskScore에서만 반영, confidence는 REST에서 자연적으로 낮으므로 제외
    private static final float ABNORMAL_SIGNAL_QUALITY_THRESHOLD = 0.2f;

    private final SessionRepository sessionRepository;
    private final IntentLogRepository intentLogRepository;
    private final CommandRepository commandRepository;
    private final FsmService fsmService;
    private final MotorWebSocketService motorWebSocketService;
    private final FailSafeStateManager failSafeStateManager;
    private final TurnControlManager turnControlManager;    // 회전 확정 + FORWARD 예약
    private final SessionWebSocketService sessionWebSocketService;

    @Transactional
    public IntentReceiveResponse receiveIntent(IntentReceiveRequest request) {
        Session session = sessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new CustomException(ErrorCode.SESSION_NOT_FOUND));

        // [Fail-safe 1] 명령 유효시간 처리 (3초 초과 시 BLOCKED)
        long now = Instant.now().toEpochMilli();
        boolean stale = (now - request.getTimestamp()) > STALE_COMMAND_MS;

        // [Fail-safe 3] confidence < 0.7 → BLOCKED
        boolean lowConfidence = request.getConfidence() < CONFIDENCE_THRESHOLD;

        // rollback 시 복구 여부 추적
        boolean staleIncremented = false;
        boolean abnormalIncremented = false;

        // stale 연속 횟수 관리
        int staleCount;
        if (stale) {
            staleCount = failSafeStateManager.increaseStaleCount(session.getSessionId());
            staleIncremented = true; // 실제 증가 여부 기록
        } else {
            failSafeStateManager.resetStaleCount(session.getSessionId());
            staleCount = 0;
        }

        // 이상 패턴 감지
        // - confidence: REST 구간에서 신호 에너지 낮아 자연적으로 낮게 나오므로 제외 (lowConfidence/BLOCKED 로직에서 별도 처리)
        // - fatigueScore: calibration 없으면 raw 신호 평균이라 참고용에 불과 → riskScore에서 이미 반영됨
        // - signalQuality만 이상 패턴 기준으로 사용 (전극 탈락/노이즈 등 물리적 이상 감지)
        boolean abnormalPattern = request.getSignalQuality() < ABNORMAL_SIGNAL_QUALITY_THRESHOLD;

        // 이상 패턴 카운트 관리
        int abnormalCount;
        if (abnormalPattern) {
            abnormalCount = failSafeStateManager.increaseAbnormalCount(session.getSessionId());
            abnormalIncremented = true; // 실제 증가 여부 기록
        } else {
            failSafeStateManager.resetAbnormalCount(session.getSessionId());
            abnormalCount = 0;
        }

        // 트랜잭션 롤백 시 메모리 카운터 복구
        boolean finalStaleIncremented = staleIncremented;
        boolean finalAbnormalIncremented = abnormalIncremented;
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_ROLLED_BACK) {
                    if (finalStaleIncremented) {
                        failSafeStateManager.decreaseStaleCount(session.getSessionId());
                    }
                    if (finalAbnormalIncremented) {
                        failSafeStateManager.decreaseAbnormalCount(session.getSessionId());
                    }
                }
            }
        });

        // 1. durationRatio 계산
        long elapsedMinutes = Duration.between(session.getStartedAt(), LocalDateTime.now()).toMinutes();
        float durationRatio = Math.min(elapsedMinutes / MAX_DRIVING_MINUTES, 1.0f);

        // 2. Risk Score 계산 (α·피로도 + β·신호안정성 + γ·지속시간)
        float fatigueComponent = 0.4f * request.getFatigueScore();
        float stabilityComponent = 0.4f * (1 - request.getSignalQuality());
        float durationComponent = 0.2f * durationRatio;
        float riskScore = fatigueComponent + stabilityComponent + durationComponent;

        // 4. speedLevel 및 최종 command 결정
        boolean accepted = riskScore < EMERGENCY_STOP_THRESHOLD && !stale && !lowConfidence;
        int speedLevel = calculateSpeedLevel(riskScore);

        // fail-safe 우선 적용
        CommandType finalCommand;
        if (abnormalCount >= MAX_CONSECUTIVE_ABNORMAL) {
            finalCommand = CommandType.EMERGENCY_STOP; // 이상 패턴 3회 연속 시 비상정지
            failSafeStateManager.resetAbnormalCount(session.getSessionId()); // 발동 후 카운터 리셋 (연속 재발동 방지)
        } else if (staleCount >= MAX_CONSECUTIVE_STALE) {
            finalCommand = CommandType.STOP; // stale 3회 누적 시 자동 STOP
        } else {
            finalCommand = accepted
                    ? CommandType.valueOf(request.getIntent().name())
                    : CommandType.BLOCKED;
        }

        // 3. FSM 상태 전이
        if (finalCommand == CommandType.EMERGENCY_STOP) {
            fsmService.transition(session, FsmStateType.EMERGENCY_STOP, "FAIL_SAFE_ABNORMAL_PATTERN_3X"); // 이상 패턴 3회 연속 비상정지
        } else if (riskScore >= EMERGENCY_STOP_THRESHOLD) {
            fsmService.transition(session, FsmStateType.EMERGENCY_STOP, "RISK_SCORE_EXCEEDED");
        } else if (riskScore >= FATIGUE_THRESHOLD) {
            fsmService.transition(session, FsmStateType.FATIGUE_COMPENSATING, "FATIGUE");
        } else {
            fsmService.transition(session, FsmStateType.DRIVING, "NORMAL");
        }

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
                .accepted(finalCommand != CommandType.BLOCKED) // 최종 명령 기준으로 accepted 저장
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

        // 모터 웹소켓 명령 전송
        if (savedCommand.getCommand() != CommandType.BLOCKED) {

            if (session.getMotorDevice() == null) {
                log.warn("세션에 연결된 motorDevice가 없어 명령 전송을 건너뜁니다. sessionId={}", session.getSessionId());
                return IntentReceiveResponse.of(savedIntent, savedCommand);
            }

            String motorDeviceId = session.getMotorDevice().getMotorDeviceId();

            String commandToSend = savedCommand.getCommand().name();

            final String sessionIdForCallback = session.getSessionId();
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    // LEFT/RIGHT: 연속 3회 확정 + 500ms 후 FORWARD 자동 전송
                    // FORWARD/STOP 등: 즉시 전송
                    turnControlManager.process(sessionIdForCallback, motorDeviceId, commandToSend);
                }
            });
        }

        // WebSocket으로 프론트에 실시간 업데이트 전송
        sessionWebSocketService.sendSessionUpdate(session.getSessionId(),
                SessionUpdateMessage.builder()
                        .type("INTENT")
                        .sessionId(session.getSessionId())
                        .intent(request.getIntent().name())
                        .riskScore((double) riskScore)
                        .command(finalCommand.name())
                        .speedLevel(speedLevel)
                        .timestamp(String.valueOf(System.currentTimeMillis()))
                        .build()
        );

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