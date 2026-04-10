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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmgDeviceService {

    private final EmgDeviceRepository emgDeviceRepository;
    private final DeviceInfoService deviceInfoService;

    @Transactional
    public EmgDeviceRegisterResponse register(User user, EmgDeviceRegisterRequest request) {
        String emgDeviceId = deviceInfoService.getLatestEmgDeviceId();

        if (emgDeviceId == null || emgDeviceId.isBlank()) {
            throw new CustomException(ErrorCode.EMG_DEVICE_NOT_CONNECTED);
        }

        if (emgDeviceRepository.existsByEmgDeviceId(emgDeviceId)) {
            throw new CustomException(ErrorCode.EMG_DEVICE_ALREADY_REGISTERED);
        }

        EmgDevice emgDevice = EmgDevice.builder()
                .emgDeviceId(emgDeviceId)
                .user(user)
                .name(request.getName())
                .isActive(true)
                .build();

        EmgDevice savedDevice = emgDeviceRepository.save(emgDevice);

        deviceInfoService.clearLatestEmgDeviceId();

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