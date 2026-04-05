package com.neuromove.backend.domain.intent.entity;

import com.neuromove.backend.domain.intent.entity.enums.IntentType;
import com.neuromove.backend.domain.session.entity.Session;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "intent_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class IntentLog {

    @Id
    @Column(name = "intent_id", length = 36)
    private String intentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    @Enumerated(EnumType.STRING)
    @Column(name = "intent", columnDefinition = "ENUM('FORWARD','LEFT','RIGHT','STOP')", nullable = false)
    private IntentType intent;

    @Column(name = "confidence")
    private Float confidence;

    @Column(name = "fatigue_score")
    private Float fatigueScore;

    @Column(name = "signal_quality")
    private Float signalQuality;

    @Column(name = "risk_score")
    private Float riskScore;

    @Column(name = "fatigue_component")
    private Float fatigueComponent;

    @Column(name = "stability_component")
    private Float stabilityComponent;

    @Column(name = "duration_component")
    private Float durationComponent;

    @Column(name = "accepted")
    private Boolean accepted;

    @Column(name = "emg_timestamp")
    private Long emgTimestamp;

    @Column(name = "received_at", nullable = false, updatable = false)
    private LocalDateTime receivedAt;

    @PrePersist
    protected void onCreate() {
        if (this.intentId == null) {
            this.intentId = UUID.randomUUID().toString();
        }
        this.receivedAt = LocalDateTime.now();
    }
}
