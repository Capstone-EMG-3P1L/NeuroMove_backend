package com.neuromove.backend.domain.device.controller;

import com.neuromove.backend.domain.device.dto.request.EmgDeviceInfoRequest;
import com.neuromove.backend.domain.device.dto.response.EmgDeviceInfoResponse;
import com.neuromove.backend.global.api.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/api/emg-devices-info")
public class EmgDeviceInfoController {
    @PostMapping
    public ApiResponse<EmgDeviceInfoResponse> receiveDeviceInfo(
            @Valid @RequestBody EmgDeviceInfoRequest request
    ) {
        EmgDeviceInfoResponse response = new EmgDeviceInfoResponse(
                request.emgDeviceId(),
                request.connectionStatus(),
                true
        );

        return ApiResponse.success(
                "EMG_DEVICE_INFO_RECEIVED",
                "EMG 디바이스 정보를 수신했습니다.",
                response
        );
    }
}