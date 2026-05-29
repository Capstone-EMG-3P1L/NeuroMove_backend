package com.neuromove.backend.domain.calibration.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CalibrationEndRequest {

    private String onboardingId;

    private String calibrationSessionId;

    private Float ch1Mean;
    private Float ch1Std;

    private Float ch2Mean;
    private Float ch2Std;

    private Float ch3Mean;
    private Float ch3Std;

    private Float activationThreshold;

    private Float intentThresholdRest;
    private Float intentThresholdLeft;
    private Float intentThresholdRight;
    private Float intentThresholdStop;

    private Float fatigueBaseline;

    private Float signalQuality;
}