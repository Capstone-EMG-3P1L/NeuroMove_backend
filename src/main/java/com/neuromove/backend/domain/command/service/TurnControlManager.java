package com.neuromove.backend.domain.command.service;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.*;

/**
 * [회전 제어 매니저]
 *
 * AI 전송 주기: 0.5초 기준으로 설계
 *
 * LEFT / RIGHT 확정 로직:
 *   1. 연속 CONSECUTIVE_THRESHOLD(2)회 같은 방향 → 확정, 즉시 전송
 *   2. 확정 후 lastExecutedTurn = "LEFT" or "RIGHT" 저장
 *   3. 같은 방향 intent가 또 오면 → 이미 실행됨 → 무시
 *   4. AI가 FORWARD intent를 보내면 → lastExecutedTurn 초기화
 *   5. 이후 LEFT/RIGHT 오면 다시 새 카운트로 인정
 *
 *   ※ 백엔드 auto-FORWARD(500ms 후 스케줄)는 process()를 거치지 않으므로
 *     lastExecutedTurn을 리셋하지 않는다. 오직 AI FORWARD intent만 리셋.
 *
 * FORWARD 는 즉시 반영하고 카운트 및 lastExecutedTurn 초기화
 * STOP / EMERGENCY_STOP 은 안전 최우선으로 즉시 반영하고 카운트만 초기화
 *   (lastExecutedTurn은 유지 — FORWARD 오기 전까지 같은 방향 재실행 방지)
 *
 * 나중에 조절:
 *   너무 많이 꺾임  → TURN_DURATION_MS 400으로 감소
 *   너무 적게 꺾임  → TURN_DURATION_MS 600으로 증가
 *   너무 민감함     → CONSECUTIVE_THRESHOLD 3으로 증가
 *   반응 느림       → CONSECUTIVE_THRESHOLD 1로 감소 (즉시 반영)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TurnControlManager {

    // ──────────────────────────────────────────
    //  설정값 (실험 후 조절) — AI 전송 주기 0.5초 기준
    // ──────────────────────────────────────────
    private static final int  CONSECUTIVE_THRESHOLD = 2;    // 연속 2회(=1.0초) 확정
    private static final long TURN_DURATION_MS      = 500L; // 회전 지속 시간 (ms) — 범위: 400~600
    // 쿨다운 없음: lastExecutedTurn 상태 기반으로 중복 실행 차단
    // (AI FORWARD intent 수신 시에만 리셋되어 다음 회전 허용)

    private final MotorWebSocketService motorWebSocketService;

    // 세션별 상태
    private final Map<String, TurnControlState> states = new ConcurrentHashMap<>();

    // FORWARD 예약용 스케줄러
    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(4, r -> {
                Thread t = new Thread(r, "turn-control");
                t.setDaemon(true);
                return t;
            });

    // ──────────────────────────────────────────
    //  핵심 처리 메서드
    //  반환값: 실제로 모터에 전송한 명령 (null = 억제됨)
    // ──────────────────────────────────────────
    public String process(String sessionId, String motorDeviceId, String command) {

        // LEFT / RIGHT 이외 명령 → 즉시 전송
        if (!command.equals("LEFT") && !command.equals("RIGHT")) {
            TurnControlState state = states.get(sessionId);
            if (state != null) {
                state.cancelPendingForward();
                state.consecutiveCount = 0;
                state.lastDirection    = "";
                state.lastExecutedTurn = "";  // 모든 비-회전 명령에서 초기화
            }
            boolean sent = motorWebSocketService.sendCommand(motorDeviceId, command);
            if (!sent) log.warn("[TURN] 명령 전송 실패: sessionId={}, command={}", sessionId, command);
            return sent ? command : null;
        }

        // LEFT / RIGHT 처리
        TurnControlState state = states.computeIfAbsent(sessionId, k -> new TurnControlState());

        // 이미 같은 방향 실행됨 → FORWARD 올 때까지 무시
        if (command.equals(state.lastExecutedTurn)) {
            log.debug("[TURN] 이미 실행된 방향 무시 (FORWARD 대기 중): sessionId={}, command={}", sessionId, command);
            return null;
        }

        // 연속 카운트 업데이트
        if (command.equals(state.lastDirection)) {
            state.consecutiveCount++;
        } else {
            state.consecutiveCount = 1;
            state.lastDirection    = command;
        }

        log.debug("[TURN] 연속 카운트: sessionId={}, command={}, count={}/{} (확정까지 {}회 남음)",
                sessionId, command, state.consecutiveCount, CONSECUTIVE_THRESHOLD,
                Math.max(0, CONSECUTIVE_THRESHOLD - state.consecutiveCount));

        // 아직 임계값 미달 → 전송 안 함
        if (state.consecutiveCount < CONSECUTIVE_THRESHOLD) {
            return null;
        }

        // ── 확정! ──
        log.info("[TURN] {} 확정 (연속 {}회 = {}ms): sessionId={}",
                command, CONSECUTIVE_THRESHOLD, (long) CONSECUTIVE_THRESHOLD * 500L, sessionId);

        // 카운트 초기화 + 실행된 방향 기록
        state.consecutiveCount = 0;
        state.lastDirection    = "";
        state.lastExecutedTurn = command; // FORWARD 올 때까지 같은 방향 무시

        // 회전 명령 즉시 전송
        boolean sent = motorWebSocketService.sendCommand(motorDeviceId, command);
        if (!sent) {
            log.warn("[TURN] 회전 명령 전송 실패: sessionId={}, command={}", sessionId, command);
            state.lastExecutedTurn = ""; // 전송 실패 시 상태 롤백
            return null;
        }

        // TURN_DURATION_MS 후 FORWARD 예약 (auto-FORWARD는 process()를 거치지 않아 lastExecutedTurn 유지됨)
        state.cancelPendingForward();
        state.pendingForward = scheduler.schedule(() -> {
            log.info("[TURN] 회전 완료 → auto-FORWARD 전송: sessionId={}", sessionId);
            motorWebSocketService.sendCommand(motorDeviceId, "FORWARD");
            state.lastExecutedTurn = "";
        }, TURN_DURATION_MS, TimeUnit.MILLISECONDS);

        return command;
    }

    /**
     * 세션 종료 시 상태 정리 (SessionService.end() 에서 호출)
     * — pendingForward 취소 후 상태 제거
     */
    public void clear(String sessionId) {
        TurnControlState state = states.remove(sessionId);
        if (state != null) {
            state.cancelPendingForward();
            log.info("[TURN] 세션 상태 정리 완료: sessionId={}", sessionId);
        }
    }

    @PreDestroy
    public void shutdown() {
        scheduler.shutdownNow();
    }

    // ──────────────────────────────────────────
    //  세션별 내부 상태
    // ──────────────────────────────────────────
    private static class TurnControlState {
        String             lastDirection    = "";   // 현재 카운트 중인 방향
        int                consecutiveCount = 0;    // 연속 카운트
        String             lastExecutedTurn = "";   // 마지막으로 실행한 회전 방향 ("" = 없음)
        ScheduledFuture<?> pendingForward   = null; // auto-FORWARD 예약 태스크

        void cancelPendingForward() {
            if (pendingForward != null && !pendingForward.isDone()) {
                pendingForward.cancel(false);
                pendingForward = null;
            }
        }
    }
}
