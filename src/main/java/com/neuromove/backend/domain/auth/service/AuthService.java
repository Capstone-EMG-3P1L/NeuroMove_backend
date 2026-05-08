package com.neuromove.backend.domain.auth.service;

import com.neuromove.backend.domain.auth.dto.request.LoginRequest;
import com.neuromove.backend.domain.auth.dto.request.RefreshTokenRequest;
import com.neuromove.backend.domain.auth.dto.request.RegisterRequest;
import com.neuromove.backend.domain.auth.dto.response.LoginResponse;
import com.neuromove.backend.domain.auth.dto.response.LoginUserResponse;
import com.neuromove.backend.domain.auth.dto.response.OnboardingStartResponse;
import com.neuromove.backend.domain.auth.dto.response.TokenRefreshResponse;
import com.neuromove.backend.domain.auth.jwt.JwtTokenProvider;
import com.neuromove.backend.domain.user.entity.User;
import com.neuromove.backend.domain.user.repository.UserRepository;
import com.neuromove.backend.global.exception.CustomException;
import com.neuromove.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    // Redis 기반 Refresh Token 저장/검증 서비스
    private final RefreshTokenService refreshTokenService;

    // 온보딩 임시 데이터 Redis 저장 서비스
    private final OnboardingRedisService onboardingRedisService;

    /**
     * 온보딩 시작
     * - 회원 정보를 DB에 저장하지 않고 Redis에 임시 저장
     * - onboardingId 발급하여 이후 단계(EMG/Motor/Calibration)에서 사용하도록 반환
     * - 마지막 complete 단계에서 비로소 User Entity로 DB에 저장됨
     *
     * 시작 시점에 username 중복을 미리 검사하는 이유:
     * 사용자가 EMG/Motor/Calibration 다 끝내고 마지막 단계에서 "이미 사용 중인 ID"
     * 에러를 받으면 처음부터 다시 해야 하므로 UX가 매우 나쁨.
     * 다만 시작 단계 이후 다른 사용자가 같은 username을 가져갈 수 있는 race condition은
     * 남아있으므로, complete 단계에서 한 번 더 검사한다.
     */
    @Transactional(readOnly = true)
    public OnboardingStartResponse startOnboarding(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new CustomException(ErrorCode.DUPLICATE_USERNAME);
        }

        String onboardingId = onboardingRedisService.saveRegisterData(request);

        return OnboardingStartResponse.builder()
                .onboardingId(onboardingId)
                .build();
    }

    /**
     * 로그인
     * - 사용자 인증
     * - access token / refresh token 발급
     * - refresh token Redis 저장
     */
    public LoginResponse login(LoginRequest request) {

        // username으로 사용자 조회
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() ->
                        new CustomException(ErrorCode.AUTHENTICATION_FAILED));

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.AUTHENTICATION_FAILED);
        }

        // Access Token 생성
        String accessToken = jwtTokenProvider.createAccessToken(
                user.getUserId(),
                user.getUsername()
        );

        // Refresh Token 생성
        String refreshToken = jwtTokenProvider.createRefreshToken(
                user.getUserId(),
                user.getUsername()
        );

        // Redis에 Refresh Token 저장
        refreshTokenService.save(user.getUserId(), refreshToken);

        // 로그인 응답 반환
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .user(LoginUserResponse.from(user))
                .build();
    }

    /**
     * Access Token 재발급
     * - Refresh Token JWT 자체 유효성 검증
     * - Refresh Token 내부 userId / username 추출
     * - 새로운 Access Token / Refresh Token 발급
     * - Redis에 저장된 Refresh Token을 새로운 값으로 교체
     */
    public TokenRefreshResponse refresh(RefreshTokenRequest request) {

        String refreshToken = request.getRefreshToken();

        // JWT 자체 유효성 검사
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // Refresh Token 내부 정보 추출
        String userId = jwtTokenProvider.getUserId(refreshToken);
        String username = jwtTokenProvider.getUsername(refreshToken);

        // 토큰에 담긴 사용자가 현재 DB에도 존재하는지 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 새로운 Access Token 발급
        String newAccessToken =
                jwtTokenProvider.createAccessToken(user.getUserId(), user.getUsername());

        // 새로운 Refresh Token 발급
        String newRefreshToken =
                jwtTokenProvider.createRefreshToken(user.getUserId(), user.getUsername());

        /*
         * Refresh Token Rotation
         * - 기존 Refresh Token이 Redis에 저장된 값과 일치하는지 검증
         * - 일치하면 새로운 Refresh Token으로 Redis 값을 교체
         *
         * 기존 코드처럼 validate()와 save()를 AuthService에서 따로 호출하면
         * 동시 재발급 요청 시 두 요청이 모두 검증을 통과할 수 있다.
         * 따라서 RefreshTokenService의 rotate() 메서드에서
         * 검증과 교체를 하나의 흐름으로 처리한다.
         */
        refreshTokenService.rotate(
                userId,
                refreshToken,
                newRefreshToken
        );

        // 재발급 응답 반환
        return TokenRefreshResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .build();
    }

    /**
     * 로그아웃
     * - Redis에 저장된 Refresh Token 삭제
     */
    public void logout(String userId) {
        refreshTokenService.delete(userId);
    }
}