package com.neuromove.backend.domain.calibration.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CalibrationStartRequest {

    private String onboardingId;

    private String emgDeviceId;
}