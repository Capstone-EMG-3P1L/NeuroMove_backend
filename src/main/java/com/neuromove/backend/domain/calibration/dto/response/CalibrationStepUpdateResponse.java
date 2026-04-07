package com.neuromove.backend.domain.calibration.dto.response;

import com.neuromove.backend.domain.calibration.entity.CalibrationSession;
import com.neuromove.backend.domain.calibration.entity.enums.CalibrationStep;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CalibrationStepUpdateResponse {

    private String calibrationSessionId;
    private CalibrationStep currentStep;
    private CalibrationStep nextStep;
    private LocalDateTime updatedAt;

    public static CalibrationStepUpdateResponse from(CalibrationSession session) {
        return CalibrationStepUpdateResponse.builder()
                .calibrationSessionId(session.getCalibrationSessionId())
                .currentStep(session.getCurrentStep())
                .nextStep(session.getCurrentStep().next())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
