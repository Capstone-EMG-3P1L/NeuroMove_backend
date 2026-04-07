package com.neuromove.backend.domain.session.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SessionStartRequest {

    @NotBlank
    private String profileId;

    @NotBlank
    private String emgDeviceId;

    @NotBlank
    private String motorDeviceId;
}
