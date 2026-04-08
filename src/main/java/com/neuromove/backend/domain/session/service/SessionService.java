package com.neuromove.backend.domain.session.service;

import com.neuromove.backend.domain.calibration.entity.CalibrationProfile;
import com.neuromove.backend.domain.calibration.repository.CalibrationProfileRepository;
import com.neuromove.backend.domain.command.entity.Command;
import com.neuromove.backend.domain.command.repository.CommandRepository;
import com.neuromove.backend.domain.device.entity.EmgDevice;
import com.neuromove.backend.domain.device.entity.MotorDevice;
import com.neuromove.backend.domain.device.repository.EmgDeviceRepository;
import com.neuromove.backend.domain.device.repository.MotorDeviceRepository;
import com.neuromove.backend.domain.fsm.entity.FsmState;
import com.neuromove.backend.domain.fsm.repository.FsmStateRepository;
import com.neuromove.backend.domain.session.dto.request.SessionStartRequest;
import com.neuromove.backend.domain.session.dto.request.SessionEndRequest;
import com.neuromove.backend.domain.session.dto.response.SessionStartResponse;
import com.neuromove.backend.domain.session.dto.response.SessionEndResponse;
import com.neuromove.backend.domain.session.dto.response.SessionStatusResponse;
import com.neuromove.backend.domain.session.entity.Session;
import com.neuromove.backend.domain.session.repository.SessionRepository;
import com.neuromove.backend.domain.user.entity.User;
import com.neuromove.backend.global.exception.CustomException;
import com.neuromove.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SessionService {

    private final SessionRepository sessionRepository;
    private final CalibrationProfileRepository calibrationProfileRepository;
    private final EmgDeviceRepository emgDeviceRepository;
    private final MotorDeviceRepository motorDeviceRepository;
    private final FsmStateRepository fsmStateRepository;
    private final CommandRepository commandRepository;

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

        session.end();
        return SessionEndResponse.from(session);
    }
}
