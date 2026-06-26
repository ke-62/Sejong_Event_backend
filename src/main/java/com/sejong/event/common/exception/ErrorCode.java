package com.sejong.event.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // Auth
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    DUPLICATE_USERNAME(HttpStatus.CONFLICT, "이미 존재하는 아이디입니다."),

    // Event
    EVENT_NOT_FOUND(HttpStatus.NOT_FOUND, "행사를 찾을 수 없습니다."),
    EVENT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 행사에 대한 권한이 없습니다."),

    // Attendance
    DUPLICATE_TICKET(HttpStatus.CONFLICT, "이미 입장 처리된 학번입니다."),
    TICKET_NOT_FOUND(HttpStatus.NOT_FOUND, "입장 기록을 찾을 수 없습니다."),
    INVALID_QR_TOKEN(HttpStatus.BAD_REQUEST, "유효하지 않은 QR 토큰입니다."),

    // Booth
    BOOTH_NOT_FOUND(HttpStatus.NOT_FOUND, "부스를 찾을 수 없습니다."),
    BOOTH_ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 부스에 대한 권한이 없습니다."),

    // Waiting
    WAITING_NOT_FOUND(HttpStatus.NOT_FOUND, "웨이팅 정보를 찾을 수 없습니다."),

    // Common
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "잘못된 입력값입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
