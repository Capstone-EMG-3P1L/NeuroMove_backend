package com.neuromove.backend.domain.user.dto.response;

public record SessionLogItemResponse(
        String sessionId,
        String emgDeviceId,
        String motorDeviceId,
        String startedAt,
        String endedAt,
        Integer durationSeconds,
        Double maxRiskScore,
        String status
) {
}