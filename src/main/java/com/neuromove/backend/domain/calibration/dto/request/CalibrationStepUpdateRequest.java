package com.neuromove.backend.domain.calibration.dto.request;

import com.neuromove.backend.domain.calibration.entity.enums.CalibrationStep;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CalibrationStepUpdateRequest {

    /**
     * 온보딩 모드에서 사용. null이면 일반 모드(JWT 인증된 사용자).
     */
    private String onboardingId;

    @NotBlank
    private String calibrationSessionId;

    @NotNull
    private CalibrationStep step;
}
