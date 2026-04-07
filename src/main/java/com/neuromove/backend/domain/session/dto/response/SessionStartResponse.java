package com.neuromove.backend.domain.session.dto.response;

import com.neuromove.backend.domain.session.entity.Session;
import com.neuromove.backend.domain.session.entity.enums.SessionStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SessionStartResponse {

    private String sessionId;
    private String userId;
    private String profileId;
    private String emgDeviceId;
    private String motorDeviceId;
    private SessionStatus status;
    private LocalDateTime startedAt;

    public static SessionStartResponse from(Session session) {
        return SessionStartResponse.builder()
                .sessionId(session.getSessionId())
                .userId(session.getUser().getUserId())
                .profileId(session.getCalibrationProfile().getProfileId())
                .emgDeviceId(session.getEmgDevice().getEmgDeviceId())
                .motorDeviceId(session.getMotorDevice().getMotorDeviceId())
                .status(session.getStatus())
                .startedAt(session.getStartedAt())
                .build();
    }
}
