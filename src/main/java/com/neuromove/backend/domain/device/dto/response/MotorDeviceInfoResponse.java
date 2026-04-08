package com.neuromove.backend.domain.device.dto.response;

public record MotorDeviceInfoResponse(
        String motorDeviceId,
        String connectionStatus,
        Boolean cached
) {
}