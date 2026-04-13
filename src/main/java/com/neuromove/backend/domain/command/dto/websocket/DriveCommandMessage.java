package com.neuromove.backend.domain.command.dto.websocket;

import lombok.Builder;

@Builder
public record DriveCommandMessage(
        String type,
        String command
) {}