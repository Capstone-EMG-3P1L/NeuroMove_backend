package com.neuromove.backend.infrastructure.ai.session.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiSessionStatusResponse {

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
        private AiSessionStatus status;
        private Integer bufferedWindowCount;
        private String lastIntent;
    }
}