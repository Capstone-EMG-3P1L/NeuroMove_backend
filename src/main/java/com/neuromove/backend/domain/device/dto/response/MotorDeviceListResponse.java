package com.neuromove.backend.domain.device.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MotorDeviceListResponse {

    private List<MotorDeviceResponse> devices;
}