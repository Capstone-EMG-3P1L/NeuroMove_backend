package com.neuromove.backend.domain.session.controller;

import com.neuromove.backend.domain.auth.jwt.CustomUserPrincipal;
import com.neuromove.backend.domain.session.dto.request.SessionStartRequest;
import com.neuromove.backend.domain.session.dto.response.SessionStartResponse;
import com.neuromove.backend.domain.session.service.SessionService;
import com.neuromove.backend.domain.user.entity.User;
import com.neuromove.backend.domain.user.repository.UserRepository;
import com.neuromove.backend.global.api.ApiResponse;
import com.neuromove.backend.global.exception.CustomException;
import com.neuromove.backend.global.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.neuromove.backend.domain.session.dto.response.SessionDetailResponse;

@Tag(name = "Session", description = "주행 세션 관리")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;
    private final UserRepository userRepository;

    @Operation(summary = "주행 세션 시작", description = "운행 시작 버튼 클릭 시 주행 세션을 시작합니다.")
    @PostMapping
    public ApiResponse<SessionStartResponse> start(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody SessionStartRequest request
    ) {
        User user = userRepository.findByUsername(principal.username())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        SessionStartResponse response = sessionService.start(user, request);
        return ApiResponse.success("SESSION_CREATED", "주행 세션이 시작되었습니다.", response);
    }

    @GetMapping("/{sessionId}/detail")
    public ApiResponse<SessionDetailResponse> getSessionDetail(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable String sessionId
    ) {
        SessionDetailResponse response = sessionService.getSessionDetail(
                principal.username(),
                sessionId
        );

        return ApiResponse.success("SESSION_DETAIL_FETCHED", "운행 로그 상세를 조회했습니다.", response);
    }
}
