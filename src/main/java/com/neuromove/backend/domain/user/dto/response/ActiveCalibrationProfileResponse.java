package com.neuromove.backend.domain.user.dto.response;

public record ActiveCalibrationProfileResponse(
        String profileId,
        Double signalQuality,
        String updatedAt
) {
}