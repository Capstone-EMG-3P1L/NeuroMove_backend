package com.neuromove.backend.domain.calibration.dto.request;

import com.neuromove.backend.domain.calibration.entity.enums.CalibrationStep;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CalibrationStepUpdateRequest {

    @NotBlank
    private String calibrationSessionId;

    @NotNull
    private CalibrationStep step;
}
