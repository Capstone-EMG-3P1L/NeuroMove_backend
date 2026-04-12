package com.neuromove.backend.domain.websocket.controller;

import com.neuromove.backend.domain.command.service.MotorWebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/test")
public class WebSocketTestController {

    private final MotorWebSocketService motorWebSocketService;

    @PostMapping("/motor")
    public String testMotor() {
        boolean sent = motorWebSocketService.sendCommand("motor-001", "FORWARD");
        return sent ? "sent" : "failed";
    }
}