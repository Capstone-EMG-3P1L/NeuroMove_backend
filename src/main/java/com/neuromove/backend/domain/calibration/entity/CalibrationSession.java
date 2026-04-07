package com.neuromove.backend.domain.calibration.entity;

import com.neuromove.backend.domain.calibration.entity.enums.CalibrationStatus;
import com.neuromove.backend.domain.calibration.entity.enums.CalibrationStep;
import com.neuromove.backend.domain.device.entity.EmgDevice;
import com.neuromove.backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "calibration_sessions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CalibrationSession {

    @Id
    @Column(name = "calibration_session_id", length = 36)
    private String calibrationSessionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emg_device_id", nullable = false)
    private EmgDevice emgDevice;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "ENUM('IN_PROGRESS','COMPLETED')", nullable = false)
    private CalibrationStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_step", columnDefinition = "ENUM('REST','LEFT','RIGHT','STOP')", nullable = false)
    private CalibrationStep currentStep;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "calibration_session_completed_steps",
            joinColumns = @JoinColumn(name = "calibration_session_id"))
    @Column(name = "step")
    @Enumerated(EnumType.STRING)
    @OrderColumn(name = "step_order")
    @Builder.Default
    private List<CalibrationStep> completedSteps = new ArrayList<>();

    @Column(name = "signal_quality")
    private Float signalQuality;

    @Column(name = "started_at", nullable = false, updatable = false)
    private LocalDateTime startedAt;

    @PrePersist
    protected void onCreate() {
        if (this.calibrationSessionId == null) {
            this.calibrationSessionId = UUID.randomUUID().toString();
        }
        this.startedAt = LocalDateTime.now();
        this.status = CalibrationStatus.IN_PROGRESS;
        this.currentStep = CalibrationStep.REST;
    }

    public void proceedToStep(CalibrationStep newStep) {
        this.completedSteps.add(this.currentStep);
        this.currentStep = newStep;
    }

    public void complete(Float signalQuality) {
        this.completedSteps.add(this.currentStep);
        this.status = CalibrationStatus.COMPLETED;
        this.signalQuality = signalQuality;
    }
}
