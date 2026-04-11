package com.neuromove.backend.domain.device.service;

import com.neuromove.backend.domain.device.dto.request.EmgDeviceRegisterRequest;
import com.neuromove.backend.domain.device.dto.response.EmgDeviceListResponse;
import com.neuromove.backend.domain.device.dto.response.EmgDeviceRegisterResponse;
import com.neuromove.backend.domain.device.dto.response.EmgDeviceResponse;
import com.neuromove.backend.domain.device.entity.EmgDevice;
import com.neuromove.backend.domain.device.repository.EmgDeviceRepository;
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
public class EmgDeviceService {

    private final EmgDeviceRepository emgDeviceRepository;
    private final DeviceInfoService deviceInfoService;

    @Transactional
    public EmgDeviceRegisterResponse register(User user, EmgDeviceRegisterRequest request) {
        // Redis에 저장된 현재 latest EMG deviceId를 조회
        // 이 시점에는 삭제하지 않음
        String emgDeviceId = deviceInfoService.getLatestEmgDeviceId();

        // latest 값이 없으면 아직 연결된 EMG 장치가 없다고 판단
        if (emgDeviceId == null || emgDeviceId.isBlank()) {
            throw new CustomException(ErrorCode.EMG_DEVICE_NOT_CONNECTED);
        }

        // 이미 등록된 deviceId라면 중복 등록 방지
        if (emgDeviceRepository.existsByEmgDeviceId(emgDeviceId)) {
            throw new CustomException(ErrorCode.EMG_DEVICE_ALREADY_REGISTERED);
        }

        // 사용자 입력(name)과 Redis에서 읽은 deviceId를 합쳐 최종 등록 엔티티 생성
        EmgDevice emgDevice = EmgDevice.builder()
                .emgDeviceId(emgDeviceId)
                .user(user)
                .name(request.getName())
                .isActive(true)
                .build();

        EmgDevice savedDevice;
        try {
            // 실제 DB 저장
            savedDevice = emgDeviceRepository.save(emgDevice);
        } catch (DataIntegrityViolationException e) {
            // 동시성/유니크 제약 등으로 인한 DB 레벨 중복 예외 방어
            throw new CustomException(ErrorCode.EMG_DEVICE_ALREADY_REGISTERED);
        }

        // afterCommit 콜백 내부에서 사용하기 위해 final 변수로 보관
        final String registeredEmgDeviceId = emgDeviceId;

        // Redis latest key 정리는 save 직후가 아니라 "트랜잭션 commit 성공 후" 수행
        // 이유:
        // save()가 끝났더라도 실제 commit은 메서드 종료 후 일어날 수 있으므로,
        // commit 실패 시 Redis 값이 먼저 사라지는 문제를 막기 위함
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    // 내가 읽은 deviceId와 현재 Redis latest 값이 같을 때만 삭제
                    // 중간에 더 새로운 값으로 바뀐 경우에는 삭제하지 않음
                    boolean deleted = deviceInfoService.deleteLatestEmgDeviceIdIfMatch(registeredEmgDeviceId);

                    if (!deleted) {
                        log.info(
                                "Skipped deleting latest EMG device key because Redis value changed. expectedDeviceId={}",
                                registeredEmgDeviceId
                        );
                    }
                } catch (Exception e) {
                    // Redis 정리 실패가 등록 API 자체를 실패시키지 않도록 로그만 남김
                    log.warn(
                            "Failed to delete latest EMG device key after commit. expectedDeviceId={}",
                            registeredEmgDeviceId,
                            e
                    );
                }
            }
        });

        return EmgDeviceRegisterResponse.from(savedDevice);
    }

    public EmgDeviceListResponse getMyDevices(User user) {
        List<EmgDeviceResponse> devices = emgDeviceRepository.findAllByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(EmgDeviceResponse::from)
                .toList();

        return EmgDeviceListResponse.builder()
                .devices(devices)
                .build();
    }
}