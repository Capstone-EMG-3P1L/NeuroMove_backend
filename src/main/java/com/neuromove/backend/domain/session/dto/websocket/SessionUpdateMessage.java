package com.neuromove.backend.domain.session.dto.websocket;

import lombok.Builder;

@Builder
public record SessionUpdateMessage(
        String type,
        String sessionId,
        String fsmState,
        String intent,
        Double confidence,
        Double riskScore,
        String command,
        Integer speedLevel,
        String timestamp
) {
}