package com.neuromove.backend.domain.session.dto.response;

import com.neuromove.backend.domain.command.entity.Command;
import com.neuromove.backend.domain.fsm.entity.FsmState;
import com.neuromove.backend.domain.session.entity.Session;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SessionStatusResponse {

    private String sessionId;
    private String status;
    private String emgDeviceId;
    private String motorDeviceId;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private Integer durationSeconds;
    private Float maxRiskScore;
    private String latestFsmState;
    private LatestCommandResponse latestCommand;

    public static SessionStatusResponse of(Session session, FsmState fsmState, Command command) {
        return SessionStatusResponse.builder()
                .sessionId(session.getSessionId())
                .status(session.getStatus().name())
                .emgDeviceId(session.getEmgDevice().getEmgDeviceId())
                .motorDeviceId(session.getMotorDevice().getMotorDeviceId())
                .startedAt(session.getStartedAt())
                .endedAt(session.getEndedAt())
                .durationSeconds(session.getDurationSeconds())
                .maxRiskScore(session.getMaxRiskScore())
                .latestFsmState(fsmState != null ? fsmState.getToState().name() : null)
                .latestCommand(command != null ? LatestCommandResponse.from(command) : null)
                .build();
    }

    @Getter
    @Builder
    public static class LatestCommandResponse {
        private String command;
        private Integer speedLevel;
        private LocalDateTime issuedAt;

        public static LatestCommandResponse from(Command command) {
            return LatestCommandResponse.builder()
                    .command(command.getCommand().name())
                    .speedLevel(command.getSpeedLevel())
                    .issuedAt(command.getIssuedAt())
                    .build();
        }
    }
}
