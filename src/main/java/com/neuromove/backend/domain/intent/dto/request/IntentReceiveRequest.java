package com.neuromove.backend.domain.intent.dto.request;

import com.neuromove.backend.domain.intent.entity.enums.IntentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class IntentReceiveRequest {

    @NotBlank
    private String sessionId;

    @NotBlank
    private String emgDeviceId;

    @NotNull
    private Long timestamp;

    @NotNull
    private IntentType intent;

    @NotNull
    private Float confidence;

    @NotNull
    private Float fatigueScore;

    @NotNull
    private Float signalQuality;
}
