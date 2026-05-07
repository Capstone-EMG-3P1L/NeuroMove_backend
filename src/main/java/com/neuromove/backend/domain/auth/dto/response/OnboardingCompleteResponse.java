package com.neuromove.backend.domain.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OnboardingCompleteResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private LoginUserResponse user;
}