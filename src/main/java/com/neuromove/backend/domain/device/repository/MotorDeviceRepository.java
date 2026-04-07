package com.neuromove.backend.domain.device.repository;

import com.neuromove.backend.domain.device.entity.MotorDevice;
import com.neuromove.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MotorDeviceRepository extends JpaRepository<MotorDevice, String> {

    boolean existsByMotorDeviceId(String motorDeviceId);

    List<MotorDevice> findAllByUserOrderByCreatedAtDesc(User user);
}