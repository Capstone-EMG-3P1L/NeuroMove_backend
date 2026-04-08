package com.neuromove.backend.domain.session.dto.response;

import com.neuromove.backend.domain.session.entity.Session;
import com.neuromove.backend.domain.session.entity.enums.SessionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionEndResponse {
    private String sessionId;
    private SessionStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private Integer durationSeconds;
    private Float maxRiskScore;

    public static SessionEndResponse from(Session session) {
        return SessionEndResponse.builder()
                .sessionId(session.getSessionId())
                .status(session.getStatus())
                .startedAt(session.getStartedAt())
                .endedAt(session.getEndedAt())
                .durationSeconds(session.getDurationSeconds())
                .maxRiskScore(session.getMaxRiskScore())
                .build();
    }
}
