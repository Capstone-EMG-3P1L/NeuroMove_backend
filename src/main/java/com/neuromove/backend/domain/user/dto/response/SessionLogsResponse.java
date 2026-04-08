package com.neuromove.backend.domain.user.dto.response;

import java.util.List;

public record SessionLogsResponse(
        List<SessionLogItemResponse> logs
) {
}