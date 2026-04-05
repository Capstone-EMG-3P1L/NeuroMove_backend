package com.neuromove.backend.domain.session.entity;

import com.neuromove.backend.domain.calibration.entity.CalibrationProfile;
import com.neuromove.backend.domain.device.entity.EmgDevice;
import com.neuromove.backend.domain.device.entity.MotorDevice;
import com.neuromove.backend.domain.session.entity.enums.SessionStatus;
import com.neuromove.backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "sessions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Session {

    @Id
    @Column(name = "session_id", length = 36)
    private String sessionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id")
    private CalibrationProfile calibrationProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emg_device_id")
    private EmgDevice emgDevice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "motor_device_id")
    private MotorDevice motorDevice;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "ENUM('ACTIVE','ENDED')", nullable = false)
    private SessionStatus status;

    @Column(name = "max_risk_score")
    private Float maxRiskScore;

    @Column(name = "started_at", nullable = false, updatable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @PrePersist
    protected void onCreate() {
        if (this.sessionId == null) {
            this.sessionId = UUID.randomUUID().toString();
        }
        this.startedAt = LocalDateTime.now();
        this.status = SessionStatus.ACTIVE;
    }
}
