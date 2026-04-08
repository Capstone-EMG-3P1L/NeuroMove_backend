package com.neuromove.backend.domain.user.controller;

import com.neuromove.backend.domain.auth.jwt.CustomUserPrincipal;
import com.neuromove.backend.domain.user.dto.response.SessionLogsResponse;
import com.neuromove.backend.domain.user.dto.response.UserStatusResponse;
import com.neuromove.backend.domain.user.service.UserService;
import com.neuromove.backend.global.api.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SecurityRequirement(name = "BearerAuth")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ApiResponse<UserStatusResponse> getMyInfo(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        UserStatusResponse response = userService.getMyInfo(principal.username());
        return ApiResponse.success("USER_FETCHED", "사용자 상태를 조회했습니다.", response);
    }

    @GetMapping("/me/logs")
    public ApiResponse<SessionLogsResponse> getMyLogs(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        SessionLogsResponse response = userService.getMyLogs(principal.username());
        return ApiResponse.success("SESSION_LOGS_FETCHED", "운행 로그 목록을 조회했습니다.", response);
    }
}