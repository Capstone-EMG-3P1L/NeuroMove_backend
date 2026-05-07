package com.neuromove.backend.domain.auth.service;

import com.neuromove.backend.domain.auth.dto.request.RegisterRequest;
import com.neuromove.backend.domain.calibration.dto.request.CalibrationEndRequest;
import com.neuromove.backend.global.exception.CustomException;
import com.neuromove.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OnboardingRedisService {

    private static final String PREFIX = "onboarding:";
    private static final Duration TTL = Duration.ofMinutes(30);

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 회원가입 정보 Redis 임시 저장
     */
    public String saveRegisterData(RegisterRequest request) {
        String onboardingId = UUID.randomUUID().toString();

        Map<String, String> values = new HashMap<>();
        values.put("username", request.getUsername());
        values.put("password", request.getPassword());
        values.put("name", request.getName());

        saveHash(registerKey(onboardingId), values);

        return onboardingId;
    }

    /**
     * Calibration 세션 정보 저장
     */
    public void saveCalibrationSession(
            String onboardingId,
            String calibrationSessionId
    ) {
        validateOnboardingExists(onboardingId);

        Map<String, String> values = new HashMap<>();
        values.put("calibrationSessionId", calibrationSessionId);

        saveHash(calibrationKey(onboardingId), values);
    }

    /**
     * Calibration 결과 저장
     */
    public void saveCalibrationResult(CalibrationEndRequest request) {
        validateOnboardingExists(request.getOnboardingId());

        Map<String, String> values = new HashMap<>();

        put(values, "calibrationSessionId", request.getCalibrationSessionId());

        put(values, "ch1Mean", request.getCh1Mean());
        put(values, "ch1Std", request.getCh1Std());

        put(values, "ch2Mean", request.getCh2Mean());
        put(values, "ch2Std", request.getCh2Std());

        put(values, "ch3Mean", request.getCh3Mean());
        put(values, "ch3Std", request.getCh3Std());

        put(values, "activationThreshold", request.getActivationThreshold());

        put(values, "intentThresholdLeft", request.getIntentThresholdLeft());
        put(values, "intentThresholdRight", request.getIntentThresholdRight());
        put(values, "intentThresholdForward", request.getIntentThresholdForward());

        put(values, "fatigueBaseline", request.getFatigueBaseline());

        put(values, "signalQuality", request.getSignalQuality());

        saveHash(calibrationKey(request.getOnboardingId()), values);
    }

    /**
     * EMG 디바이스 정보 저장
     */
    public void saveEmgDevice(
            String onboardingId,
            String emgDeviceId,
            String name
    ) {
        validateOnboardingExists(onboardingId);

        Map<String, String> values = new HashMap<>();
        values.put("deviceId", emgDeviceId);
        values.put("name", name);

        saveHash(emgDeviceKey(onboardingId), values);
    }

    /**
     * MOTOR 디바이스 정보 저장
     */
    public void saveMotorDevice(
            String onboardingId,
            String motorDeviceId,
            String name
    ) {
        validateOnboardingExists(onboardingId);

        Map<String, String> values = new HashMap<>();
        values.put("deviceId", motorDeviceId);
        values.put("name", name);

        saveHash(motorDeviceKey(onboardingId), values);
    }

    private void validateOnboardingExists(String onboardingId) {
        if (onboardingId == null
                || onboardingId.isBlank()
                || !Boolean.TRUE.equals(
                stringRedisTemplate.hasKey(registerKey(onboardingId))
        )) {
            throw new CustomException(ErrorCode.ONBOARDING_NOT_FOUND);
        }
    }

    private void saveHash(String key, Map<String, String> values) {
        stringRedisTemplate.opsForHash().putAll(key, values);
        stringRedisTemplate.expire(key, TTL);
    }

    private void put(
            Map<String, String> values,
            String key,
            Object value
    ) {
        if (value != null) {
            values.put(key, value.toString());
        }
    }

    private String registerKey(String onboardingId) {
        return PREFIX + onboardingId + ":register";
    }

    private String calibrationKey(String onboardingId) {
        return PREFIX + onboardingId + ":calibration";
    }

    private String emgDeviceKey(String onboardingId) {
        return PREFIX + onboardingId + ":emg-device";
    }

    private String motorDeviceKey(String onboardingId) {
        return PREFIX + onboardingId + ":motor-device";
    }
}