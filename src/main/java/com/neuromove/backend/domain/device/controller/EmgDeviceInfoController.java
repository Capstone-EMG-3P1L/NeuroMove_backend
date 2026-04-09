package com.neuromove.backend.domain.device.controller;

import com.neuromove.backend.domain.device.dto.request.EmgDeviceInfoRequest;
import com.neuromove.backend.domain.device.dto.response.EmgDeviceInfoResponse;
import com.neuromove.backend.domain.device.service.DeviceInfoService;
import com.neuromove.backend.global.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/emg-devices-info")
@RequiredArgsConstructor
public class EmgDeviceInfoController {

    private final DeviceInfoService deviceInfoService;

    @PostMapping
    public ApiResponse<EmgDeviceInfoResponse> receiveDeviceInfo(
            @RequestHeader("X-API-KEY") String apiKey,
            @Valid @RequestBody EmgDeviceInfoRequest request
    ) {
        EmgDeviceInfoResponse response = deviceInfoService.saveEmgDeviceInfo(request);

        return ApiResponse.success(
                "EMG_DEVICE_INFO_RECEIVED",
                "EMG 디바이스 정보를 수신했습니다.",
                response
        );
    }
}