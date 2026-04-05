package com.neuromove.backend.domain.fsm.entity.enums;

public enum FsmStateType {
    IDLE,
    CALIBRATING,
    READY,
    DRIVING,
    FATIGUE_COMPENSATING,
    EMERGENCY_STOP
}
