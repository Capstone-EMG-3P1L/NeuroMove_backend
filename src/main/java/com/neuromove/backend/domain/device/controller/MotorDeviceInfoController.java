package com.neuromove.backend.domain.device.controller;

import com.neuromove.backend.domain.device.dto.request.MotorDeviceInfoRequest;
import com.neuromove.backend.domain.device.dto.response.MotorDeviceInfoResponse;
import com.neuromove.backend.global.api.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/api/motor-devices-info")
public class MotorDeviceInfoController {
    @PostMapping
    public ApiResponse<MotorDeviceInfoResponse> receiveDeviceInfo(
            @Valid @RequestBody MotorDeviceInfoRequest request
    ) {
        MotorDeviceInfoResponse response = new MotorDeviceInfoResponse(
                request.motorDeviceId(),
                request.connectionStatus(),
                true
        );

        return ApiResponse.success(
                "MOTOR_DEVICE_INFO_RECEIVED",
                "MOTOR 디바이스 정보를 수신했습니다.",
                response
        );
    }
}
