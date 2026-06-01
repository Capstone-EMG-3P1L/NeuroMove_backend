package com.neuromove.backend.domain.session.service;

import com.neuromove.backend.domain.calibration.entity.CalibrationProfile;
import com.neuromove.backend.domain.calibration.repository.CalibrationProfileRepository;
import com.neuromove.backend.domain.command.entity.Command;
import com.neuromove.backend.domain.command.repository.CommandRepository;
import com.neuromove.backend.domain.command.service.FailSafeStateManager;
import com.neuromove.backend.domain.command.service.MotorWebSocketService;
import com.neuromove.backend.domain.command.service.TurnControlManager;
import com.neuromove.backend.domain.device.entity.EmgDevice;
import com.neuromove.backend.domain.device.entity.MotorDevice;
import com.neuromove.backend.domain.device.repository.EmgDeviceRepository;
import com.neuromove.backend.domain.device.repository.MotorDeviceRepository;
import com.neuromove.backend.domain.fsm.entity.FsmState;
import com.neuromove.backend.domain.fsm.repository.FsmStateRepository;
import com.neuromove.backend.domain.intent.entity.IntentLog;
import com.neuromove.backend.domain.intent.repository.IntentLogRepository;
import com.neuromove.backend.domain.session.dto.request.SessionEndRequest;
import com.neuromove.backend.domain.session.dto.request.SessionStartRequest;
import com.neuromove.backend.domain.session.dto.response.FsmStateLogResponse;
import com.neuromove.backend.domain.session.dto.response.IntentLogResponse;
import com.neuromove.backend.domain.session.dto.response.SessionDetailResponse;
import com.neuromove.backend.domain.session.dto.response.SessionEndResponse;
import com.neuromove.backend.domain.session.dto.response.SessionStartResponse;
import com.neuromove.backend.domain.session.dto.response.SessionStatusResponse;
import com.neuromove.backend.domain.session.dto.response.SessionSummaryResponse;
import com.neuromove.backend.domain.session.entity.Session;
import com.neuromove.backend.domain.session.repository.SessionRepository;
import com.neuromove.backend.domain.user.entity.User;
import com.neuromove.backend.domain.user.repository.UserRepository;
import com.neuromove.backend.global.exception.CustomException;
import com.neuromove.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.neuromove.backend.infrastructure.ai.session.AiSessionClient;
import com.neuromove.backend.infrastructure.ai.session.dto.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SessionService {

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final CalibrationProfileRepository calibrationProfileRepository;
    private final EmgDeviceRepository emgDeviceRepository;
    private final MotorDeviceRepository motorDeviceRepository;
    private final FsmStateRepository fsmStateRepository;
    private final IntentLogRepository intentLogRepository;
    private final CommandRepository commandRepository;
    private final FailSafeStateManager failSafeStateManager;
    private final TurnControlManager   turnControlManager;
    private final MotorWebSocketService motorWebSocketService;
    private final AiSessionClient aiSessionClient;

    @Transactional
    public SessionStartResponse start(User user, SessionStartRequest request) {
        // 가장 최근 활성 CalibrationProfile 자동 조회
        CalibrationProfile profile = calibrationProfileRepository
                .findFirstByUserAndIsActiveTrueOrderByCreatedAtDesc(user)
                .orElseThrow(() -> new CustomException(ErrorCode.CALIBRATION_PROFILE_NOT_FOUND));

        EmgDevice emgDevice = emgDeviceRepository.findById(request.getEmgDeviceId())
                .orElseThrow(() -> new CustomException(ErrorCode.EMG_DEVICE_NOT_FOUND));
        if (!emgDevice.getUser().getUserId().equals(user.getUserId())) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        MotorDevice motorDevice = motorDeviceRepository.findById(request.getMotorDeviceId())
                .orElseThrow(() -> new CustomException(ErrorCode.MOTOR_DEVICE_NOT_FOUND));
        if (!motorDevice.getUser().getUserId().equals(user.getUserId())) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        Session session = Session.builder()
                .user(user)
                .calibrationProfile(profile)
                .emgDevice(emgDevice)
                .motorDevice(motorDevice)
                .build();

        Session saved = sessionRepository.save(session);

        // AI 서버에 세션 시작 요청 (최근 calibration 정보 포함)
        aiSessionClient.start(
                AiSessionStartRequest.builder()
                        .sessionId(saved.getSessionId())
                        .userId(user.getUserId())
                        .deviceId(emgDevice.getEmgDeviceId())
                        .profileId(profile.getProfileId())
                        .calibration(toAiCalibrationSnapshot(profile))
                        .build()
        );

        return SessionStartResponse.from(saved);
    }

    public SessionDetailResponse getSessionDetail(String username, String sessionId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Session session = sessionRepository.findBySessionIdAndUser(sessionId, user)
                .orElseThrow(() -> new CustomException(ErrorCode.SESSION_NOT_FOUND));

        List<FsmStateLogResponse> fsmStates = fsmStateRepository
                .findAllBySessionOrderByTransitionedAtAsc(session)
                .stream()
                .map(this::toFsmStateLogResponse)
                .toList();

        List<IntentLogResponse> intentLogs = intentLogRepository
                .findAllBySessionOrderByReceivedAtAsc(session)
                .stream()
                .map(this::toIntentLogResponse)
                .toList();

        return new SessionDetailResponse(
                toSessionSummaryResponse(session),
                fsmStates,
                intentLogs
        );
    }

    private SessionSummaryResponse toSessionSummaryResponse(Session session) {
        return new SessionSummaryResponse(
                session.getSessionId(),
                session.getEmgDevice() != null ? session.getEmgDevice().getEmgDeviceId() : null,
                session.getMotorDevice() != null ? session.getMotorDevice().getMotorDeviceId() : null,
                session.getStatus().name(),
                format(session.getStartedAt()),
                format(session.getEndedAt()),
                session.getDurationSeconds(),
                session.getMaxRiskScore() == null ? null : session.getMaxRiskScore().doubleValue()
        );
    }

    private FsmStateLogResponse toFsmStateLogResponse(FsmState state) {
        return new FsmStateLogResponse(
                state.getFromState() != null ? state.getFromState().name() : null,
                state.getToState().name(),
                state.getReason(),
                format(state.getTransitionedAt())
        );
    }

    private IntentLogResponse toIntentLogResponse(IntentLog log) {
        return new IntentLogResponse(
                log.getIntent().name(),
                log.getRiskScore() == null ? null : log.getRiskScore().doubleValue(),
                format(log.getReceivedAt())
        );
    }

    private String format(LocalDateTime time) {
        if (time == null) {
            return null;
        }
        return time.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    public SessionStatusResponse getSessionStatus(String sessionId, String userId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new CustomException(ErrorCode.SESSION_NOT_FOUND));

        if (!session.getUser().getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        FsmState latestFsmState = fsmStateRepository.findTopBySessionOrderByTransitionedAtDesc(session)
                .orElse(null);
        Command latestCommand = commandRepository.findTopBySessionOrderByIssuedAtDesc(session)
                .orElse(null);

        return SessionStatusResponse.of(session, latestFsmState, latestCommand);
    }

    /**
     * CalibrationProfile -> AI 서버용 CalibrationSnapshot 변환
     * intentThresholds: REST, LEFT, RIGHT, STOP 4개를 Map으로 전달
     */
    private AiSessionStartRequest.CalibrationSnapshot toAiCalibrationSnapshot(CalibrationProfile profile) {
        Map<String, Double> intentThresholds = new LinkedHashMap<>();
        intentThresholds.put("REST", profile.getIntentThresholdRest() != null ? profile.getIntentThresholdRest().doubleValue() : null);
        intentThresholds.put("LEFT", profile.getIntentThresholdLeft() != null ? profile.getIntentThresholdLeft().doubleValue() : null);
        intentThresholds.put("RIGHT", profile.getIntentThresholdRight() != null ? profile.getIntentThresholdRight().doubleValue() : null);
        intentThresholds.put("STOP", profile.getIntentThresholdStop() != null ? profile.getIntentThresholdStop().doubleValue() : null);

        return AiSessionStartRequest.CalibrationSnapshot.builder()
                .baseline(AiSessionStartRequest.Baseline.builder()
                        .ch1Mean(profile.getCh1Mean() != null ? profile.getCh1Mean().doubleValue() : null)
                        .ch1Std(profile.getCh1Std() != null ? profile.getCh1Std().doubleValue() : null)
                        .ch2Mean(profile.getCh2Mean() != null ? profile.getCh2Mean().doubleValue() : null)
                        .ch2Std(profile.getCh2Std() != null ? profile.getCh2Std().doubleValue() : null)
                        .ch3Mean(profile.getCh3Mean() != null ? profile.getCh3Mean().doubleValue() : null)
                        .ch3Std(profile.getCh3Std() != null ? profile.getCh3Std().doubleValue() : null)
                        .build())
                .activationThreshold(profile.getActivationThreshold() != null ? profile.getActivationThreshold().doubleValue() : null)
                .intentThresholds(intentThresholds)
                .fatigueBaseline(profile.getFatigueBaseline() != null ? profile.getFatigueBaseline().doubleValue() : null)
                .signalQuality(profile.getSignalQuality() != null ? profile.getSignalQuality().doubleValue() : null)
                .build();
    }

    @Transactional
    public SessionEndResponse end(String sessionId, String userId, SessionEndRequest request) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new CustomException(ErrorCode.SESSION_NOT_FOUND));

        if (!session.getUser().getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        if (session.getStatus() == com.neuromove.backend.domain.session.entity.enums.SessionStatus.ENDED) {
            return SessionEndResponse.from(session);
        }

        // 세션 상태 ENDED로 업데이트 (endedAt, durationSeconds 포함)
        session.end();

        // AI 서버에 세션 종료 요청
        aiSessionClient.end(
                AiSessionEndRequest.builder()
                        .sessionId(sessionId)
                        .build()
        );

        // 트랜잭션 커밋 이후 모터 FINISH 전송 + 메모리 상태 정리
        String motorDeviceId = session.getMotorDevice() != null
                ? session.getMotorDevice().getMotorDeviceId()
                : null;

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                // 모터에 즉시 정지 명령 전송
                if (motorDeviceId != null) {
                    motorWebSocketService.sendCommand(motorDeviceId, "FINISH");
                }
                failSafeStateManager.clear(sessionId);
                turnControlManager.clear(sessionId);   // 예약된 auto-REST 취소 + 연속 카운트 초기화
            }
        });

        return SessionEndResponse.from(session);
    }
}
