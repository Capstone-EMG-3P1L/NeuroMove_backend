package com.neuromove.backend.global.config;

import com.neuromove.backend.domain.websocket.handler.MotorWebSocketHandler;
import com.neuromove.backend.domain.websocket.interceptor.MotorWebSocketHandshakeInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class RawWebSocketConfig implements WebSocketConfigurer {

    private final MotorWebSocketHandler motorWebSocketHandler;
    private final MotorWebSocketHandshakeInterceptor handshakeInterceptor;

    /**
     * Raw WebSocket (모터 보드용)
     * - Origin: ESP32는 Origin 헤더 없이 연결하므로 * 허용
     * - 인증: HandshakeInterceptor에서 X-API-KEY 헤더 검증
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(motorWebSocketHandler, "/ws/motor")
                .addInterceptors(handshakeInterceptor)
                .setAllowedOriginPatterns("*");
    }
}
