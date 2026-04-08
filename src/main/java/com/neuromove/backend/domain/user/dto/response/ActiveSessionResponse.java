package com.neuromove.backend.domain.user.dto.response;

public record ActiveSessionResponse(
        String sessionId,
        String status,
        String startedAt
) {
}