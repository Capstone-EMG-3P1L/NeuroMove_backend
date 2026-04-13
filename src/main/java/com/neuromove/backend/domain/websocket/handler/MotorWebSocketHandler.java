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

        String type = json.path("type").asText(null);

        if (type == null) {
            log.warn("type 없는 메시지 수신: {}", message.getPayload());
            return;
        }

        /**
         * 1. REGISTER 처리
         */
        if ("register".equals(type)) {

            String deviceId = json.path("deviceId").asText(null);

            if (deviceId == null) {
                log.warn("deviceId 없는 register 메시지");
                return;
            }

            String previousDeviceId = (String) session.getAttributes().get("deviceId");

            if (previousDeviceId != null && !previousDeviceId.equals(deviceId)) {
                sessionManager.remove(previousDeviceId, session);
                log.info("기존 모터 매핑 제거: {}", previousDeviceId);
            }

            sessionManager.register(deviceId, session);
            session.getAttributes().put("deviceId", deviceId);

            log.info("모터 등록됨: {}", deviceId);
        }

        /**
         * 2. drive_result 처리
         */
        else if ("drive_result".equals(type)) {

            String status = json.path("status").asText(null);

            String deviceId = (String) session.getAttributes().get("deviceId");

            if (deviceId == null) {
                log.warn("deviceId 없는 상태에서 결과 수신");
                return;
            }

            log.info("모터 결과 수신: deviceId={}, status={}", deviceId, status);
        }

        /**
         * 3. 알 수 없는 메시지
         */
        else {
            log.warn("알 수 없는 메시지 타입: {}", type);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Object deviceId = session.getAttributes().get("deviceId");

        if (deviceId != null) {
            sessionManager.remove(deviceId.toString(), session);
            log.info("모터 연결 종료: {}", deviceId);
        }
    }
}