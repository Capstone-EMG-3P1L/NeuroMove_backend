package com.neuromove.backend.domain.user.dto.response;

public record RegisteredEmgDeviceResponse(
        String emgDeviceId,
        String name,
        Boolean isActive
) {
}