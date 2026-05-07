package com.neuromove.backend.domain.device.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MotorDeviceRegisterRequest {

    private String onboardingId;

    @NotBlank(message = "디바이스 이름은 필수입니다.")
    private String name;
}