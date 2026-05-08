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

    /**
     * 온보딩 모드 응답 팩토리
     * - DB profile이 아직 만들어지지 않았으므로 profileId, createdAt은 null
     * - sessionId만 클라이언트에 돌려줘서 complete 단계까지 추적 가능하게 함
     */
    public static CalibrationEndResponse ofOnboarding(String calibrationSessionId, Float signalQuality) {
        return CalibrationEndResponse.builder()
                .profileId(null)
                .calibrationSessionId(calibrationSessionId)
                .signalQuality(signalQuality)
                .isActive(false)
                .createdAt(null)
                .build();
    }
}
