package com.neuromove.backend.domain.device.entity;

import com.neuromove.backend.domain.device.entity.enums.ConnectionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "motor_devices")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MotorDevice {

    @Id
    @Column(name = "motor_device_id", length = 50)
    private String motorDeviceId;

    @Column(name = "name", length = 50)
    private String name;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Enumerated(EnumType.STRING)
    @Column(name = "connection_status", columnDefinition = "ENUM('CONNECTED','DISCONNECTED')", nullable = false)
    private ConnectionStatus connectionStatus;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.motorDeviceId == null) {
            this.motorDeviceId = UUID.randomUUID().toString();
        }
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
