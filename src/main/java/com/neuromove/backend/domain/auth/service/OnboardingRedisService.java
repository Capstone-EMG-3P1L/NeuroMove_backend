package com.neuromove.backend.domain.auth.service;

import com.neuromove.backend.domain.auth.dto.request.RegisterRequest;
import com.neuromove.backend.domain.calibration.dto.request.CalibrationEndRequest;
import com.neuromove.backend.global.exception.CustomException;
import com.neuromove.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
     * - password는 호출하는 쪽(AuthService)에서 이미 인코딩한 값을 받음
     * - Redis에는 절대 평문 비밀번호가 저장되지 않도록 보장
     */
    public String saveRegisterData(RegisterRequest request, String encodedPassword) {
        String onboardingId = UUID.randomUUID().toString();

        Map<String, String> values = new HashMap<>();
        values.put("username", request.getUsername());
        values.put("password", encodedPassword);
        values.put("name", request.getName());

        saveHash(registerKey(onboardingId), values);

        return onboardingId;
    }

    /**
     * Calibration 세션 정보 저장 (start 시점)
     * - sessionId와 함께 초기 단계(REST)를 같이 저장
     */
    public void saveCalibrationSession(
            String onboardingId,
            String calibrationSessionId
    ) {
        validateOnboardingExists(onboardingId);

        Map<String, String> values = new HashMap<>();
        values.put("calibrationSessionId", calibrationSessionId);
        // 초기 단계는 REST로 시작 (CalibrationSession entity의 PrePersist와 동일)
        values.put("currentStep", "REST");

        saveHash(calibrationKey(onboardingId), values);
    }

    /**
     * Calibration 단계 업데이트
     * - updateStep 호출 시 currentStep만 갱신
     * - completedSteps는 굳이 별도 추적하지 않음 (complete에서 어차피 모든 단계 통과로 간주)
     * - /start 호출 전 /step이 호출되면 partial state가 생기므로 calibration key 존재 여부 검증
     */
    public void updateCalibrationStep(
            String onboardingId,
            String currentStep
    ) {
        validateOnboardingExists(onboardingId);

        // /calibration/start 가 먼저 호출됐는지 확인
        if (!Boolean.TRUE.equals(stringRedisTemplate.hasKey(calibrationKey(onboardingId)))) {
            throw new CustomException(ErrorCode.ONBOARDING_CALIBRATION_NOT_FOUND);
        }

        // 기존 hash 유지하면서 currentStep만 업데이트
        stringRedisTemplate.opsForHash().put(
                calibrationKey(onboardingId),
                "currentStep",
                currentStep
        );
        // TTL 갱신
        stringRedisTemplate.expire(calibrationKey(onboardingId), TTL);
    }

    /**
     * Calibration 결과 저장
     * - /start 호출 후에만 호출 가능
     * - phase="ENDED" 마커를 함께 저장하여 complete 단계에서 "/end가 정상 호출되었는지" 검증
     */
    public void saveCalibrationResult(CalibrationEndRequest request) {
        validateOnboardingExists(request.getOnboardingId());

        // /calibration/start 가 먼저 호출됐는지 확인
        if (!Boolean.TRUE.equals(stringRedisTemplate.hasKey(calibrationKey(request.getOnboardingId())))) {
            throw new CustomException(ErrorCode.ONBOARDING_CALIBRATION_NOT_FOUND);
        }

        Map<String, String> values = new HashMap<>();

        put(values, "calibrationSessionId", request.getCalibrationSessionId());

        put(values, "ch1Mean", request.getCh1Mean());
        put(values, "ch1Std", request.getCh1Std());

        put(values, "ch2Mean", request.getCh2Mean());
        put(values, "ch2Std", request.getCh2Std());

        put(values, "ch3Mean", request.getCh3Mean());
        put(values, "ch3Std", request.getCh3Std());

        put(values, "activationThreshold", request.getActivationThreshold());

        put(values, "intentThresholdRest", request.getIntentThresholdRest());
        put(values, "intentThresholdLeft", request.getIntentThresholdLeft());
        put(values, "intentThresholdRight", request.getIntentThresholdRight());
        put(values, "intentThresholdStop", request.getIntentThresholdStop());

        put(values, "fatigueBaseline", request.getFatigueBaseline());

        put(values, "signalQuality", request.getSignalQuality());

        // /end 호출 완료 마커
        values.put("phase", "ENDED");

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

    // =========================================================
    // 조회 메서드
    // - complete() 단계에서 Redis 데이터를 Entity로 변환할 때 사용
    // =========================================================

    /**
     * 회원가입 정보 조회
     * - register 단계에서 저장한 username/password/name 반환
     * - 정보가 없으면 ONBOARDING_REGISTER_NOT_FOUND
     */
    public RegisterData getRegisterData(String onboardingId) {
        Map<String, String> data = readHash(registerKey(onboardingId));
        if (data.isEmpty()) {
            throw new CustomException(ErrorCode.ONBOARDING_REGISTER_NOT_FOUND);
        }
        return new RegisterData(
                data.get("username"),
                data.get("password"),
                data.get("name")
        );
    }

    /**
     * EMG 디바이스 정보 조회
     */
    public DeviceData getEmgDevice(String onboardingId) {
        Map<String, String> data = readHash(emgDeviceKey(onboardingId));
        if (data.isEmpty()) {
            throw new CustomException(ErrorCode.ONBOARDING_EMG_DEVICE_NOT_FOUND);
        }
        return new DeviceData(data.get("deviceId"), data.get("name"));
    }

    /**
     * MOTOR 디바이스 정보 조회
     */
    public DeviceData getMotorDevice(String onboardingId) {
        Map<String, String> data = readHash(motorDeviceKey(onboardingId));
        if (data.isEmpty()) {
            throw new CustomException(ErrorCode.ONBOARDING_MOTOR_DEVICE_NOT_FOUND);
        }
        return new DeviceData(data.get("deviceId"), data.get("name"));
    }

    /**
     * Calibration 결과 조회
     * - end 단계에서 저장한 결과(threshold, baseline 등) 반환
     * - phase=="ENDED" 마커가 없으면 /end가 호출되지 않은 상태이므로 거부
     *   (그렇지 않으면 /start만 부르고 /complete를 호출했을 때 빈 profile이 저장됨)
     */
    public CalibrationData getCalibrationData(String onboardingId) {
        Map<String, String> data = readHash(calibrationKey(onboardingId));
        if (data.isEmpty() || !"ENDED".equals(data.get("phase"))) {
            throw new CustomException(ErrorCode.ONBOARDING_CALIBRATION_NOT_FOUND);
        }
        return new CalibrationData(
                data.get("calibrationSessionId"),
                parseFloat(data.get("ch1Mean")),
                parseFloat(data.get("ch1Std")),
                parseFloat(data.get("ch2Mean")),
                parseFloat(data.get("ch2Std")),
                parseFloat(data.get("ch3Mean")),
                parseFloat(data.get("ch3Std")),
                parseFloat(data.get("activationThreshold")),
                parseFloat(data.get("intentThresholdRest")),
                parseFloat(data.get("intentThresholdLeft")),
                parseFloat(data.get("intentThresholdRight")),
                parseFloat(data.get("intentThresholdStop")),
                parseFloat(data.get("fatigueBaseline")),
                parseFloat(data.get("signalQuality"))
        );
    }

    /**
     * 온보딩 관련 모든 키 삭제
     * - complete() 트랜잭션 commit 성공 후 호출
     * - 실패해도 TTL이 자동 정리하므로 치명적이지 않음
     */
    public void purge(String onboardingId) {
        List<String> keys = Arrays.asList(
                registerKey(onboardingId),
                emgDeviceKey(onboardingId),
                motorDeviceKey(onboardingId),
                calibrationKey(onboardingId)
        );
        stringRedisTemplate.delete(keys);
    }

    // =========================================================
    // 내부 유틸
    // =========================================================

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

    private Map<String, String> readHash(String key) {
        Map<Object, Object> raw = stringRedisTemplate.opsForHash().entries(key);
        Map<String, String> result = new HashMap<>();
        raw.forEach((k, v) -> result.put(k.toString(), v.toString()));
        return result;
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

    private Float parseFloat(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            return null;
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

    // =========================================================
    // 데이터 전달용 record
    // - complete() 단계에서 Entity 변환에 사용
    // =========================================================

    public record RegisterData(
            String username,
            String password,
            String name
    ) {}

    public record DeviceData(
            String deviceId,
            String name
    ) {}

    public record CalibrationData(
            String calibrationSessionId,
            Float ch1Mean,
            Float ch1Std,
            Float ch2Mean,
            Float ch2Std,
            Float ch3Mean,
            Float ch3Std,
            Float activationThreshold,
            Float intentThresholdRest,
            Float intentThresholdLeft,
            Float intentThresholdRight,
            Float intentThresholdStop,
            Float fatigueBaseline,
            Float signalQuality
    ) {}
}