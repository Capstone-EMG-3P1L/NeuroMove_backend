package com.neuromove.backend.domain.device.dto.request;

import jakarta.validation.constraints.NotBlank;

public record MotorDeviceInfoRequest(
        @NotBlank
        String motorDeviceId,

        @NotBlank
        String connectionStatus
) {
}