package com.neuromove.backend.infrastructure.ai.session;

import com.neuromove.backend.infrastructure.ai.session.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class AiSessionClient {

    private final RestTemplate restTemplate;

    @Value("${ai.server.url}")
    private String aiServerUrl;

    public AiSessionStartResponse start(AiSessionStartRequest request) {
        try {
            return restTemplate.postForObject(
                    aiServerUrl + "/ai/sessions/start",
                    request,
                    AiSessionStartResponse.class
            );
        } catch (HttpClientErrorException e) {
            System.out.println("AI session start failed");
            System.out.println("status = " + e.getStatusCode());
            System.out.println("body = " + e.getResponseBodyAsString());
            throw e;
        }
    }

    public AiSessionStatusResponse getStatus(String sessionId) {
        return restTemplate.getForObject(
                aiServerUrl + "/ai/sessions/status/" + sessionId,
                AiSessionStatusResponse.class
        );
    }

    public AiSessionEndResponse end(AiSessionEndRequest request) {
        return restTemplate.postForObject(
                aiServerUrl + "/ai/sessions/end",
                request,
                AiSessionEndResponse.class
        );
    }
}