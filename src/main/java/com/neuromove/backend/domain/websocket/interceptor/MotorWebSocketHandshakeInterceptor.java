package com.neuromove.backend.domain.websocket.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * [WebSocket 핸드셰이크 인증 인터셉터]
 *
 * ESP32가 ws://host/ws/motor 연결 시 HTTP Upgrade 요청에
 * X-API-KEY 헤더가 있는지 검증.
 * 키가 없거나 틀리면 핸드셰이크를 거부(401)해서 연결 자체를 차단.
 */
@Slf4j
@Component
public class MotorWebSocketHandshakeInterceptor implements HandshakeInterceptor {

    private static final String API_KEY_HEADER = "X-API-KEY";

    @Value("${security.api-key}")
    private String internalApiKey;

    /**
     * 핸드셰이크 전 호출 - false 반환 시 연결 거부
     */
    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {
        String key = request.getHeaders().getFirst(API_KEY_HEADER);

        if (key == null || key.isBlank() || !internalApiKey.equals(key)) {
            log.warn("[WS] 핸드셰이크 거부 - 유효하지 않은 X-API-KEY. remoteAddress={}",
                    request.getRemoteAddress());

            // HTTP 401 반환
            if (response instanceof ServletServerHttpResponse servletResponse) {
                servletResponse.getServletResponse()
                        .setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            }
            return false;
        }

        log.info("[WS] 핸드셰이크 인증 성공. remoteAddress={}", request.getRemoteAddress());
        return true;
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception
    ) {
        // 핸드셰이크 후 처리 불필요
    }
}
