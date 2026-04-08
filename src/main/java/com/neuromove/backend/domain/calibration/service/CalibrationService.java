package com.neuromove.backend.domain.calibration.service;

import com.neuromove.backend.domain.calibration.dto.request.CalibrationEndRequest;
import com.neuromove.backend.domain.calibration.dto.request.CalibrationStartRequest;
import com.neuromove.backend.domain.calibration.dto.request.CalibrationStepUpdateRequest;
import com.neuromove.backend.domain.calibration.dto.response.*;
import com.neuromove.backend.domain.calibration.entity.CalibrationProfile;
import com.neuromove.backend.domain.calibration.entity.CalibrationSession;
import com.neuromove.backend.domain.calibration.entity.enums.CalibrationStatus;
import com.neuromove.backend.domain.calibration.entity.enums.CalibrationStep;
import com.neuromove.backend.domain.calibration.repository.CalibrationProfileRepository;
import com.neuromove.backend.domain.calibration.repository.CalibrationSessionRepository;
import com.neuromove.backend.domain.device.entity.EmgDevice;
import com.neuromove.backend.domain.device.repository.EmgDeviceRepository;
import com.neuromove.backend.domain.user.entity.User;
import com.neuromove.backend.global.exception.CustomException;
import com.neuromove.backend.global.exception.ErrorCode;
import com.neuromove.backend.infrastructure.ai.calibration.AiCalibrationClient;
import com.neuromove.backend.infrastructure.ai.calibration.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CalibrationService {

    private final CalibrationSessionRepository calibrationSessionRepository;
    private final CalibrationProfileRepository calibrationProfileRepository;
    private final EmgDeviceRepository emgDeviceRepository;
    private final AiCalibrationClient aiCalibrationClient;

    @Transactional
    public CalibrationStartResponse start(User user, CalibrationStartRequest request) {
        EmgDevice emgDevice = emgDeviceRepository.findById(request.getEmgDeviceId())
                .orElseThrow(() -> new CustomException(ErrorCode.EMG_DEVICE_NOT_FOUND));

        CalibrationSession session = CalibrationSession.builder()
                .user(user)
                .emgDevice(emgDevice)
                .build();

        CalibrationSession saved = calibrationSessionRepository.save(session);

        // TODO: AI 서버 연동 후 주석 해제
//        aiCalibrationClient.start(AiCalibrationStartRequest.builder()
//                .calibrationSessionId(saved.getCalibrationSessionId())
//                .userId(user.getUserId())
//                .deviceId(emgDevice.getEmgDeviceId())
//                .initialStep(CalibrationStep.REST.name())
//                .build());

        return CalibrationStartResponse.from(saved);
    }

    @Transactional
    public CalibrationStepUpdateResponse updateStep(User user, CalibrationStepUpdateRequest request) {
        CalibrationSession session = calibrationSessionRepository.findById(request.getCalibrationSessionId())
                .orElseThrow(() -> new CustomException(ErrorCode.CALIBRATION_SESSION_NOT_FOUND));

        validateOwner(user, session);

        if (session.getStatus() == CalibrationStatus.COMPLETED) {
            throw new CustomException(ErrorCode.CALIBRATION_ALREADY_COMPLETED);
        }

        session.proceedToStep(request.getStep());

        // TODO: AI 서버 연동 후 주석 해제
//        aiCalibrationClient.updateStep(AiCalibrationStepRequest.builder()
//                .calibrationSessionId(session.getCalibrationSessionId())
//                .step(request.getStep().name())
//                .build());

        return CalibrationStepUpdateResponse.from(session);
    }

@Transactional
    public CalibrationEndResponse end(User user, CalibrationEndRequest request) {
        CalibrationSession session = calibrationSessionRepository.findById(request.getCalibrationSessionId())
                .orElseThrow(() -> new CustomException(ErrorCode.CALIBRATION_SESSION_NOT_FOUND));

        validateOwner(user, session);

        if (session.getStatus() == CalibrationStatus.COMPLETED) {
            throw new CustomException(ErrorCode.CALIBRATION_ALREADY_COMPLETED);
        }

        // TODO: AI 서버 연동 후 아래 더미 데이터 제거하고 주석 해제
//        AiCalibrationFinishResponse aiResponse = aiCalibrationClient.finish(
//                AiCalibrationFinishRequest.builder()
//                        .calibrationSessionId(session.getCalibrationSessionId())
//                        .build()
//        );
//        AiCalibrationFinishResponse.Result result = aiResponse.getData().getResult();
//        AiCalibrationFinishResponse.Baseline baseline = result.getBaseline();
//        Map<String, Float> intentThresholds = result.getIntentThresholds();
//        session.complete(result.getSignalQuality());

        session.complete(null);

        calibrationProfileRepository.findFirstByUserAndIsActiveTrueOrderByCreatedAtDesc(user)
                .ifPresent(CalibrationProfile::deactivate);

        CalibrationProfile profile = CalibrationProfile.builder()
                .user(user)
                .calibrationSessionId(session.getCalibrationSessionId())
                .isActive(true)
                .build();

        CalibrationProfile saved = calibrationProfileRepository.save(profile);
        return CalibrationEndResponse.from(saved);
    }

    private void validateOwner(User user, CalibrationSession session) {
        if (!session.getUser().getUserId().equals(user.getUserId())) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
    }

    public CalibrationProfileResponse getProfile(User user) {
        CalibrationProfile profile = calibrationProfileRepository.findFirstByUserAndIsActiveTrueOrderByCreatedAtDesc(user)
                .orElseThrow(() -> new CustomException(ErrorCode.CALIBRATION_PROFILE_NOT_FOUND));

        return CalibrationProfileResponse.from(profile);
    }
}
