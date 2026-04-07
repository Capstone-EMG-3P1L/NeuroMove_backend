package com.neuromove.backend.domain.device.dto.response;

import com.neuromove.backend.domain.device.entity.EmgDevice;
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

    public static EmgDeviceRegisterResponse from(EmgDevice emgDevice) {
        return EmgDeviceRegisterResponse.builder()
                .emgDeviceId(emgDevice.getEmgDeviceId())
                .userId(emgDevice.getUser().getUserId())
                .name(emgDevice.getName())
                .isActive(emgDevice.isActive())
                .createdAt(emgDevice.getCreatedAt())
                .build();
    }
}