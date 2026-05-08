package com.neuromove.backend.domain.calibration.dto.response;

import com.neuromove.backend.domain.calibration.entity.CalibrationSession;
import com.neuromove.backend.domain.calibration.entity.enums.CalibrationStep;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CalibrationStepUpdateResponse {

    private String calibrationSessionId;
    private CalibrationStep currentStep;
    private CalibrationStep nextStep;
    private LocalDateTime updatedAt;

    public static CalibrationStepUpdateResponse from(CalibrationSession session) {
        return CalibrationStepUpdateResponse.builder()
                .calibrationSessionId(session.getCalibrationSessionId())
                .currentStep(session.getCurrentStep())
                .nextStep(session.getCurrentStep().next())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 온보딩 모드 응답 팩토리
     * - DB 세션 없이 sessionId와 step 정보만으로 응답 생성
     */
    public static CalibrationStepUpdateResponse ofOnboarding(String calibrationSessionId, CalibrationStep currentStep) {
        return CalibrationStepUpdateResponse.builder()
                .calibrationSessionId(calibrationSessionId)
                .currentStep(currentStep)
                .nextStep(currentStep.next())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
