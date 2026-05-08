package com.neuromove.backend.domain.device.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MotorDeviceRegisterResponse {

    private String motorDeviceId;
    private String userId;
    private String name;
    private boolean isActive;
    private String connectionStatus;
    private LocalDateTime createdAt;

    /**
     * 온보딩 모드 응답 팩토리
     * - User/DB 저장 전이므로 userId, createdAt은 null
     * - 연결 상태는 등록 시점에 CONNECTED로 가정
     */
    public static MotorDeviceRegisterResponse ofOnboarding(String motorDeviceId, String name) {
        return MotorDeviceRegisterResponse.builder()
                .motorDeviceId(motorDeviceId)
                .userId(null)
                .name(name)
                .isActive(true)
                .connectionStatus("CONNECTED")
                .createdAt(null)
                .build();
    }
}