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
}