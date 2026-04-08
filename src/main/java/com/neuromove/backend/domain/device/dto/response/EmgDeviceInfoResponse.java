package com.neuromove.backend.domain.device.dto.response;

public record EmgDeviceInfoResponse(
        String emgDeviceId,
        String connectionStatus,
        Boolean cached
) {
}