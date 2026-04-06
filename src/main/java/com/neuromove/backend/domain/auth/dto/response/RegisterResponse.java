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

    public static RegisterResponse from(User user) {
        return RegisterResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .name(user.getName())
                .createdAt(user.getCreatedAt())
                .build();
    }
}