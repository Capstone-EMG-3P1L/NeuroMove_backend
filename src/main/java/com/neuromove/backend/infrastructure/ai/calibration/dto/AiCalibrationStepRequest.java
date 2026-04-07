package com.neuromove.backend.infrastructure.ai.calibration.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AiCalibrationStepRequest {

    private String calibrationSessionId;
    private String step;
}
