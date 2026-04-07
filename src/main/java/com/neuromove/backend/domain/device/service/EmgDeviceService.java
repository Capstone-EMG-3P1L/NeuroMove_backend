package com.neuromove.backend.domain.device.service;

import com.neuromove.backend.domain.device.dto.request.EmgDeviceRegisterRequest;
import com.neuromove.backend.domain.device.dto.response.EmgDeviceListResponse;
import com.neuromove.backend.domain.device.dto.response.EmgDeviceRegisterResponse;
import com.neuromove.backend.domain.device.dto.response.EmgDeviceResponse;
import com.neuromove.backend.domain.device.entity.EmgDevice;
import com.neuromove.backend.domain.device.repository.EmgDeviceRepository;
import com.neuromove.backend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmgDeviceService {

    private final EmgDeviceRepository emgDeviceRepository;

    @Transactional
    public EmgDeviceRegisterResponse register(User user, EmgDeviceRegisterRequest request) {
        if (emgDeviceRepository.existsByEmgDeviceId(request.getEmgDeviceId())) {
            throw new IllegalArgumentException("이미 등록된 EMG 디바이스입니다.");
        }

        EmgDevice emgDevice = EmgDevice.builder()
                .emgDeviceId(request.getEmgDeviceId())
                .user(user)
                .name(request.getName())
                .isActive(true)
                .build();

        EmgDevice savedDevice = emgDeviceRepository.save(emgDevice);
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