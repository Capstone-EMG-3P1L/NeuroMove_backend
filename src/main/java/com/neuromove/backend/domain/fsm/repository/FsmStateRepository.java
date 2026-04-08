package com.neuromove.backend.domain.fsm.repository;

import com.neuromove.backend.domain.fsm.entity.FsmState;
import com.neuromove.backend.domain.session.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FsmStateRepository extends JpaRepository<FsmState, String> {

    Optional<FsmState> findTopBySessionOrderByTransitionedAtDesc(Session session);
}
