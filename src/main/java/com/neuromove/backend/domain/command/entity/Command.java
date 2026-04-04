package com.neuromove.backend.domain.command.entity;

import com.neuromove.backend.domain.command.entity.enums.CommandType;
import com.neuromove.backend.domain.intent.entity.IntentLog;
import com.neuromove.backend.domain.session.entity.Session;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "commands")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Command {

    @Id
    @Column(name = "command_id", length = 36)
    private String commandId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "intent_id", nullable = false)
    private IntentLog intentLog;

    @Enumerated(EnumType.STRING)
    @Column(name = "command", columnDefinition = "ENUM('FORWARD','LEFT','RIGHT','STOP','EMERGENCY_STOP','BLOCKED')", nullable = false)
    private CommandType command;

    @Column(name = "speed_level")
    private Integer speedLevel;

    @Column(name = "risk_score")
    private Float riskScore;

    @Column(name = "is_fetched", nullable = false)
    private Boolean isFetched;

    @Column(name = "issued_at", nullable = false, updatable = false)
    private LocalDateTime issuedAt;

    @Column(name = "fetched_at")
    private LocalDateTime fetchedAt;

    @PrePersist
    protected void onCreate() {
        if (this.commandId == null) {
            this.commandId = UUID.randomUUID().toString();
        }
        this.issuedAt = LocalDateTime.now();
        this.isFetched = false;
    }
}
