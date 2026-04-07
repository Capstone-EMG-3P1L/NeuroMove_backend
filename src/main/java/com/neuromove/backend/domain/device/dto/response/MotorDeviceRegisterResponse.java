package com.neuromove.backend.domain.device.dto.response;

import com.neuromove.backend.domain.device.entity.MotorDevice;
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

    public static MotorDeviceRegisterResponse from(MotorDevice motorDevice) {
        return MotorDeviceRegisterResponse.builder()
                .motorDeviceId(motorDevice.getMotorDeviceId())
                .userId(motorDevice.getUser().getUserId())
                .name(motorDevice.getName())
                .isActive(motorDevice.isActive())
                .connectionStatus(motorDevice.getConnectionStatus().name())
                .createdAt(motorDevice.getCreatedAt())
                .build();
    }
}