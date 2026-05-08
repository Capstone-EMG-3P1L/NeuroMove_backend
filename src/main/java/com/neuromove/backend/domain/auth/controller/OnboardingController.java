package com.neuromove.backend.domain.auth.controller;

import com.neuromove.backend.domain.auth.dto.request.RegisterRequest;
import com.neuromove.backend.domain.auth.dto.response.OnboardingCompleteResponse;
import com.neuromove.backend.domain.auth.dto.response.OnboardingStartResponse;
import com.neuromove.backend.domain.auth.service.AuthService;
import com.neuromove.backend.domain.auth.service.OnboardingService;
import com.neuromove.backend.domain.calibration.dto.request.CalibrationEndRequest;
import com.neuromove.backend.domain.calibration.dto.request.CalibrationStartRequest;
import com.neuromove.backend.domain.calibration.dto.request.CalibrationStepUpdateRequest;
import com.neuromove.backend.domain.calibration.dto.response.CalibrationEndResponse;
import com.neuromove.backend.domain.calibration.dto.response.CalibrationStartResponse;
import com.neuromove.backend.domain.calibration.dto.response.CalibrationStepUpdateResponse;
import com.neuromove.backend.domain.calibration.service.CalibrationService;
import com.neuromove.backend.domain.device.dto.request.EmgDeviceRegisterRequest;
import com.neuromove.backend.domain.device.dto.request.MotorDeviceRegisterRequest;
import com.neuromove.backend.domain.device.dto.response.EmgDeviceRegisterResponse;
import com.neuromove.backend.domain.device.dto.response.MotorDeviceRegisterResponse;
import com.neuromove.backend.domain.device.service.EmgDeviceService;
import com.neuromove.backend.domain.device.service.MotorDeviceService;
import com.neuromove.backend.global.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 온보딩 전용 컨트롤러
 * - 모든 endpoint는 /api/auth/onboarding/* 하위에 위치
 * - /api/auth/** 는 SecurityFilterConfig에서 이미 permitAll 처리되어 있음
 * - User가 아직 DB에 없는 상태에서 동작하므로 JWT 인증을 요구하지 않음
 * - 마지막 complete 단계까지 모든 단계는 Redis 임시 저장만 수행
 */
@Tag(name = "Onboarding", description = "회원가입 → 디바이스 등록 → Calibration → 일괄 저장 흐름")
@RestController
@RequestMapping("/api/auth/onboarding")
@RequiredArgsConstructor
public class OnboardingController {

    private final AuthService authService;
    private final EmgDeviceService emgDeviceService;
    private final MotorDeviceService motorDeviceService;
    private final CalibrationService calibrationService;
    private final OnboardingService onboardingService;

    @Operation(summary = "온보딩 시작", description = "회원 정보를 Redis에 임시 저장하고 onboardingId를 발급합니다.")
    @PostMapping("/start")
    public ApiResponse<OnboardingStartResponse> start(
            @Valid @RequestBody RegisterRequest request
    ) {
        OnboardingStartResponse response = authService.startOnboarding(request);
        return ApiResponse.success("ONBOARDING_STARTED", "온보딩이 시작되었습니다.", response);
    }

    @Operation(summary = "온보딩 EMG 디바이스 등록", description = "EMG 디바이스 정보를 Redis에 임시 저장합니다.")
    @PostMapping("/emg-devices")
    public ApiResponse<EmgDeviceRegisterResponse> registerEmgDevice(
            @Valid @RequestBody EmgDeviceRegisterRequest request
    ) {
        EmgDeviceRegisterResponse response = emgDeviceService.registerForOnboarding(request);
        return ApiResponse.success("ONBOARDING_EMG_DEVICE_REGISTERED", "EMG 디바이스가 임시 저장되었습니다.", response);
    }

    @Operation(summary = "온보딩 MOTOR 디바이스 등록", description = "MOTOR 디바이스 정보를 Redis에 임시 저장합니다.")
    @PostMapping("/motor-devices")
    public ApiResponse<MotorDeviceRegisterResponse> registerMotorDevice(
            @Valid @RequestBody MotorDeviceRegisterRequest request
    ) {
        MotorDeviceRegisterResponse response = motorDeviceService.registerForOnboarding(request);
        return ApiResponse.success("ONBOARDING_MOTOR_DEVICE_REGISTERED", "MOTOR 디바이스가 임시 저장되었습니다.", response);
    }

    @Operation(summary = "온보딩 Calibration 시작", description = "Calibration 세션을 시작하고 sessionId를 Redis에 저장합니다.")
    @PostMapping("/calibration/start")
    public ApiResponse<CalibrationStartResponse> startCalibration(
            @Valid @RequestBody CalibrationStartRequest request
    ) {
        CalibrationStartResponse response = calibrationService.startForOnboarding(request);
        return ApiResponse.success("ONBOARDING_CALIBRATION_STARTED", "Calibration이 시작되었습니다.", response);
    }

    @Operation(summary = "온보딩 Calibration 단계 변경", description = "Calibration 진행 중 다음 단계로 이동합니다 (REST → LEFT → RIGHT → STOP).")
    @org.springframework.web.bind.annotation.PatchMapping("/calibration")
    public ApiResponse<CalibrationStepUpdateResponse> updateCalibrationStep(
            @Valid @RequestBody CalibrationStepUpdateRequest request
    ) {
        CalibrationStepUpdateResponse response = calibrationService.updateStepForOnboarding(request);
        return ApiResponse.success("ONBOARDING_CALIBRATION_STEP_UPDATED", "Calibration 단계가 변경되었습니다.", response);
    }

    @Operation(summary = "온보딩 Calibration 종료", description = "Calibration 결과를 Redis에 임시 저장합니다.")
    @PostMapping("/calibration/end")
    public ApiResponse<CalibrationEndResponse> endCalibration(
            @Valid @RequestBody CalibrationEndRequest request
    ) {
        CalibrationEndResponse response = calibrationService.endForOnboarding(request);
        return ApiResponse.success("ONBOARDING_CALIBRATION_COMPLETED", "Calibration 결과가 임시 저장되었습니다.", response);
    }

    @Operation(summary = "온보딩 완료", description = "Redis에 저장된 모든 단계 정보를 DB에 일괄 저장하고 토큰을 발급합니다.")
    @PostMapping("/complete")
    public ApiResponse<OnboardingCompleteResponse> complete(
            @RequestBody CompleteRequest request
    ) {
        OnboardingCompleteResponse response = onboardingService.complete(request.onboardingId());
        return ApiResponse.success("ONBOARDING_COMPLETED", "회원가입이 완료되었습니다.", response);
    }

    /**
     * complete 요청 body (간단한 record)
     * - body 별도 DTO를 만들지 않고 컨트롤러 내부에 둠
     */
    public record CompleteRequest(String onboardingId) {}
}
