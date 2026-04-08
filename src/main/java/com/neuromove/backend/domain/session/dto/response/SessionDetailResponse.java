package com.neuromove.backend.domain.session.dto.response;

import java.util.List;

public record SessionDetailResponse(
        SessionSummaryResponse session,
        List<FsmStateLogResponse> fsmStates,
        List<IntentLogResponse> intentLogs
) {
}