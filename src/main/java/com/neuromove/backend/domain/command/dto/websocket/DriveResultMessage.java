package com.neuromove.backend.domain.command.dto.websocket;

import lombok.Builder;

@Builder
public record DriveResultMessage(
        String type,
        String status
) {}