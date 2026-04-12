package com.neuromove.backend.domain.websocket.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neuromove.backend.domain.websocket.session.MotorWebSocketSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public class MotorWebSocketHandler extends TextWebSocketHandler {

    private final MotorWebSocketSessionManager sessionManager;
    private final ObjectMapper objectMapper;

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        JsonNode json = objectMapper.readTree(message.getPayload());
        String type = json.get("type").asText();

        if ("register".equals(type)) {
            String deviceId = json.get("deviceId").asText();

            sessionManager.register(deviceId, session);
            session.getAttributes().put("deviceId", deviceId);

            log.info("모터 등록됨: {}", deviceId);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {

        Object deviceId = session.getAttributes().get("deviceId");

        if (deviceId != null) {
            sessionManager.remove(deviceId.toString());
            log.info("모터 연결 종료: {}", deviceId);
        }
    }
}