package com.neuromove.backend.infrastructure.ai.calibration.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@NoArgsConstructor
public class AiCalibrationFinishResponse {

    private boolean success;
    private String message;
    private Data data;

    @Getter
    @NoArgsConstructor
    public static class Data {
        private String calibrationSessionId;
        private String userId;
        private String deviceId;
        private Result result;
        private Long completedAt;
    }

    @Getter
    @NoArgsConstructor
    public static class Result {
        private Baseline baseline;
        private Float activationThreshold;
        private Map<String, Float> intentThresholds;
        private Float fatigueBaseline;
        private Float signalQuality;
    }

    @Getter
    @NoArgsConstructor
    public static class Baseline {
        private Float ch1Mean;
        private Float ch1Std;
        private Float ch2Mean;
        private Float ch2Std;
        private Float ch3Mean;
        private Float ch3Std;
    }
}
