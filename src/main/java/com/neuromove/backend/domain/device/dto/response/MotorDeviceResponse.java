package com.neuromove.backend.domain.device.dto.response;

import com.neuromove.backend.domain.device.entity.MotorDevice;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MotorDeviceResponse {

    private String motorDeviceId;
    private String name;
    private boolean isActive;
    private String connectionStatus;
    private LocalDateTime createdAt;

    public static MotorDeviceResponse from(MotorDevice motorDevice) {
        return MotorDeviceResponse.builder()
                .motorDeviceId(motorDevice.getMotorDeviceId())
                .name(motorDevice.getName())
                .isActive(motorDevice.isActive())
                .connectionStatus(motorDevice.getConnectionStatus().name())
                .createdAt(motorDevice.getCreatedAt())
                .build();
    }
}