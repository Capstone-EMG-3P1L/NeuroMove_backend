package com.neuromove.backend.domain.auth.service;

import com.neuromove.backend.domain.auth.dto.request.LoginRequest;
import com.neuromove.backend.domain.auth.dto.request.RegisterRequest;
import com.neuromove.backend.domain.auth.dto.response.LoginResponse;
import com.neuromove.backend.domain.auth.dto.response.LoginUserResponse;
import com.neuromove.backend.domain.auth.dto.response.RegisterResponse;
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

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new CustomException(ErrorCode.DUPLICATE_USERNAME);
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .build();

        User savedUser = userRepository.save(user);

        return RegisterResponse.from(savedUser);
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        String accessToken = jwtTokenProvider.createAccessToken(user.getUserId(), user.getUsername());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .user(LoginUserResponse.from(user))
                .build();
    }
}