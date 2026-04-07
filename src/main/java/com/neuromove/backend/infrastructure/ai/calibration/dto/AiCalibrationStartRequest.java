package com.neuromove.backend.infrastructure.ai.calibration.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AiCalibrationStartRequest {

    private String calibrationSessionId;
    private String userId;
    private String deviceId;
    private String initialStep;
}
