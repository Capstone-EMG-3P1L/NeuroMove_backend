package com.neuromove.backend.infrastructure.ai.calibration.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AiCalibrationStartResponse {

    private boolean success;
    private String message;
    private Data data;

    @Getter
    @NoArgsConstructor
    public static class Data {
        private String calibrationSessionId;
        private String userId;
        private String deviceId;
        private String status;
        private String currentStep;
        private Long startedAt;
    }
}
