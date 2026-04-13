package com.neuromove.backend.test;

import com.neuromove.backend.domain.session.dto.websocket.SessionUpdateMessage;
import com.neuromove.backend.domain.session.service.SessionWebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Profile("local")
@RestController
@RequiredArgsConstructor
@RequestMapping("/test")
public class SessionWebSocketTestController {

    private final SessionWebSocketService sessionWebSocketService;

    @PostMapping("/session")
    public String testSession() {
        sessionWebSocketService.sendSessionUpdate(
                "7da1a1c6-7d0b-46c1-bbd9-a7135bd30d64",
                SessionUpdateMessage.builder()
                        .type("SESSION_UPDATE")
                        .sessionId("7da1a1c6-7d0b-46c1-bbd9-a7135bd30d64")
                        .fsmState("DRIVING")
                        .intent("LEFT")
                        .confidence(0.99)
                        .riskScore(0.24)
                        .command("LEFT")
                        .speedLevel(4)
                        .timestamp(LocalDateTime.now().toString())
                        .build()
        );

        return "sent";
    }
}