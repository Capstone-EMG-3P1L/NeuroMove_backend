package com.neuromove.backend.domain.calibration.entity.enums;

public enum CalibrationStep {
    REST, LEFT, RIGHT, STOP;

    public CalibrationStep next() {
        CalibrationStep[] values = CalibrationStep.values();
        int nextIdx = this.ordinal() + 1;
        return nextIdx < values.length ? values[nextIdx] : null;
    }
}
