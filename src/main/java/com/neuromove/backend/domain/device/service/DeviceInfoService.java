package com.neuromove.backend.domain.device.service;

import com.neuromove.backend.domain.device.dto.request.EmgDeviceInfoRequest;
import com.neuromove.backend.domain.device.dto.request.MotorDeviceInfoRequest;
import com.neuromove.backend.domain.device.dto.response.EmgDeviceInfoResponse;
import com.neuromove.backend.domain.device.dto.response.MotorDeviceInfoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceInfoService {

    private final StringRedisTemplate stringRedisTemplate;

    public EmgDeviceInfoResponse saveEmgDeviceInfo(EmgDeviceInfoRequest request) {
        String key = "emg-device-info:" + request.emgDeviceId();

        try {
            log.info("[EMG] save start - key={}, status={}", key, request.connectionStatus());

            stringRedisTemplate.opsForHash().putAll(
                    key,
                    Map.of(
                            "connectionStatus", request.connectionStatus(),
                            "updatedAt", now()
                    )
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
        String key = "motor-device-info:" + request.motorDeviceId();

        try {
            log.info("[MOTOR] save start - key={}, status={}", key, request.connectionStatus());

            stringRedisTemplate.opsForHash().putAll(
                    key,
                    Map.of(
                            "connectionStatus", request.connectionStatus(),
                            "updatedAt", now()
                    )
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

    private String now() {
        return LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}