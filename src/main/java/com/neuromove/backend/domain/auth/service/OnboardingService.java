package com.neuromove.backend.domain.auth.service;

import com.neuromove.backend.domain.auth.dto.response.LoginUserResponse;
import com.neuromove.backend.domain.auth.dto.response.OnboardingCompleteResponse;
import com.neuromove.backend.domain.auth.jwt.JwtTokenProvider;
import com.neuromove.backend.domain.calibration.entity.CalibrationProfile;
import com.neuromove.backend.domain.calibration.entity.CalibrationSession;
import com.neuromove.backend.domain.calibration.repository.CalibrationProfileRepository;
import com.neuromove.backend.domain.calibration.repository.CalibrationSessionRepository;
import com.neuromove.backend.domain.device.entity.EmgDevice;
import com.neuromove.backend.domain.device.entity.MotorDevice;
import com.neuromove.backend.domain.device.entity.enums.ConnectionStatus;
import com.neuromove.backend.domain.device.repository.EmgDeviceRepository;
import com.neuromove.backend.domain.device.repository.MotorDeviceRepository;
import com.neuromove.backend.domain.user.entity.User;
import com.neuromove.backend.domain.user.repository.UserRepository;
import com.neuromove.backend.global.exception.CustomException;
import com.neuromove.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 온보딩 최종 완료 서비스
 * - 모든 단계의 Redis 임시 데이터를 DB Entity로 일괄 변환하여 저장
 * - 한 트랜잭션으로 묶여 있어 중간 실패 시 전체 롤백
 * - DB commit 성공 후 afterCommit 훅에서 Redis 키들을 정리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OnboardingService {

    private final OnboardingRedisService onboardingRedisService;

    private final UserRepository userRepository;
    private final EmgDeviceRepository emgDeviceRepository;
    private final MotorDeviceRepository motorDeviceRepository;
    private final CalibrationSessionRepository calibrationSessionRepository;
    private final CalibrationProfileRepository calibrationProfileRepository;

    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public OnboardingCompleteResponse complete(String onboardingId) {

        // 1. Redis에서 단계별 데이터 모두 조회
        //    각 단계 데이터가 없으면 알맞은 ONBOARDING_*_NOT_FOUND 예외가 던져짐
        OnboardingRedisService.RegisterData registerData =
                onboardingRedisService.getRegisterData(onboardingId);
        OnboardingRedisService.DeviceData emgDeviceData =
                onboardingRedisService.getEmgDevice(onboardingId);
        OnboardingRedisService.DeviceData motorDeviceData =
                onboardingRedisService.getMotorDevice(onboardingId);
        OnboardingRedisService.CalibrationData calibrationData =
                onboardingRedisService.getCalibrationData(onboardingId);

        // 2. username 중복 재검사
        //    startOnboarding 시점 이후 다른 사용자가 같은 username을 가져갔을 수 있음
        if (userRepository.existsByUsername(registerData.username())) {
            throw new CustomException(ErrorCode.DUPLICATE_USERNAME);
        }

        // 3. User 저장
        User user;
        try {
            user = userRepository.save(User.builder()
                    .username(registerData.username())
                    .password(passwordEncoder.encode(registerData.password()))
                    .name(registerData.name())
                    .build());
        } catch (DataIntegrityViolationException e) {
            // DB unique 제약(동시성) 방어
            throw new CustomException(ErrorCode.DUPLICATE_USERNAME);
        }

        // 4. EmgDevice 저장
        EmgDevice emgDevice;
        try {
            emgDevice = emgDeviceRepository.save(EmgDevice.builder()
                    .emgDeviceId(emgDeviceData.deviceId())
                    .user(user)
                    .name(emgDeviceData.name())
                    .isActive(true)
                    .build());
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(ErrorCode.EMG_DEVICE_ALREADY_REGISTERED);
        }

        // 5. MotorDevice 저장
        try {
            motorDeviceRepository.save(MotorDevice.builder()
                    .motorDeviceId(motorDeviceData.deviceId())
                    .user(user)
                    .name(motorDeviceData.name())
                    .isActive(true)
                    .connectionStatus(ConnectionStatus.CONNECTED)
                    .build());
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(ErrorCode.MOTOR_DEVICE_ALREADY_REGISTERED);
        }

        // 6. CalibrationSession 저장 (이미 완료 상태)
        //    온보딩 중 startForOnboarding에서 만든 sessionId를 그대로 재사용
        //    @PrePersist는 calibrationSessionId가 null일 때만 UUID 생성하므로 안전
        CalibrationSession session = CalibrationSession.builder()
                .calibrationSessionId(calibrationData.calibrationSessionId())
                .user(user)
                .emgDevice(emgDevice)
                .build();
        CalibrationSession savedSession = calibrationSessionRepository.save(session);

        // PrePersist로 status=IN_PROGRESS, currentStep=REST가 세팅된 직후
        // complete()를 호출해 COMPLETED 상태로 전환
        savedSession.complete(calibrationData.signalQuality());

        // 7. CalibrationProfile 저장 (활성 프로필)
        calibrationProfileRepository.save(CalibrationProfile.builder()
                .user(user)
                .calibrationSessionId(savedSession.getCalibrationSessionId())
                .ch1Mean(calibrationData.ch1Mean())
                .ch1Std(calibrationData.ch1Std())
                .ch2Mean(calibrationData.ch2Mean())
                .ch2Std(calibrationData.ch2Std())
                .ch3Mean(calibrationData.ch3Mean())
                .ch3Std(calibrationData.ch3Std())
                .activationThreshold(calibrationData.activationThreshold())
                .intentThresholdLeft(calibrationData.intentThresholdLeft())
                .intentThresholdRight(calibrationData.intentThresholdRight())
                .intentThresholdForward(calibrationData.intentThresholdForward())
                .fatigueBaseline(calibrationData.fatigueBaseline())
                .signalQuality(calibrationData.signalQuality())
                .isActive(true)
                .build());

        // 8. 토큰 발급 + Redis에 refresh token 저장
        String accessToken = jwtTokenProvider.createAccessToken(
                user.getUserId(), user.getUsername()
        );
        String refreshToken = jwtTokenProvider.createRefreshToken(
                user.getUserId(), user.getUsername()
        );
        refreshTokenService.save(user.getUserId(), refreshToken);

        // 9. afterCommit에서 온보딩 Redis 키 일괄 정리
        //    DB commit 실패 시에는 Redis 데이터를 살려둬야 사용자가 재시도 가능
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    onboardingRedisService.purge(onboardingId);
                } catch (Exception e) {
                    // 정리 실패해도 TTL이 결국 자동 정리하므로 로그만
                    log.warn("Failed to purge onboarding redis keys. onboardingId={}",
                            onboardingId, e);
                }
            }
        });

        // 10. 응답
        return OnboardingCompleteResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .user(LoginUserResponse.from(user))
                .build();
    }
}
