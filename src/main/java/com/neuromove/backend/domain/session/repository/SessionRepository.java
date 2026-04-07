package com.neuromove.backend.domain.session.repository;

import com.neuromove.backend.domain.session.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionRepository extends JpaRepository<Session, String> {
}
