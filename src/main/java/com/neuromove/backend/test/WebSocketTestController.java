package com.neuromove.backend.test;

import com.neuromove.backend.domain.command.service.MotorWebSocketService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Test API", description = "테스트용 API입니다.")
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