package com.neuromove.backend.domain.calibration.repository;

import com.neuromove.backend.domain.calibration.entity.CalibrationProfile;
import com.neuromove.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CalibrationProfileRepository extends JpaRepository<CalibrationProfile, String> {

    Optional<CalibrationProfile> findByUserAndIsActiveTrue(User user);

    Optional<CalibrationProfile> findFirstByUserAndIsActiveTrue(User user);
}