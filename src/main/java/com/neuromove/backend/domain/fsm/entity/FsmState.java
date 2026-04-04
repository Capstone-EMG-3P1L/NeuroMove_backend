package com.neuromove.backend.domain.fsm.entity;

import com.neuromove.backend.domain.fsm.entity.enums.FsmStateType;
import com.neuromove.backend.domain.session.entity.Session;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "fsm_states")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class FsmState {

    @Id
    @Column(name = "fsm_id", length = 36)
    private String fsmId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_state", columnDefinition = "ENUM('IDLE','CALIBRATING','READY','DRIVING','FATIGUE_COMPENSATING','EMERGENCY_STOP')")
    private FsmStateType fromState;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_state", columnDefinition = "ENUM('IDLE','CALIBRATING','READY','DRIVING','FATIGUE_COMPENSATING','EMERGENCY_STOP')", nullable = false)
    private FsmStateType toState;

    @Column(name = "reason", length = 255)
    private String reason;

    @Column(name = "transitioned_at", nullable = false, updatable = false)
    private LocalDateTime transitionedAt;

    @PrePersist
    protected void onCreate() {
        this.transitionedAt = LocalDateTime.now();
    }
}
