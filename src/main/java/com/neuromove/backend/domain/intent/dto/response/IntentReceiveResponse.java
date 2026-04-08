package com.neuromove.backend.domain.intent.dto.response;

import com.neuromove.backend.domain.command.entity.Command;
import com.neuromove.backend.domain.command.entity.enums.CommandType;
import com.neuromove.backend.domain.intent.entity.IntentLog;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class IntentReceiveResponse {

    private String intentId;
    private String sessionId;
    private boolean accepted;
    private float riskScore;
    private CommandDto command;

    @Getter
    @Builder
    public static class CommandDto {
        private String commandId;
        private CommandType command;
        private int speedLevel;
        private LocalDateTime issuedAt;

        public static CommandDto from(Command command) {
            return CommandDto.builder()
                    .commandId(command.getCommandId())
                    .command(command.getCommand())
                    .speedLevel(command.getSpeedLevel())
                    .issuedAt(command.getIssuedAt())
                    .build();
        }
    }

    public static IntentReceiveResponse of(IntentLog intentLog, Command command) {
        return IntentReceiveResponse.builder()
                .intentId(intentLog.getIntentId())
                .sessionId(intentLog.getSession().getSessionId())
                .accepted(intentLog.getAccepted())
                .riskScore(intentLog.getRiskScore())
                .command(CommandDto.from(command))
                .build();
    }
}
