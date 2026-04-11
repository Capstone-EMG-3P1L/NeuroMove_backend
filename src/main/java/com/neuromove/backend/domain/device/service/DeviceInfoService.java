package com.neuromove.backend.domain.device.service;

import com.neuromove.backend.domain.device.dto.request.EmgDeviceInfoRequest;
import com.neuromove.backend.domain.device.dto.request.MotorDeviceInfoRequest;
import com.neuromove.backend.domain.device.dto.response.EmgDeviceInfoResponse;
import com.neuromove.backend.domain.device.dto.response.MotorDeviceInfoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
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

    /**
     * Redis latest key 삭제 시,
     * 현재 Redis 값이 expectedDeviceId와 같을 때만 삭제하도록 하는 Lua 스크립트.
     *
     * 이유:
     * get()으로 읽은 뒤 delete()를 별도로 수행하면,
     * 그 사이에 latest 값이 다른 deviceId로 바뀌었을 때 새 값을 잘못 삭제할 수 있음.
     * 이를 원자적으로(compare-and-delete) 처리하기 위해 Lua script 사용.
     */
    private static final DefaultRedisScript<Long> COMPARE_AND_DELETE_SCRIPT =
            new DefaultRedisScript<>(
                    """
                    if redis.call('get', KEYS[1]) == ARGV[1] then
                        return redis.call('del', KEYS[1])
                    else
                        return 0
                    end
                    """,
                    Long.class
            );

    private final StringRedisTemplate stringRedisTemplate;

    public EmgDeviceInfoResponse saveEmgDeviceInfo(EmgDeviceInfoRequest request) {
        String key = EMG_DEVICE_INFO_PREFIX + request.emgDeviceId();

        try {
            log.info("[EMG] save start - key={}, status={}", key, request.connectionStatus());

            // deviceId별 상세 상태 정보를 Redis hash에 저장
            stringRedisTemplate.opsForHash().putAll(
                    key,
                    Map.of(
                            "connectionStatus", request.connectionStatus(),
                            "updatedAt", now()
                    )
            );

            // 상세 상태 정보는 일정 시간 후 자동 만료되도록 TTL 설정
            Boolean expireApplied = stringRedisTemplate.expire(key, DEVICE_INFO_TTL);
            if (Boolean.FALSE.equals(expireApplied)) {
                log.warn("[EMG] failed to set TTL for device info key - key={}", key);
            }

            // 현재 가장 최근에 연결된 EMG deviceId를 latest key에 저장
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

            // deviceId별 상세 상태 정보를 Redis hash에 저장
            stringRedisTemplate.opsForHash().putAll(
                    key,
                    Map.of(
                            "connectionStatus", request.connectionStatus(),
                            "updatedAt", now()
                    )
            );

            // 상세 상태 정보는 일정 시간 후 자동 만료되도록 TTL 설정
            Boolean expireApplied = stringRedisTemplate.expire(key, DEVICE_INFO_TTL);
            if (Boolean.FALSE.equals(expireApplied)) {
                log.warn("[MOTOR] failed to set TTL for device info key - key={}", key);
            }

            // 현재 가장 최근에 연결된 MOTOR deviceId를 latest key에 저장
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

    /**
     * 현재 가장 최근 EMG deviceId 조회
     * - 즉시 삭제하지 않고 조회만 수행
     * - 실제 삭제는 DB commit 이후 별도 수행
     */
    public String getLatestEmgDeviceId() {
        return stringRedisTemplate.opsForValue().get(EMG_DEVICE_LATEST_KEY);
    }

    /**
     * 현재 가장 최근 MOTOR deviceId 조회
     * - 즉시 삭제하지 않고 조회만 수행
     * - 실제 삭제는 DB commit 이후 별도 수행
     */
    public String getLatestMotorDeviceId() {
        return stringRedisTemplate.opsForValue().get(MOTOR_DEVICE_LATEST_KEY);
    }

    /**
     * Redis latest EMG key의 현재 값이 expectedDeviceId와 같을 때만 삭제
     *
     * @return true  -> 삭제 성공
     *         false -> 값이 다르거나 이미 없어서 삭제하지 않음
     */
    public boolean deleteLatestEmgDeviceIdIfMatch(String expectedDeviceId) {
        Long result = stringRedisTemplate.execute(
                COMPARE_AND_DELETE_SCRIPT,
                Collections.singletonList(EMG_DEVICE_LATEST_KEY),
                expectedDeviceId
        );
        return Long.valueOf(1L).equals(result);
    }

    /**
     * Redis latest MOTOR key의 현재 값이 expectedDeviceId와 같을 때만 삭제
     *
     * @return true  -> 삭제 성공
     *         false -> 값이 다르거나 이미 없어서 삭제하지 않음
     */
    public boolean deleteLatestMotorDeviceIdIfMatch(String expectedDeviceId) {
        Long result = stringRedisTemplate.execute(
                COMPARE_AND_DELETE_SCRIPT,
                Collections.singletonList(MOTOR_DEVICE_LATEST_KEY),
                expectedDeviceId
        );
        return Long.valueOf(1L).equals(result);
    }

    private String now() {
        return LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}