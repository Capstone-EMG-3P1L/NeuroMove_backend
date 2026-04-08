package com.neuromove.backend.domain.device.repository;

import com.neuromove.backend.domain.device.entity.EmgDevice;
import com.neuromove.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmgDeviceRepository extends JpaRepository<EmgDevice, String> {

    boolean existsByEmgDeviceId(String emgDeviceId);

    List<EmgDevice> findAllByUserOrderByCreatedAtDesc(User user);

    Optional<EmgDevice> findFirstByUserAndIsActiveTrue(User user);
}