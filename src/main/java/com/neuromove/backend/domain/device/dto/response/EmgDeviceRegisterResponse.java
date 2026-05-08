package com.neuromove.backend.domain.device.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class EmgDeviceRegisterResponse {

    private String emgDeviceId;
    private String userId;
    private String name;
    private boolean isActive;
    private LocalDateTime createdAt;

    /**
     * 온보딩 모드 응답 팩토리
     * - User가 아직 DB에 없으므로 userId는 null
     * - 디바이스도 아직 DB에 저장되지 않았으므로 createdAt도 null
     * - Redis에 임시 저장된 정보만 반환
     */
    public static EmgDeviceRegisterResponse ofOnboarding(String emgDeviceId, String name) {
        return EmgDeviceRegisterResponse.builder()
                .emgDeviceId(emgDeviceId)
                .userId(null)
                .name(name)
                .isActive(true)
                .createdAt(null)
                .build();
    }
}