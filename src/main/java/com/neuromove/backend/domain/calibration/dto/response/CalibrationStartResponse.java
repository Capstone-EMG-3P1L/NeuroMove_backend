package com.neuromove.backend.domain.calibration.dto.response;

import com.neuromove.backend.domain.calibration.entity.CalibrationSession;
import com.neuromove.backend.domain.calibration.entity.enums.CalibrationStatus;
import com.neuromove.backend.domain.calibration.entity.enums.CalibrationStep;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CalibrationStartResponse {

    private String calibrationSessionId;
    private String emgDeviceId;
    private CalibrationStatus status;
    private CalibrationStep currentStep;
    private LocalDateTime startedAt;

    public static CalibrationStartResponse from(CalibrationSession session) {
        return CalibrationStartResponse.builder()
                .calibrationSessionId(session.getCalibrationSessionId())
                .emgDeviceId(session.getEmgDevice().getEmgDeviceId())
                .status(session.getStatus())
                .currentStep(session.getCurrentStep())
                .startedAt(session.getStartedAt())
                .build();
    }
}
