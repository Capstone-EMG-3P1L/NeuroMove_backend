package com.neuromove.backend.domain.device.service;

import com.neuromove.backend.domain.auth.service.OnboardingRedisService;
import com.neuromove.backend.domain.device.dto.request.EmgDeviceRegisterRequest;
import com.neuromove.backend.domain.device.dto.response.EmgDeviceListResponse;
import com.neuromove.backend.domain.device.dto.response.EmgDeviceRegisterResponse;
import com.neuromove.backend.domain.device.dto.response.EmgDeviceResponse;
import com.neuromove.backend.domain.device.repository.EmgDeviceRepository;
import com.neuromove.backend.domain.user.entity.User;
import com.neuromove.backend.global.exception.CustomException;
import com.neuromove.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmgDeviceService {

    private final EmgDeviceRepository emgDeviceRepository;
    private final DeviceInfoService deviceInfoService;
    private final OnboardingRedisService onboardingRedisService;

    public EmgDeviceListResponse getMyDevices(User user) {
        List<EmgDeviceResponse> devices = emgDeviceRepository.findAllByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(EmgDeviceResponse::from)
                .toList();

        return EmgDeviceListResponse.builder()
                .devices(devices)
                .build();
    }

    public EmgDeviceRegisterResponse registerForOnboarding(EmgDeviceRegisterRequest request) {
        // Redis에서 latest deviceId 조회 (기존과 동일한 방식)
        String emgDeviceId = deviceInfoService.getLatestEmgDeviceId();
        if (emgDeviceId == null || emgDeviceId.isBlank()) {
            throw new CustomException(ErrorCode.EMG_DEVICE_NOT_CONNECTED);
        }

        // 온보딩 모드에서는 DB 중복 체크 / DB 저장 X
        // 디바이스 정보를 Redis에만 임시 저장
        onboardingRedisService.saveEmgDevice(
                request.getOnboardingId(),
                emgDeviceId,
                request.getName()
        );

        // 트랜잭션이 없으니 afterCommit 대신 즉시 정리
        deviceInfoService.deleteLatestEmgDeviceIdIfMatch(emgDeviceId);

        // Response: User나 DB ID 없이 만들 수 있는 형태가 필요
        // → 새 정적 팩토리 from(deviceId, name) 또는 ofOnboarding() 추가 필요
        return EmgDeviceRegisterResponse.ofOnboarding(emgDeviceId, request.getName());
    }
}