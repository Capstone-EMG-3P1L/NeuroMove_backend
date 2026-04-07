package com.neuromove.backend.infrastructure.ai.calibration.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AiCalibrationStepResponse {

    private boolean success;
    private String message;
    private Data data;

    @Getter
    @NoArgsConstructor
    public static class Data {
        private String calibrationSessionId;
        private String currentStep;
        private String status;
    }
}
