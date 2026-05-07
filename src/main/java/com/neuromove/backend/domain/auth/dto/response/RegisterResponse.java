package com.neuromove.backend.domain.auth.dto.response;

import com.neuromove.backend.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RegisterResponse {

    private String userId;
    private String username;
    private String name;
    private LocalDateTime createdAt;

    private String accessToken;
    private String refreshToken;
    private String tokenType;

    public static RegisterResponse of(User user,
                                      String accessToken,
                                      String refreshToken) {

        return RegisterResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .name(user.getName())
                .createdAt(user.getCreatedAt())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .build();
    }
}