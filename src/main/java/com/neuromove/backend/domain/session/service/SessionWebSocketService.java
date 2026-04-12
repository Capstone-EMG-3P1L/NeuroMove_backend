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
        simpMessagingTemplate.convertAndSend("/topic/sessions/" + sessionId, message);
    }
}