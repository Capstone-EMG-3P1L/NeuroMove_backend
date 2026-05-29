package com.neuromove.backend.domain.calibration.dto.response;

import com.neuromove.backend.domain.calibration.entity.CalibrationProfile;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CalibrationProfileResponse {

    private String profileId;
    private String userId;
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
    private boolean isActive;
    private LocalDateTime updatedAt;

    public static CalibrationProfileResponse from(CalibrationProfile profile) {
        return CalibrationProfileResponse.builder()
                .profileId(profile.getProfileId())
                .userId(profile.getUser().getUserId())
                .ch1Mean(profile.getCh1Mean())
                .ch1Std(profile.getCh1Std())
                .ch2Mean(profile.getCh2Mean())
                .ch2Std(profile.getCh2Std())
                .ch3Mean(profile.getCh3Mean())
                .ch3Std(profile.getCh3Std())
                .activationThreshold(profile.getActivationThreshold())
                .intentThresholdRest(profile.getIntentThresholdRest())
                .intentThresholdLeft(profile.getIntentThresholdLeft())
                .intentThresholdRight(profile.getIntentThresholdRight())
                .intentThresholdStop(profile.getIntentThresholdStop())
                .fatigueBaseline(profile.getFatigueBaseline())
                .signalQuality(profile.getSignalQuality())
                .isActive(profile.isActive())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }
}
