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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
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
        String motorDeviceId = deviceInfoService.getLatestMotorDeviceId();

        if (motorDeviceId == null || motorDeviceId.isBlank()) {
            throw new CustomException(ErrorCode.MOTOR_DEVICE_NOT_CONNECTED);
        }

        if (motorDeviceRepository.existsByMotorDeviceId(motorDeviceId)) {
            throw new CustomException(ErrorCode.MOTOR_DEVICE_ALREADY_REGISTERED);
        }

        MotorDevice motorDevice = MotorDevice.builder()
                .motorDeviceId(motorDeviceId)
                .user(user)
                .name(request.getName())
                .isActive(true)
                .connectionStatus(ConnectionStatus.CONNECTED)
                .build();

        MotorDevice savedDevice;
        try {
            savedDevice = motorDeviceRepository.save(motorDevice);
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(ErrorCode.MOTOR_DEVICE_ALREADY_REGISTERED);
        }

        try {
            deviceInfoService.deleteLatestMotorDeviceId();
        } catch (Exception e) {
            log.warn("Failed to delete latest MOTOR device key", e);
        }

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