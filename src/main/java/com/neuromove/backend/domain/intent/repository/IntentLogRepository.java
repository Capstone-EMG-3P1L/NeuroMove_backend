package com.neuromove.backend.domain.intent.repository;

import com.neuromove.backend.domain.intent.entity.IntentLog;
import com.neuromove.backend.domain.session.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IntentLogRepository extends JpaRepository<IntentLog, String> {

    List<IntentLog> findAllBySessionOrderByReceivedAtAsc(Session session);
}
