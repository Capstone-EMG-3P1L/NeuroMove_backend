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

    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED,"INVALID_REFRESH_TOKEN","유효하지 않은 Refresh Token입니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED,"REFRESH_TOKEN_NOT_FOUND","저장된 Refresh Token을 찾을 수 없습니다."),

    FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN", "접근 권한이 없습니다."),
    USER_NOT_FOUND(HttpStatus.UNAUTHORIZED, "USER_NOT_FOUND", "존재하지 않는 사용자입니다."),

    EMG_DEVICE_NOT_FOUND(HttpStatus.NOT_FOUND, "EMG_DEVICE_NOT_FOUND", "EMG 디바이스를 찾을 수 없습니다."),
    EMG_DEVICE_NOT_CONNECTED(HttpStatus.BAD_REQUEST, "EMG_DEVICE_NOT_CONNECTED", "현재 연결된 EMG 디바이스가 없습니다."),
    EMG_DEVICE_ALREADY_REGISTERED(HttpStatus.CONFLICT, "EMG_DEVICE_ALREADY_REGISTERED", "이미 등록된 EMG 디바이스입니다."),

    CALIBRATION_SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "CALIBRATION_SESSION_NOT_FOUND", "Calibration 세션을 찾을 수 없습니다."),
    CALIBRATION_ALREADY_COMPLETED(HttpStatus.BAD_REQUEST, "CALIBRATION_ALREADY_COMPLETED", "이미 완료된 Calibration 세션입니다."),
    CALIBRATION_PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "CALIBRATION_PROFILE_NOT_FOUND", "Calibration 프로파일을 찾을 수 없습니다."),

    MOTOR_DEVICE_NOT_FOUND(HttpStatus.NOT_FOUND, "MOTOR_DEVICE_NOT_FOUND", "모터 디바이스를 찾을 수 없습니다."),
    MOTOR_DEVICE_NOT_CONNECTED(HttpStatus.BAD_REQUEST, "MOTOR_DEVICE_NOT_CONNECTED", "현재 연결된 MOTOR 디바이스가 없습니다."),
    MOTOR_DEVICE_ALREADY_REGISTERED(HttpStatus.CONFLICT, "MOTOR_DEVICE_ALREADY_REGISTERED", "이미 등록된 MOTOR 디바이스입니다."),

    SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "SESSION_NOT_FOUND", "세션을 찾을 수 없습니다."),
    INVALID_INTERNAL_KEY(HttpStatus.UNAUTHORIZED, "INVALID_INTERNAL_KEY", "유효하지 않은 Internal Key입니다."),

    ONBOARDING_NOT_FOUND(HttpStatus.NOT_FOUND, "ONBOARDING_NOT_FOUND", "온보딩 정보를 찾을 수 없습니다."),
    ONBOARDING_REGISTER_NOT_FOUND(HttpStatus.BAD_REQUEST, "ONBOARDING_REGISTER_NOT_FOUND", "회원가입 정보가 입력되지 않았습니다."),
    ONBOARDING_CALIBRATION_NOT_FOUND(HttpStatus.BAD_REQUEST, "ONBOARDING_CALIBRATION_NOT_FOUND", "캘리브레이션 정보가 입력되지 않았습니다."),
    ONBOARDING_EMG_DEVICE_NOT_FOUND(HttpStatus.BAD_REQUEST, "ONBOARDING_EMG_DEVICE_NOT_FOUND", "EMG 디바이스 정보가 입력되지 않았습니다."),
    ONBOARDING_MOTOR_DEVICE_NOT_FOUND(HttpStatus.BAD_REQUEST, "ONBOARDING_MOTOR_DEVICE_NOT_FOUND", "MOTOR 디바이스 정보가 입력되지 않았습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}