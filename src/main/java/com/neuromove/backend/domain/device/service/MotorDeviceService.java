package com.neuromove.backend.domain.device.service;

import com.neuromove.backend.domain.device.dto.request.MotorDeviceRegisterRequest;
import com.neuromove.backend.domain.device.dto.response.MotorDeviceListResponse;
import com.neuromove.backend.domain.device.dto.response.MotorDeviceRegisterResponse;
import com.neuromove.backend.domain.device.dto.response.MotorDeviceResponse;
import com.neuromove.backend.domain.device.entity.MotorDevice;
import com.neuromove.backend.domain.device.entity.enums.ConnectionStatus;
import com.neuromove.backend.domain.device.repository.MotorDeviceRepository;
import com.neuromove.backend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MotorDeviceService {

    private final MotorDeviceRepository motorDeviceRepository;

    @Transactional
    public MotorDeviceRegisterResponse register(User user, MotorDeviceRegisterRequest request) {
        if (motorDeviceRepository.existsByMotorDeviceId(request.getMotorDeviceId())) {
            throw new IllegalArgumentException("이미 등록된 모터 디바이스입니다.");
        }

        MotorDevice motorDevice = MotorDevice.builder()
                .motorDeviceId(request.getMotorDeviceId())
                .user(user)
                .name(request.getName())
                .isActive(true)
                .connectionStatus(ConnectionStatus.DISCONNECTED)
                .build();

        MotorDevice savedDevice = motorDeviceRepository.save(motorDevice);
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