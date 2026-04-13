package com.neuromove.backend.domain.session.service;

import com.neuromove.backend.domain.session.dto.websocket.SessionUpdateMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SessionWebSocketService {

    private final SimpMessagingTemplate simpMessagingTemplate;

    public void sendSessionUpdate(String sessionId, SessionUpdateMessage message) {
        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException("sessionId는 비어 있을 수 없습니다.");
        }
        if (message == null) {
            throw new IllegalArgumentException("message는 null일 수 없습니다.");
        }
        if (message.sessionId() != null && !sessionId.equals(message.sessionId())) {
            throw new IllegalArgumentException("topic sessionId와 payload sessionId가 일치하지 않습니다.");
        }

        simpMessagingTemplate.convertAndSend("/topic/sessions/" + sessionId, message);
    }
}