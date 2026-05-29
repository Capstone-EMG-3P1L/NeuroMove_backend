package com.neuromove.backend.infrastructure.ai.session.dto;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiSessionStartRequest {

    private String sessionId;
    private String userId;
    private String deviceId;
    private String profileId;
    private CalibrationSnapshot calibration;


    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CalibrationSnapshot {
        private Baseline baseline;
        private Double activationThreshold;
        private Map<String, Double> intentThresholds;
        private Double fatigueBaseline;
        private Double signalQuality;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Baseline {
        private Double ch1Mean;
        private Double ch1Std;
        private Double ch2Mean;
        private Double ch2Std;
        private Double ch3Mean;
        private Double ch3Std;
    }
}
