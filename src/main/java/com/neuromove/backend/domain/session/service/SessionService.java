package com.neuromove.backend.domain.session.service;

import com.neuromove.backend.domain.calibration.entity.CalibrationProfile;
import com.neuromove.backend.domain.calibration.repository.CalibrationProfileRepository;
import com.neuromove.backend.domain.device.entity.EmgDevice;
import com.neuromove.backend.domain.device.entity.MotorDevice;
import com.neuromove.backend.domain.device.repository.EmgDeviceRepository;
import com.neuromove.backend.domain.device.repository.MotorDeviceRepository;
import com.neuromove.backend.domain.fsm.entity.FsmState;
import com.neuromove.backend.domain.fsm.repository.FsmStateRepository;
import com.neuromove.backend.domain.intent.entity.IntentLog;
import com.neuromove.backend.domain.intent.repository.IntentLogRepository;
import com.neuromove.backend.domain.session.dto.request.SessionStartRequest;
import com.neuromove.backend.domain.session.dto.response.FsmStateLogResponse;
import com.neuromove.backend.domain.session.dto.response.IntentLogResponse;
import com.neuromove.backend.domain.session.dto.response.SessionDetailResponse;
import com.neuromove.backend.domain.session.dto.response.SessionStartResponse;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

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

    @Transactional
    public SessionStartResponse start(User user, SessionStartRequest request) {
        CalibrationProfile profile = calibrationProfileRepository.findById(request.getProfileId())
                .orElseThrow(() -> new CustomException(ErrorCode.CALIBRATION_PROFILE_NOT_FOUND));
        if (!profile.getUser().getUserId().equals(user.getUserId())) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

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
}