package com.neuromove.backend.domain.device.service;

import com.neuromove.backend.domain.device.dto.request.EmgDeviceInfoRequest;
import com.neuromove.backend.domain.device.dto.request.MotorDeviceInfoRequest;
import com.neuromove.backend.domain.device.dto.response.EmgDeviceInfoResponse;
import com.neuromove.backend.domain.device.dto.response.MotorDeviceInfoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceInfoService {

    private static final String EMG_DEVICE_INFO_PREFIX = "emg-device-info:";
    private static final String MOTOR_DEVICE_INFO_PREFIX = "motor-device-info:";
    private static final String EMG_DEVICE_LATEST_KEY = "emg-device-latest";
    private static final String MOTOR_DEVICE_LATEST_KEY = "motor-device-latest";
    private static final Duration DEVICE_INFO_TTL = Duration.ofMinutes(10);

    private final StringRedisTemplate stringRedisTemplate;

    public EmgDeviceInfoResponse saveEmgDeviceInfo(EmgDeviceInfoRequest request) {
        String key = EMG_DEVICE_INFO_PREFIX + request.emgDeviceId();

        try {
            log.info("[EMG] save start - key={}, status={}", key, request.connectionStatus());

            stringRedisTemplate.opsForHash().putAll(
                    key,
                    Map.of(
                            "connectionStatus", request.connectionStatus(),
                            "updatedAt", now()
                    )
            );

            Boolean expireApplied = stringRedisTemplate.expire(key, DEVICE_INFO_TTL);
            if (Boolean.FALSE.equals(expireApplied)) {
                log.warn("[EMG] failed to set TTL for device info key - key={}", key);
            }

            stringRedisTemplate.opsForValue().set(
                    EMG_DEVICE_LATEST_KEY,
                    request.emgDeviceId(),
                    DEVICE_INFO_TTL
            );

            Object savedStatus = stringRedisTemplate.opsForHash().get(key, "connectionStatus");
            log.info("[EMG] save done - key={}, savedStatus={}", key, savedStatus);

            return new EmgDeviceInfoResponse(
                    request.emgDeviceId(),
                    request.connectionStatus(),
                    savedStatus != null
            );
        } catch (Exception e) {
            log.error("[EMG] redis save failed - key={}", key, e);
            throw e;
        }
    }

    public MotorDeviceInfoResponse saveMotorDeviceInfo(MotorDeviceInfoRequest request) {
        String key = MOTOR_DEVICE_INFO_PREFIX + request.motorDeviceId();

        try {
            log.info("[MOTOR] save start - key={}, status={}", key, request.connectionStatus());

            stringRedisTemplate.opsForHash().putAll(
                    key,
                    Map.of(
                            "connectionStatus", request.connectionStatus(),
                            "updatedAt", now()
                    )
            );

            Boolean expireApplied = stringRedisTemplate.expire(key, DEVICE_INFO_TTL);
            if (Boolean.FALSE.equals(expireApplied)) {
                log.warn("[MOTOR] failed to set TTL for device info key - key={}", key);
            }

            stringRedisTemplate.opsForValue().set(
                    MOTOR_DEVICE_LATEST_KEY,
                    request.motorDeviceId(),
                    DEVICE_INFO_TTL
            );

            Object savedStatus = stringRedisTemplate.opsForHash().get(key, "connectionStatus");
            log.info("[MOTOR] save done - key={}, savedStatus={}", key, savedStatus);

            return new MotorDeviceInfoResponse(
                    request.motorDeviceId(),
                    request.connectionStatus(),
                    savedStatus != null
            );
        } catch (Exception e) {
            log.error("[MOTOR] redis save failed - key={}", key, e);
            throw e;
        }
    }

    public String consumeLatestEmgDeviceId() {
        return stringRedisTemplate.opsForValue().getAndDelete(EMG_DEVICE_LATEST_KEY);
    }

    public String consumeLatestMotorDeviceId() {
        return stringRedisTemplate.opsForValue().getAndDelete(MOTOR_DEVICE_LATEST_KEY);
    }

    private String now() {
        return LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}