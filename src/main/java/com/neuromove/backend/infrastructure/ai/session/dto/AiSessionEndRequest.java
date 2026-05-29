package com.neuromove.backend.infrastructure.ai.session.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiSessionEndRequest {

    private String sessionId;
}