package com.neuromove.backend.domain.calibration.repository;

import com.neuromove.backend.domain.calibration.entity.CalibrationSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CalibrationSessionRepository extends JpaRepository<CalibrationSession, String> {
}
