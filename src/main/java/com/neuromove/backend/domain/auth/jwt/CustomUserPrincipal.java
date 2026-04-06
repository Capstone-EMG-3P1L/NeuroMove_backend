package com.neuromove.backend.domain.auth.jwt;

public record CustomUserPrincipal(
        String userId,
        String username
) {
}