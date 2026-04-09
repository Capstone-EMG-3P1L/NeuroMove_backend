package com.neuromove.backend.domain.device.controller;

import com.neuromove.backend.domain.device.dto.request.MotorDeviceInfoRequest;
import com.neuromove.backend.domain.device.dto.response.MotorDeviceInfoResponse;
import com.neuromove.backend.domain.device.service.DeviceInfoService;
import com.neuromove.backend.global.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/motor-devices-info")
@RequiredArgsConstructor
public class MotorDeviceInfoController {

    private final DeviceInfoService deviceInfoService;

    @PostMapping
    public ApiResponse<MotorDeviceInfoResponse> receiveDeviceInfo(
            @RequestHeader("X-API-KEY") String apiKey,
            @Valid @RequestBody MotorDeviceInfoRequest request
    ) {
        MotorDeviceInfoResponse response = deviceInfoService.saveMotorDeviceInfo(request);

        return ApiResponse.success(
                "MOTOR_DEVICE_INFO_RECEIVED",
                "MOTOR 디바이스 정보를 수신했습니다.",
                response
        );
    }
}