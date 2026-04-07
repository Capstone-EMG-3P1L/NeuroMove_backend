package com.neuromove.backend.domain.command.repository;

import com.neuromove.backend.domain.command.entity.Command;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommandRepository extends JpaRepository<Command, String> {
}
