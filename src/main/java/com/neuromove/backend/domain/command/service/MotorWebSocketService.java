package com.neuromove.backend.domain.command.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neuromove.backend.domain.command.dto.websocket.DriveCommandMessage;
import com.neuromove.backend.domain.websocket.session.MotorWebSocketSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

/**
 * [모터 웹소켓 명령 전송 서비스]
 * - 서버 → 모터 보드로 주행 명령을 전달하는 역할
 * - deviceId 기준으로 연결된 WebSocketSession을 찾아 메시지 전송
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MotorWebSocketService {

    private final MotorWebSocketSessionManager sessionManager;
    private final ObjectMapper objectMapper;

    /**
     * [모터에게 주행 명령 전송]
     *
     * @param deviceId 모터 디바이스 ID
     * @param command  주행 명령 (FORWARD / LEFT / RIGHT / STOP)
     * @return 전송 성공 여부
     */
    public boolean sendCommand(String deviceId, String command) {

        // deviceId로 현재 연결된 WebSocketSession 조회
        return sessionManager.getSession(deviceId)
                .filter(WebSocketSession::isOpen) // 연결 살아있는지 확인
                .map(session -> send(session, command))
                .orElseGet(() -> {
                    log.warn("모터가 연결되어 있지 않음: deviceId={}", deviceId);
                    return false;
                });
    }

    /**
     * 실제 메시지 전송 처리
     */
    private boolean send(WebSocketSession session, String command) {
        try {
            // DTO 생성
            DriveCommandMessage message = DriveCommandMessage.builder()
                    .type("drive_command")
                    .command(command)
                    .build();

            // JSON 변환
            String json = objectMapper.writeValueAsString(message);

            // WebSocket으로 전송
            session.sendMessage(new TextMessage(json));

            log.info("모터 명령 전송 성공: {}", json);

            return true;

        } catch (Exception e) {
            log.error("모터 명령 전송 실패", e);
            return false;
        }
    }
}