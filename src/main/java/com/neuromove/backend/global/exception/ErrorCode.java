package com.neuromove.backend.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    DUPLICATE_USERNAME(HttpStatus.CONFLICT, "DUPLICATE_USERNAME", "이미 사용 중인 아이디입니다."),
    AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "AUTHENTICATION_FAILED", "아이디 또는 비밀번호가 올바르지 않습니다."),
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "INVALID_INPUT", "입력값이 올바르지 않습니다."),

    EMG_DEVICE_NOT_FOUND(HttpStatus.NOT_FOUND, "EMG_DEVICE_NOT_FOUND", "EMG 디바이스를 찾을 수 없습니다."),
    CALIBRATION_SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "CALIBRATION_SESSION_NOT_FOUND", "Calibration 세션을 찾을 수 없습니다."),
    CALIBRATION_ALREADY_COMPLETED(HttpStatus.BAD_REQUEST, "CALIBRATION_ALREADY_COMPLETED", "이미 완료된 Calibration 세션입니다."),
    CALIBRATION_PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "CALIBRATION_PROFILE_NOT_FOUND", "Calibration 프로파일을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}