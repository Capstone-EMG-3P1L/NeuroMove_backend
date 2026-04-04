package com.neuromove.backend.domain.calibration.entity;

import com.neuromove.backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "calibration_profiles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CalibrationProfile {

    @Id
    @Column(name = "profile_id", length = 36)
    private String profileId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "calibration_session_id", length = 36)
    private String calibrationSessionId;

    @Column(name = "ch1_mean")
    private Float ch1Mean;

    @Column(name = "ch1_std")
    private Float ch1Std;

    @Column(name = "ch2_mean")
    private Float ch2Mean;

    @Column(name = "ch2_std")
    private Float ch2Std;

    @Column(name = "ch3_mean")
    private Float ch3Mean;

    @Column(name = "ch3_std")
    private Float ch3Std;

    @Column(name = "activation_threshold")
    private Float activationThreshold;

    @Column(name = "intent_threshold_left")
    private Float intentThresholdLeft;

    @Column(name = "intent_threshold_right")
    private Float intentThresholdRight;

    @Column(name = "intent_threshold_forward")
    private Float intentThresholdForward;

    @Column(name = "fatigue_baseline")
    private Float fatigueBaseline;

    @Column(name = "signal_quality")
    private Float signalQuality;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
