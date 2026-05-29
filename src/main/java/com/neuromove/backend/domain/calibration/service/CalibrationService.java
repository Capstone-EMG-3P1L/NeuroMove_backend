package com.neuromove.backend.domain.calibration.service;

import com.neuromove.backend.domain.auth.service.OnboardingRedisService;
import com.neuromove.backend.domain.calibration.dto.request.CalibrationEndRequest;
import com.neuromove.backend.domain.calibration.dto.request.CalibrationStartRequest;
import com.neuromove.backend.domain.calibration.dto.request.CalibrationStepUpdateRequest;
import com.neuromove.backend.domain.calibration.dto.response.*;
import com.neuromove.backend.domain.calibration.entity.CalibrationProfile;
import com.neuromove.backend.domain.calibration.entity.CalibrationSession;
import com.neuromove.backend.domain.calibration.entity.enums.CalibrationStatus;
import com.neuromove.backend.domain.calibration.entity.enums.CalibrationStep;
import com.neuromove.backend.domain.calibration.repository.CalibrationProfileRepository;
import com.neuromove.backend.domain.calibration.repository.CalibrationSessionRepository;
import com.neuromove.backend.domain.device.entity.EmgDevice;
import com.neuromove.backend.domain.device.repository.EmgDeviceRepository;
import com.neuromove.backend.domain.user.entity.User;
import com.neuromove.backend.global.exception.CustomException;
import com.neuromove.backend.global.exception.ErrorCode;
import com.neuromove.backend.infrastructure.ai.calibration.AiCalibrationClient;
import com.neuromove.backend.infrastructure.ai.calibration.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CalibrationService {

    private final CalibrationSessionRepository calibrationSessionRepository;
    private final CalibrationProfileRepository calibrationProfileRepository;
    private final EmgDeviceRepository emgDeviceRepository;
    private final AiCalibrationClient aiCalibrationClient;
    private final OnboardingRedisService onboardingRedisService;

    @Transactional
    public CalibrationStartResponse start(User user, CalibrationStartRequest request) {
        EmgDevice emgDevice = emgDeviceRepository.findById(request.getEmgDeviceId())
                .orElseThrow(() -> new CustomException(ErrorCode.EMG_DEVICE_NOT_FOUND));

        CalibrationSession session = CalibrationSession.builder()
                .user(user)
                .emgDevice(emgDevice)
                .build();

        CalibrationSession saved = calibrationSessionRepository.save(session);

        // AI 서버에 캘리브레이션 시작 요청
        aiCalibrationClient.start(AiCalibrationStartRequest.builder()
                .calibrationSessionId(saved.getCalibrationSessionId())
                .userId(user.getUserId())
                .deviceId(emgDevice.getEmgDeviceId())
                .initialStep(CalibrationStep.REST.name())
                .build());

        return CalibrationStartResponse.from(saved);
    }

    @Transactional
    public CalibrationStepUpdateResponse updateStep(User user, CalibrationStepUpdateRequest request) {
        CalibrationSession session = calibrationSessionRepository.findById(request.getCalibrationSessionId())
                .orElseThrow(() -> new CustomException(ErrorCode.CALIBRATION_SESSION_NOT_FOUND));

        validateOwner(user, session);

        if (session.getStatus() == CalibrationStatus.COMPLETED) {
            throw new CustomException(ErrorCode.CALIBRATION_ALREADY_COMPLETED);
        }

        session.proceedToStep(request.getStep());

        // AI 서버에 캘리브레이션 단계 변경 요청
        aiCalibrationClient.updateStep(AiCalibrationStepRequest.builder()
                .calibrationSessionId(session.getCalibrationSessionId())
                .step(request.getStep().name())
                .build());

        return CalibrationStepUpdateResponse.from(session);
    }

@Transactional
    public CalibrationEndResponse end(User user, CalibrationEndRequest request) {
        CalibrationSession session = calibrationSessionRepository.findById(request.getCalibrationSessionId())
                .orElseThrow(() -> new CustomException(ErrorCode.CALIBRATION_SESSION_NOT_FOUND));

        validateOwner(user, session);

        if (session.getStatus() == CalibrationStatus.COMPLETED) {
            throw new CustomException(ErrorCode.CALIBRATION_ALREADY_COMPLETED);
        }

        // AI 서버에 캘리브레이션 종료 요청
        AiCalibrationFinishResponse aiResponse = aiCalibrationClient.finish(
                AiCalibrationFinishRequest.builder()
                        .calibrationSessionId(session.getCalibrationSessionId())
                        .build()
        );
        AiCalibrationFinishResponse.Result result = aiResponse.getData().getResult();
        AiCalibrationFinishResponse.Baseline baseline = result.getBaseline();
        Map<String, Float> intentThresholds = result.getIntentThresholds();
        session.complete(result.getSignalQuality());

        calibrationProfileRepository.findFirstByUserAndIsActiveTrueOrderByCreatedAtDesc(user)
                .ifPresent(CalibrationProfile::deactivate);

        CalibrationProfile profile = CalibrationProfile.builder()
                .user(user)
                .calibrationSessionId(session.getCalibrationSessionId())
                .ch1Mean(request.getCh1Mean())
                .ch1Std(request.getCh1Std())
                .ch2Mean(request.getCh2Mean())
                .ch2Std(request.getCh2Std())
                .ch3Mean(request.getCh3Mean())
                .ch3Std(request.getCh3Std())
                .activationThreshold(request.getActivationThreshold())
                .intentThresholdRest(request.getIntentThresholdRest())
                .intentThresholdLeft(request.getIntentThresholdLeft())
                .intentThresholdRight(request.getIntentThresholdRight())
                .intentThresholdStop(request.getIntentThresholdStop())
                .fatigueBaseline(request.getFatigueBaseline())
                .signalQuality(request.getSignalQuality())
                .isActive(true)
                .build();

        CalibrationProfile saved = calibrationProfileRepository.save(profile);
        return CalibrationEndResponse.from(saved);
    }

    private void validateOwner(User user, CalibrationSession session) {
        if (!session.getUser().getUserId().equals(user.getUserId())) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
    }

    /**
     * 온보딩 모드 Calibration 시작
     * - User / EmgDevice가 아직 DB에 없음 → DB 조회 안 함
     * - EmgDevice가 Redis에 등록돼 있는지만 확인
     * - calibrationSessionId는 직접 UUID 생성 (DB 시퀀스 못 씀)
     * - 어떤 Entity도 만들지 않고 sessionId만 Redis에 표시
     *
     * 트랜잭션을 안 붙이는 이유: DB를 전혀 건드리지 않음
     */
    public CalibrationStartResponse startForOnboarding(CalibrationStartRequest request) {
        // EMG 디바이스가 Redis에 등록돼 있는지 확인 (없으면 ONBOARDING_EMG_DEVICE_NOT_FOUND)
        OnboardingRedisService.DeviceData emgDevice =
                onboardingRedisService.getEmgDevice(request.getOnboardingId());

        // sessionId를 직접 UUID로 생성
        // CalibrationSession entity의 PrePersist도 UUID 생성을 사용하므로 호환됨
        String sessionId = UUID.randomUUID().toString();

        // Redis에 sessionId 저장 (calibration 진행 중임을 표시)
        onboardingRedisService.saveCalibrationSession(request.getOnboardingId(), sessionId);

        // AI 서버에 캘리브레이션 시작 요청 (온보딩)
        aiCalibrationClient.start(AiCalibrationStartRequest.builder()
                .calibrationSessionId(sessionId)
                .userId(null)  // 온보딩 중이라 아직 User 없음
                .deviceId(emgDevice.deviceId())
                .initialStep(CalibrationStep.REST.name())
                .build());

        return CalibrationStartResponse.ofOnboarding(sessionId, emgDevice.deviceId());
    }

    /**
     * 온보딩 모드 Calibration 단계 변경
     * - DB Session이 없으므로 Redis에 저장된 currentStep을 갱신
     * - 기존 updateStep과 달리 status/completedSteps 추적은 하지 않음
     *   (complete 단계에서 어차피 COMPLETED 상태로 한 번에 저장)
     * - AI 서버 연동은 기존 updateStep과 동일하게 추가 가능 (TODO)
     */
    public CalibrationStepUpdateResponse updateStepForOnboarding(CalibrationStepUpdateRequest request) {
        // Redis의 calibration hash가 존재해야 (start이 호출됐어야) updateStep 가능
        // updateCalibrationStep 내부에서 onboardingId 검증도 함께 수행됨
        onboardingRedisService.updateCalibrationStep(
                request.getOnboardingId(),
                request.getStep().name()
        );

        // AI 서버에 캘리브레이션 단계 변경 요청 (온보딩)
        aiCalibrationClient.updateStep(AiCalibrationStepRequest.builder()
                .calibrationSessionId(request.getCalibrationSessionId())
                .step(request.getStep().name())
                .build());

        return CalibrationStepUpdateResponse.ofOnboarding(
                request.getCalibrationSessionId(),
                request.getStep()
        );
    }

    /**
     * 온보딩 모드 Calibration 종료
     * - DB Entity(Session/Profile) 만들지 않음
     * - 결과(threshold, baseline 등)를 Redis에 저장만
     * - complete 단계에서 한 번에 Entity로 변환해 DB 저장
     */
    public CalibrationEndResponse endForOnboarding(CalibrationEndRequest request) {
        // AI 서버에 캘리브레이션 종료 요청 (온보딩)
        aiCalibrationClient.finish(AiCalibrationFinishRequest.builder()
                .calibrationSessionId(request.getCalibrationSessionId())
                .build());

        // 결과를 Redis에 저장 (실내부에서 onboardingId 검증도 함께 수행됨)
        onboardingRedisService.saveCalibrationResult(request);

        return CalibrationEndResponse.ofOnboarding(
                request.getCalibrationSessionId(),
                request.getSignalQuality()
        );
    }

    public CalibrationProfileResponse getProfile(User user) {
        CalibrationProfile profile = calibrationProfileRepository.findFirstByUserAndIsActiveTrueOrderByCreatedAtDesc(user)
                .orElseThrow(() -> new CustomException(ErrorCode.CALIBRATION_PROFILE_NOT_FOUND));

        return CalibrationProfileResponse.from(profile);
    }
}
