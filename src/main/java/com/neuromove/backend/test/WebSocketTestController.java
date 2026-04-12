package com.neuromove.backend.test;

import com.neuromove.backend.domain.command.service.MotorWebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/test")
public class WebSocketTestController {

    private final MotorWebSocketService motorWebSocketService;

    /**
     * 모터 웹소켓 테스트용 API
     * - 서버 → 모터 명령 전송 확인
     */
    @PostMapping("/motor")
    public String testMotor() {
        boolean sent = motorWebSocketService.sendCommand("motor-001", "FORWARD");
        return sent ? "sent" : "failed";
    }
}