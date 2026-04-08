package com.neuromove.backend.domain.user.dto.response;

import com.neuromove.backend.domain.session.dto.response.SessionSummaryResponse;

public record UserStatusResponse(
        String userId,
        String username,
        String name,
        RegisteredEmgDeviceResponse registeredEmgDevice,
        RegisteredMotorDeviceResponse registeredMotorDevice,
        ActiveCalibrationProfileResponse activeCalibrationProfile,
        SessionSummaryResponse activeSession
) {
}