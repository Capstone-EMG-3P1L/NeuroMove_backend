package com.neuromove.backend.domain.user.dto.response;

public record UserStatusResponse(
        String userId,
        String username,
        String name,
        RegisteredEmgDeviceResponse registeredEmgDevice,
        RegisteredMotorDeviceResponse registeredMotorDevice,
        ActiveCalibrationProfileResponse activeCalibrationProfile,
        Object activeSession
) {
}