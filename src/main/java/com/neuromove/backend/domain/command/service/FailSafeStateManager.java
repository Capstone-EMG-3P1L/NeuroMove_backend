package com.neuromove.backend.domain.command.service;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class FailSafeStateManager {

    private final Map<String, Integer> staleCountMap = new ConcurrentHashMap<>();
    private final Map<String, Integer> abnormalCountMap = new ConcurrentHashMap<>();

    // 연속 stale 카운트 증가
    public int increaseStaleCount(String sessionId) {
        return staleCountMap.merge(sessionId, 1, Integer::sum);
    }

    // stale 카운트 초기화
    public void resetStaleCount(String sessionId) {
        staleCountMap.remove(sessionId);
    }

    // 이상 패턴 카운트 증가
    public int increaseAbnormalCount(String sessionId) {
        return abnormalCountMap.merge(sessionId, 1, Integer::sum);
    }

    // 이상 패턴 초기화
    public void resetAbnormalCount(String sessionId) {
        abnormalCountMap.remove(sessionId);
    }

    // 롤백 시 stale 카운트 1 감소
    public void decreaseStaleCount(String sessionId) {
        staleCountMap.computeIfPresent(sessionId, (key, value) -> value > 1 ? value - 1 : null);
    }

    // 롤백 시 abnormal 카운트 1 감소
    public void decreaseAbnormalCount(String sessionId) {
        abnormalCountMap.computeIfPresent(sessionId, (key, value) -> value > 1 ? value - 1 : null);
    }

    // 세션 종료 시 fail-safe 상태 전체 정리
    public void clear(String sessionId) {
        staleCountMap.remove(sessionId);
        abnormalCountMap.remove(sessionId);
    }
}