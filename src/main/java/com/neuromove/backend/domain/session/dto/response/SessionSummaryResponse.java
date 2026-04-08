package com.neuromove.backend.domain.session.dto.response;

public record SessionSummaryResponse(
        String sessionId,
        String emgDeviceId,
        String motorDeviceId,
        String status,
        String startedAt,
        String endedAt,
        Integer durationSeconds,
        Double maxRiskScore
) {
}