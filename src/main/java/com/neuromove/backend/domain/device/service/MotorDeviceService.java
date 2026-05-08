package com.neuromove.backend.domain.device.service;

import com.neuromove.backend.domain.auth.service.OnboardingRedisService;
import com.neuromove.backend.domain.device.dto.request.MotorDeviceRegisterRequest;
import com.neuromove.backend.domain.device.dto.response.MotorDeviceListResponse;
import com.neuromove.backend.domain.device.dto.response.MotorDeviceRegisterResponse;
import com.neuromove.backend.domain.device.dto.response.MotorDeviceResponse;
import com.neuromove.backend.domain.device.repository.MotorDeviceRepository;
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
public class MotorDeviceService {

    private final MotorDeviceRepository motorDeviceRepository;
    private final DeviceInfoService deviceInfoService;
    private final OnboardingRedisService onboardingRedisService;

    /**
     * 온보딩 모드 MOTOR 디바이스 등록
     * - User가 아직 DB에 없으므로 User 객체 없이 호출됨
     * - DB 저장 X, Redis에 디바이스 정보만 임시 저장
     * - DB 중복 체크는 complete 단계에서 한 번에 수행
     * - 트랜잭션이 없으므로 latest key는 즉시 정리
     */
    public MotorDeviceRegisterResponse registerForOnboarding(MotorDeviceRegisterRequest request) {
        String motorDeviceId = deviceInfoService.getLatestMotorDeviceId();
        if (motorDeviceId == null || motorDeviceId.isBlank()) {
            throw new CustomException(ErrorCode.MOTOR_DEVICE_NOT_CONNECTED);
        }

        onboardingRedisService.saveMotorDevice(
                request.getOnboardingId(),
                motorDeviceId,
                request.getName()
        );

        deviceInfoService.deleteLatestMotorDeviceIdIfMatch(motorDeviceId);

        return MotorDeviceRegisterResponse.ofOnboarding(motorDeviceId, request.getName());
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