package com.neuromove.backend.domain.session.dto.response;

public record FsmStateLogResponse(
        String fromState,
        String toState,
        String reason,
        String transitionedAt
) {
}