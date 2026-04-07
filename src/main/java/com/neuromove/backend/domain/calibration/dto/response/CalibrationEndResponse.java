package com.neuromove.backend.domain.calibration.dto.response;

import com.neuromove.backend.domain.calibration.entity.CalibrationProfile;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CalibrationEndResponse {

    private String profileId;
    private String calibrationSessionId;
    private Float signalQuality;
    private boolean isActive;
    private LocalDateTime createdAt;

    public static CalibrationEndResponse from(CalibrationProfile profile) {
        return CalibrationEndResponse.builder()
                .profileId(profile.getProfileId())
                .calibrationSessionId(profile.getCalibrationSessionId())
                .signalQuality(profile.getSignalQuality())
                .isActive(profile.isActive())
                .createdAt(profile.getCreatedAt())
                .build();
    }
}
