package com.neuromove.backend.domain.user.dto.response;

public record RegisteredMotorDeviceResponse(
        String motorDeviceId,
        String name,
        Boolean isActive,
        String connectionStatus
) {
}