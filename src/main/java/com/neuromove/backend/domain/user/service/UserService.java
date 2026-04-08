package com.neuromove.backend.domain.user.service;

import com.neuromove.backend.domain.calibration.entity.CalibrationProfile;
import com.neuromove.backend.domain.calibration.repository.CalibrationProfileRepository;
import com.neuromove.backend.domain.device.entity.EmgDevice;
import com.neuromove.backend.domain.device.entity.MotorDevice;
import com.neuromove.backend.domain.device.repository.EmgDeviceRepository;
import com.neuromove.backend.domain.device.repository.MotorDeviceRepository;
import com.neuromove.backend.domain.session.entity.Session;
import com.neuromove.backend.domain.session.entity.enums.SessionStatus;
import com.neuromove.backend.domain.session.repository.SessionRepository;
import com.neuromove.backend.domain.user.dto.response.ActiveCalibrationProfileResponse;
import com.neuromove.backend.domain.user.dto.response.ActiveSessionResponse;
import com.neuromove.backend.domain.user.dto.response.RegisteredEmgDeviceResponse;
import com.neuromove.backend.domain.user.dto.response.RegisteredMotorDeviceResponse;
import com.neuromove.backend.domain.user.dto.response.SessionLogItemResponse;
import com.neuromove.backend.domain.user.dto.response.SessionLogsResponse;
import com.neuromove.backend.domain.user.dto.response.UserStatusResponse;
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
public class UserService {

    private final UserRepository userRepository;
    private final EmgDeviceRepository emgDeviceRepository;
    private final MotorDeviceRepository motorDeviceRepository;
    private final CalibrationProfileRepository calibrationProfileRepository;
    private final SessionRepository sessionRepository;

    public UserStatusResponse getMyInfo(String username) {
        User user = getUserByUsername(username);

        EmgDevice emgDevice = emgDeviceRepository.findFirstByUserAndIsActiveTrueOrderByCreatedAtDesc(user)
                .orElse(null);

        MotorDevice motorDevice = motorDeviceRepository.findFirstByUserAndIsActiveTrueOrderByCreatedAtDesc(user)
                .orElse(null);

        CalibrationProfile calibrationProfile = calibrationProfileRepository.findFirstByUserAndIsActiveTrueOrderByCreatedAtDesc(user)
                .orElse(null);

        Session activeSession = sessionRepository.findFirstByUserAndStatus(user, SessionStatus.ACTIVE)
                .orElse(null);

        return new UserStatusResponse(
                user.getUserId(),
                user.getUsername(),
                user.getName(),
                toRegisteredEmgDeviceResponse(emgDevice),
                toRegisteredMotorDeviceResponse(motorDevice),
                toActiveCalibrationProfileResponse(calibrationProfile),
                toActiveSessionResponse(activeSession)
        );
    }

    public SessionLogsResponse getMyLogs(String username) {
        User user = getUserByUsername(username);

        List<SessionLogItemResponse> logs = sessionRepository.findAllByUserOrderByStartedAtDesc(user)
                .stream()
                .map(this::toSessionLogItemResponse)
                .toList();

        return new SessionLogsResponse(logs);
    }

    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    private RegisteredEmgDeviceResponse toRegisteredEmgDeviceResponse(EmgDevice emgDevice) {
        if (emgDevice == null) {
            return null;
        }

        return new RegisteredEmgDeviceResponse(
                emgDevice.getEmgDeviceId(),
                emgDevice.getName(),
                emgDevice.isActive()
        );
    }

    private RegisteredMotorDeviceResponse toRegisteredMotorDeviceResponse(MotorDevice motorDevice) {
        if (motorDevice == null) {
            return null;
        }

        return new RegisteredMotorDeviceResponse(
                motorDevice.getMotorDeviceId(),
                motorDevice.getName(),
                motorDevice.isActive(),
                motorDevice.getConnectionStatus().name()
        );
    }

    private ActiveCalibrationProfileResponse toActiveCalibrationProfileResponse(CalibrationProfile profile) {
        if (profile == null) {
            return null;
        }

        return new ActiveCalibrationProfileResponse(
                profile.getProfileId(),
                profile.getSignalQuality() == null ? null : profile.getSignalQuality().doubleValue(),
                format(profile.getUpdatedAt())
        );
    }

    private ActiveSessionResponse toActiveSessionResponse(Session session) {
        if (session == null) {
            return null;
        }

        return new ActiveSessionResponse(
                session.getSessionId(),
                session.getStatus().name(),
                format(session.getStartedAt())
        );
    }

    private SessionLogItemResponse toSessionLogItemResponse(Session session) {
        return new SessionLogItemResponse(
                session.getSessionId(),
                session.getEmgDevice() != null ? session.getEmgDevice().getEmgDeviceId() : null,
                session.getMotorDevice() != null ? session.getMotorDevice().getMotorDeviceId() : null,
                format(session.getStartedAt()),
                format(session.getEndedAt()),
                session.getDurationSeconds(),
                session.getMaxRiskScore() == null ? null : session.getMaxRiskScore().doubleValue(),
                session.getStatus().name()
        );
    }

    private String format(LocalDateTime time) {
        if (time == null) {
            return null;
        }
        return time.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}