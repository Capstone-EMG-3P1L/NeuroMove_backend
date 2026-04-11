package com.neuromove.backend.domain.device.service;

import com.neuromove.backend.domain.device.dto.request.MotorDeviceRegisterRequest;
import com.neuromove.backend.domain.device.dto.response.MotorDeviceListResponse;
import com.neuromove.backend.domain.device.dto.response.MotorDeviceRegisterResponse;
import com.neuromove.backend.domain.device.dto.response.MotorDeviceResponse;
import com.neuromove.backend.domain.device.entity.MotorDevice;
import com.neuromove.backend.domain.device.entity.enums.ConnectionStatus;
import com.neuromove.backend.domain.device.repository.MotorDeviceRepository;
import com.neuromove.backend.domain.user.entity.User;
import com.neuromove.backend.global.exception.CustomException;
import com.neuromove.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MotorDeviceService {

    private final MotorDeviceRepository motorDeviceRepository;
    private final DeviceInfoService deviceInfoService;

    @Transactional
    public MotorDeviceRegisterResponse register(User user, MotorDeviceRegisterRequest request) {
        // Redis에 저장된 현재 latest MOTOR deviceId 조회
        // 조회만 하고 즉시 삭제하지 않음
        String motorDeviceId = deviceInfoService.getLatestMotorDeviceId();

        // latest 값이 없으면 연결된 MOTOR 장치가 없다고 판단
        if (motorDeviceId == null || motorDeviceId.isBlank()) {
            throw new CustomException(ErrorCode.MOTOR_DEVICE_NOT_CONNECTED);
        }

        // 이미 등록된 deviceId라면 중복 등록 방지
        if (motorDeviceRepository.existsByMotorDeviceId(motorDeviceId)) {
            throw new CustomException(ErrorCode.MOTOR_DEVICE_ALREADY_REGISTERED);
        }

        // Redis에서 읽은 deviceId + 사용자 입력(name)으로 최종 등록 엔티티 생성
        MotorDevice motorDevice = MotorDevice.builder()
                .motorDeviceId(motorDeviceId)
                .user(user)
                .name(request.getName())
                .isActive(true)
                .connectionStatus(ConnectionStatus.CONNECTED)
                .build();

        MotorDevice savedDevice;
        try {
            // 실제 DB 저장
            savedDevice = motorDeviceRepository.save(motorDevice);
        } catch (DataIntegrityViolationException e) {
            // 유니크 제약 등으로 인한 DB 레벨 중복 예외 방어
            throw new CustomException(ErrorCode.MOTOR_DEVICE_ALREADY_REGISTERED);
        }

        final String registeredMotorDeviceId = motorDeviceId;

        // Redis latest key 정리는 트랜잭션 commit 성공 후 수행
        // commit 전에 삭제하면, 이후 롤백 시 Redis 값까지 사라져 재시도가 불가능해질 수 있음
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    // 내가 읽은 deviceId와 Redis 현재 값이 일치할 때만 삭제
                    boolean deleted = deviceInfoService.deleteLatestMotorDeviceIdIfMatch(registeredMotorDeviceId);

                    if (!deleted) {
                        log.info(
                                "Skipped deleting latest MOTOR device key because Redis value changed. expectedDeviceId={}",
                                registeredMotorDeviceId
                        );
                    }
                } catch (Exception e) {
                    // Redis 정리 실패는 등록 성공 자체를 실패로 만들지 않음
                    log.warn(
                            "Failed to delete latest MOTOR device key after commit. expectedDeviceId={}",
                            registeredMotorDeviceId,
                            e
                    );
                }
            }
        });

        return MotorDeviceRegisterResponse.from(savedDevice);
    }

    public MotorDeviceListResponse getMyDevices(User user) {
        List<MotorDeviceResponse> devices = motorDeviceRepository.findAllByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(MotorDeviceResponse::from)
                .toList();

        return MotorDeviceListResponse.builder()
                .devices(devices)
                .build();
    }
}