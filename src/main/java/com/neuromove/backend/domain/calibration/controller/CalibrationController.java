package com.neuromove.backend.domain.calibration.controller;

import com.neuromove.backend.domain.auth.jwt.CustomUserPrincipal;
import com.neuromove.backend.domain.calibration.dto.request.CalibrationEndRequest;
import com.neuromove.backend.domain.calibration.dto.request.CalibrationStartRequest;
import com.neuromove.backend.domain.calibration.dto.request.CalibrationStepUpdateRequest;
import com.neuromove.backend.domain.calibration.dto.response.*;
import com.neuromove.backend.domain.calibration.service.CalibrationService;
import com.neuromove.backend.domain.user.entity.User;
import com.neuromove.backend.domain.user.repository.UserRepository;
import com.neuromove.backend.global.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Calibration", description = "Calibration 세션 및 프로파일 관리")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/api/calibration")
@RequiredArgsConstructor
public class CalibrationController {

    private final CalibrationService calibrationService;
    private final UserRepository userRepository;

    @Operation(summary = "Calibration 시작", description = "선택한 EMG 디바이스 기준으로 Calibration 세션을 시작합니다.")
    @PostMapping
    public ApiResponse<CalibrationStartResponse> start(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody CalibrationStartRequest request
    ) {
        User user = getUser(principal);
        CalibrationStartResponse response = calibrationService.start(user, request);
        return ApiResponse.success("CALIBRATION_STARTED", "Calibration이 시작되었습니다.", response);
    }

    @Operation(summary = "Calibration 단계 변경", description = "각 단계 완료 후 다음 단계로 진행합니다.")
    @PatchMapping
    public ApiResponse<CalibrationStepUpdateResponse> updateStep(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody CalibrationStepUpdateRequest request
    ) {
        User user = getUser(principal);
        CalibrationStepUpdateResponse response = calibrationService.updateStep(user, request);
        return ApiResponse.success("CALIBRATION_STEP_UPDATED", "Calibration 단계가 변경되었습니다.", response);
    }

@Operation(summary = "Calibration 종료", description = "마지막 단계 완료 후 Calibration을 종료하고 프로파일을 생성합니다.")
    @PostMapping("/end")
    public ApiResponse<CalibrationEndResponse> end(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody CalibrationEndRequest request
    ) {
        User user = getUser(principal);
        CalibrationEndResponse response = calibrationService.end(user, request);
        return ApiResponse.success("CALIBRATION_COMPLETED", "Calibration이 완료되었습니다.", response);
    }

    @Operation(summary = "Calibration 프로파일 조회", description = "사용자의 활성 Calibration 프로파일을 조회합니다.")
    @GetMapping("/profile")
    public ApiResponse<CalibrationProfileResponse> getProfile(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        User user = getUser(principal);
        CalibrationProfileResponse response = calibrationService.getProfile(user);
        return ApiResponse.success("CALIBRATION_PROFILE_FETCHED", "Calibration 프로파일을 조회했습니다.", response);
    }

    private User getUser(CustomUserPrincipal principal) {
        return userRepository.findByUsername(principal.username())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }
}
