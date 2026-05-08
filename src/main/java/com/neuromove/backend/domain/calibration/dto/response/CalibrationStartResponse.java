package com.neuromove.backend.domain.calibration.dto.response;

import com.neuromove.backend.domain.calibration.entity.CalibrationSession;
import com.neuromove.backend.domain.calibration.entity.enums.CalibrationStatus;
import com.neuromove.backend.domain.calibration.entity.enums.CalibrationStep;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CalibrationStartResponse {

    private String calibrationSessionId;
    private String emgDeviceId;
    private CalibrationStatus status;
    private CalibrationStep currentStep;
    private LocalDateTime startedAt;

    public static CalibrationStartResponse from(CalibrationSession session) {
        return CalibrationStartResponse.builder()
                .calibrationSessionId(session.getCalibrationSessionId())
                .emgDeviceId(session.getEmgDevice().getEmgDeviceId())
                .status(session.getStatus())
                .currentStep(session.getCurrentStep())
                .startedAt(session.getStartedAt())
                .build();
    }

    /**
     * 온보딩 모드 응답 팩토리
     * - DB 세션이 아직 없으므로 sessionId는 직접 생성한 UUID
     * - emgDeviceId는 Redis에 등록된 디바이스 ID
     * - 초기 상태는 IN_PROGRESS / REST 단계로 시작
     */
    public static CalibrationStartResponse ofOnboarding(String calibrationSessionId, String emgDeviceId) {
        return CalibrationStartResponse.builder()
                .calibrationSessionId(calibrationSessionId)
                .emgDeviceId(emgDeviceId)
                .status(CalibrationStatus.IN_PROGRESS)
                .currentStep(CalibrationStep.REST)
                .startedAt(java.time.LocalDateTime.now())
                .build();
    }
}
