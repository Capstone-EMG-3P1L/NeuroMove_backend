package com.neuromove.backend.domain.auth.service;

import com.neuromove.backend.global.exception.CustomException;
import com.neuromove.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final String REFRESH_TOKEN_PREFIX = "refresh-token:";

    private final StringRedisTemplate stringRedisTemplate;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    public void save(String userId, String refreshToken) {

        String key = getKey(userId);

        stringRedisTemplate.opsForValue().set(
                key,
                refreshToken,
                Duration.ofMillis(refreshTokenExpiration)
        );
    }

    public void validate(String userId, String refreshToken) {

        String savedRefreshToken =
                stringRedisTemplate.opsForValue().get(getKey(userId));

        if (savedRefreshToken == null) {
            throw new CustomException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }

        if (!savedRefreshToken.equals(refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
    }

    public void delete(String userId) {
        stringRedisTemplate.delete(getKey(userId));
    }

    private String getKey(String userId) {
        return REFRESH_TOKEN_PREFIX + userId;
    }
}