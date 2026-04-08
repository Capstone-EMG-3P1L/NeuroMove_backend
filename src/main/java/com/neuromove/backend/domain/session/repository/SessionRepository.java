package com.neuromove.backend.domain.session.repository;

import com.neuromove.backend.domain.session.entity.Session;
import com.neuromove.backend.domain.session.entity.enums.SessionStatus;
import com.neuromove.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, String> {

    Optional<Session> findFirstByUserAndStatus(User user, SessionStatus status);

    List<Session> findAllByUserOrderByStartedAtDesc(User user);

    Optional<Session> findBySessionIdAndUser(String sessionId, User user);
}