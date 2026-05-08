package com.neuromove.backend.domain.device.controller;

import com.neuromove.backend.domain.device.dto.request.MotorDeviceInfoRequest;
import com.neuromove.backend.domain.device.dto.response.MotorDeviceInfoResponse;
import com.neuromove.backend.domain.device.service.DeviceInfoService;
import com.neuromove.backend.global.api.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "MOTOR Device", description = "MOTOR 디바이스 등록 / 조회 / 펌웨어 수신")
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