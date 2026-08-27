package com.sejong.event.auth.jwt;

import org.springframework.security.core.AuthenticationException;

/**
 * 요청에 토큰이 담겨 있으나 만료·서명 오류·형식 오류로 검증에 실패한 경우.
 * ExceptionTranslationFilter 가 이 예외를 잡아 RestAuthenticationEntryPoint 로 401 을 응답한다.
 */
public class InvalidTokenException extends AuthenticationException {

    public InvalidTokenException(String message) {
        super(message);
    }
}
