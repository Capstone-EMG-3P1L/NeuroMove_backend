package com.neuromove.backend.infrastructure.ai.session.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiSessionStartResponse {

    private Boolean success;
    private String message;
    private Data data;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Data {
        private String sessionId;
        private String userId;
        private String deviceId;
        private AiSessionStatus status;
        private Long startedAt;
    }
}