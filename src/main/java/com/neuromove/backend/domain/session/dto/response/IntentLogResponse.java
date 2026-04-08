package com.neuromove.backend.domain.session.dto.response;

public record IntentLogResponse(
        String intent,
        Double riskScore,
        String loggedAt
) {
}