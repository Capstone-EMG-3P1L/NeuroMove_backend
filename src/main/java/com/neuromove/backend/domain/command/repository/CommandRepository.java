package com.neuromove.backend.domain.command.repository;

import com.neuromove.backend.domain.command.entity.Command;
import com.neuromove.backend.domain.session.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommandRepository extends JpaRepository<Command, String> {

    Optional<Command> findTopBySessionOrderByIssuedAtDesc(Session session);
}
