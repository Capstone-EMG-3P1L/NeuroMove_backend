package com.neuromove.backend.domain.intent.controller;

import com.neuromove.backend.domain.intent.dto.request.IntentReceiveRequest;
import com.neuromove.backend.domain.intent.dto.response.IntentReceiveResponse;
import com.neuromove.backend.domain.intent.service.IntentService;
import com.neuromove.backend.global.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "AI Internal", description = "AI 서버 내부 통신 API (X-Internal-Key 인증)")
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class IntentController {

    private final IntentService intentService;

    @Operation(summary = "AI 추론 결과 수신", description = "AI 서버가 분류한 intent를 수신하여 risk score 계산 후 최종 명령을 결정합니다.")
    @PostMapping("/intent")
    public ApiResponse<IntentReceiveResponse> receiveIntent(
            @Valid @RequestBody IntentReceiveRequest request
    ) {
        IntentReceiveResponse response = intentService.receiveIntent(request);
        return ApiResponse.success("INTENT_RECEIVED", "AI 추론 결과를 수신했습니다.", response);
    }
}
