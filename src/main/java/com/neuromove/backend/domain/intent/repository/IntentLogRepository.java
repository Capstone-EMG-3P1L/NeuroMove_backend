package com.neuromove.backend.domain.intent.repository;

import com.neuromove.backend.domain.intent.entity.IntentLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IntentLogRepository extends JpaRepository<IntentLog, String> {
}
