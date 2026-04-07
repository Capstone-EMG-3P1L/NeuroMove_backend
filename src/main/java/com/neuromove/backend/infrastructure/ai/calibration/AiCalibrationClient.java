package com.neuromove.backend.infrastructure.ai.calibration;

import com.neuromove.backend.infrastructure.ai.calibration.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class AiCalibrationClient {

    private final RestTemplate restTemplate;

    @Value("${ai.server.url}")
    private String aiServerUrl;

    public AiCalibrationStartResponse start(AiCalibrationStartRequest request) {
        return restTemplate.postForObject(
                aiServerUrl + "/ai/calibration/start",
                request,
                AiCalibrationStartResponse.class
        );
    }

    public AiCalibrationStepResponse updateStep(AiCalibrationStepRequest request) {
        return restTemplate.patchForObject(
                aiServerUrl + "/ai/calibration/step",
                request,
                AiCalibrationStepResponse.class
        );
    }

    public AiCalibrationFinishResponse finish(AiCalibrationFinishRequest request) {
        return restTemplate.postForObject(
                aiServerUrl + "/ai/calibration/finish",
                request,
                AiCalibrationFinishResponse.class
        );
    }
}
