package com.neuromove.backend.global.config;

import com.neuromove.backend.domain.websocket.handler.MotorWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class RawWebSocketConfig implements WebSocketConfigurer {

    private final MotorWebSocketHandler motorWebSocketHandler;

    @Value("${app.websocket.allowed-origins:*}")
    private String[] allowedOrigins;
    /**
     * Raw WebSocket (모터 보드용)
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(motorWebSocketHandler, "/ws/motor")
                .setAllowedOriginPatterns(allowedOrigins);
    }
}