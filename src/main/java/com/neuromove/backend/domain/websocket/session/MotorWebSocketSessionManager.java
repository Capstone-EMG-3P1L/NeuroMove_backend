package com.neuromove.backend.domain.websocket.session;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MotorWebSocketSessionManager {

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public void register(String deviceId, WebSocketSession session) {
        sessions.put(deviceId, session);
    }

    public void remove(String deviceId) {
        sessions.remove(deviceId);
    }

    public Optional<WebSocketSession> getSession(String deviceId) {
        return Optional.ofNullable(sessions.get(deviceId));
    }
}