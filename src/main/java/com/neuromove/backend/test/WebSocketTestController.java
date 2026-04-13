package com.neuromove.backend.test;

import com.neuromove.backend.domain.command.service.MotorWebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

@Profile("local")
@RestController
@RequiredArgsConstructor
@RequestMapping("/test")
public class WebSocketTestController {

    private final MotorWebSocketService motorWebSocketService;

    @PostMapping("/motor")
    public String testMotor() {
        boolean sent = motorWebSocketService.sendCommand("motor1", "LEFT");
        return sent ? "sent" : "failed";
    }
}