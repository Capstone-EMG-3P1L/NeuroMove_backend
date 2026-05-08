package com.neuromove.backend.domain.auth.controller;

import com.neuromove.backend.domain.auth.dto.request.LoginRequest;
import com.neuromove.backend.domain.auth.dto.response.LoginResponse;
import com.neuromove.backend.domain.auth.dto.request.RefreshTokenRequest;
import com.neuromove.backend.domain.auth.dto.response.TokenRefreshResponse;
import com.neuromove.backend.domain.auth.jwt.CustomUserPrincipal;
import com.neuromove.backend.global.exception.CustomException;
import com.neuromove.backend.global.exception.ErrorCode;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.neuromove.backend.domain.auth.service.AuthService;
import com.neuromove.backend.global.api.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ApiResponse.success("LOGIN_SUCCESS", "로그인에 성공했습니다.", response);
    }

    @PostMapping("/refresh")
    public ApiResponse<TokenRefreshResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request
    ) {

        TokenRefreshResponse response = authService.refresh(request);

        return ApiResponse.success(
                "TOKEN_REFRESH_SUCCESS",
                "토큰 재발급에 성공했습니다.",
                response
        );
    }

    @PostMapping("/logout")
    @SecurityRequirement(name = "BearerAuth")
    public ApiResponse<Void> logout(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        if (principal == null) {
            throw new CustomException(ErrorCode.AUTHENTICATION_FAILED);
        }

        authService.logout(principal.userId());

        return ApiResponse.success(
                "LOGOUT_SUCCESS",
                "로그아웃이 완료되었습니다.",
                null
        );
    }

}