package com.neuromove.backend.domain.auth.dto.response;

import com.neuromove.backend.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginUserResponse {

    private String userId;
    private String username;
    private String name;

    public static LoginUserResponse from(User user) {
        return LoginUserResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .name(user.getName())
                .build();
    }
}