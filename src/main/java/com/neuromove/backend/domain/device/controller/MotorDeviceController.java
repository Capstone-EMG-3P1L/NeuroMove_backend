package com.neuromove.backend.domain.device.controller;

import com.neuromove.backend.domain.auth.jwt.CustomUserPrincipal;
import com.neuromove.backend.domain.device.dto.response.MotorDeviceListResponse;
import com.neuromove.backend.domain.device.service.MotorDeviceService;
import com.neuromove.backend.domain.user.entity.User;
import com.neuromove.backend.domain.user.repository.UserRepository;
import com.neuromove.backend.global.api.ApiResponse;
import com.neuromove.backend.global.exception.CustomException;
import com.neuromove.backend.global.exception.ErrorCode;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/api/motor-devices")
@RequiredArgsConstructor
public class MotorDeviceController {

    private final MotorDeviceService motorDeviceService;
    private final UserRepository userRepository;

    @GetMapping
    public ApiResponse<MotorDeviceListResponse> getMyDevices(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        User user = userRepository.findByUsername(principal.username())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        MotorDeviceListResponse response = motorDeviceService.getMyDevices(user);
        return ApiResponse.success("MOTOR_DEVICE_LIST_FETCHED", "모터 디바이스 목록을 조회했습니다.", response);
    }
}