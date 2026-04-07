package com.neuromove.backend.domain.session.service;

import com.neuromove.backend.domain.calibration.entity.CalibrationProfile;
import com.neuromove.backend.domain.calibration.repository.CalibrationProfileRepository;
import com.neuromove.backend.domain.device.entity.EmgDevice;
import com.neuromove.backend.domain.device.entity.MotorDevice;
import com.neuromove.backend.domain.device.repository.EmgDeviceRepository;
import com.neuromove.backend.domain.device.repository.MotorDeviceRepository;
import com.neuromove.backend.domain.session.dto.request.SessionStartRequest;
import com.neuromove.backend.domain.session.dto.response.SessionStartResponse;
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
}
