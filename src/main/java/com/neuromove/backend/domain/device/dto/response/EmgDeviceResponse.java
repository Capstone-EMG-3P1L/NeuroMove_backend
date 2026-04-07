package com.neuromove.backend.domain.device.dto.response;

import com.neuromove.backend.domain.device.entity.EmgDevice;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class EmgDeviceResponse {

    private String emgDeviceId;
    private String name;
    private boolean isActive;
    private LocalDateTime createdAt;

    public static EmgDeviceResponse from(EmgDevice emgDevice) {
        return EmgDeviceResponse.builder()
                .emgDeviceId(emgDevice.getEmgDeviceId())
                .name(emgDevice.getName())
                .isActive(emgDevice.isActive())
                .createdAt(emgDevice.getCreatedAt())
                .build();
    }
}