package com.neuromove.backend.domain.session.dto.response;
import java.time.LocalDateTime;

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