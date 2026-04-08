package com.neuromove.backend.domain.user.controller;

import com.neuromove.backend.domain.auth.jwt.CustomUserPrincipal;
import com.neuromove.backend.domain.user.dto.response.*;
import com.neuromove.backend.global.api.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/me")
    public ApiResponse<UserStatusResponse> getMyInfo(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        UserStatusResponse response = new UserStatusResponse(
                "user-001",
                principal.username(),
                "박수빈",
                new RegisteredEmgDeviceResponse(
                        "emg-esp32-A12F",
                        "내 EMG 보드",
                        true
                ),
                new RegisteredMotorDeviceResponse(
                        "motor-esp32-C01",
                        "RC카 1번 모터 보드",
                        true,
                        "CONNECTED"
                ),
                new ActiveCalibrationProfileResponse(
                        "profile-001",
                        0.93,
                        "2026-04-04T14:35:00"
                ),
                null
        );

        return ApiResponse.success("USER_FETCHED", "사용자 상태를 조회했습니다.", response);
    }

    @GetMapping("/me/logs")
    public ApiResponse<SessionLogsResponse> getMyLogs(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        SessionLogsResponse response = new SessionLogsResponse(
                List.of(
                        new SessionLogItemResponse(
                                "sess-001",
                                "emg-esp32-A12F",
                                "motor-esp32-C01",
                                "2026-04-03T10:00:00",
                                "2026-04-03T10:12:10",
                                730,
                                0.66,
                                "ENDED"
                        ),
                        new SessionLogItemResponse(
                                "sess-002",
                                "emg-esp32-A12F",
                                "motor-esp32-C01",
                                "2026-04-04T14:40:00",
                                "2026-04-04T14:48:20",
                                500,
                                0.78,
                                "ENDED"
                        )
                )
        );

        return ApiResponse.success("SESSION_LOGS_FETCHED", "운행 로그 목록을 조회했습니다.", response);
    }
}