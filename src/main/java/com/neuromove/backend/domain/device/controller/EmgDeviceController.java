package com.neuromove.backend.domain.device.controller;

import com.neuromove.backend.domain.auth.jwt.CustomUserPrincipal;
import com.neuromove.backend.domain.device.dto.request.EmgDeviceRegisterRequest;
import com.neuromove.backend.domain.device.dto.response.EmgDeviceListResponse;
import com.neuromove.backend.domain.device.dto.response.EmgDeviceRegisterResponse;
import com.neuromove.backend.domain.device.service.EmgDeviceService;
import com.neuromove.backend.domain.user.entity.User;
import com.neuromove.backend.domain.user.repository.UserRepository;
import com.neuromove.backend.global.api.ApiResponse;
import com.neuromove.backend.global.exception.CustomException;
import com.neuromove.backend.global.exception.ErrorCode;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/api/emg-devices")
@RequiredArgsConstructor
public class EmgDeviceController {

    private final EmgDeviceService emgDeviceService;
    private final UserRepository userRepository;

    @PostMapping
    public ApiResponse<EmgDeviceRegisterResponse> register(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody EmgDeviceRegisterRequest request
    ) {
        User user = userRepository.findByUsername(principal.username())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        EmgDeviceRegisterResponse response = emgDeviceService.register(user, request);
        return ApiResponse.success("EMG_DEVICE_REGISTERED", "EMG 디바이스가 등록되었습니다.", response);
    }

    @GetMapping
    public ApiResponse<EmgDeviceListResponse> getMyDevices(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        User user = userRepository.findByUsername(principal.username())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        EmgDeviceListResponse response = emgDeviceService.getMyDevices(user);
        return ApiResponse.success("EMG_DEVICE_LIST_FETCHED", "EMG 디바이스 목록을 조회했습니다.", response);
    }
}