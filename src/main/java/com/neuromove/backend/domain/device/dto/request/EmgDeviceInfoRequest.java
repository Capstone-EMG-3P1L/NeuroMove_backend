package com.neuromove.backend.domain.device.dto.request;

import jakarta.validation.constraints.NotBlank;

public record EmgDeviceInfoRequest(
        @NotBlank
        String emgDeviceId,

        @NotBlank
        String connectionStatus
) {
}