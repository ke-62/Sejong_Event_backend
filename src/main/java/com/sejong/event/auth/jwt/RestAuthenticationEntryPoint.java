package com.sejong.event.auth.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 인증되지 않은 요청이 보호된 엔드포인트에 도달했을 때 401 을 응답한다.
 * - 토큰이 만료·무효면 그 사유를 메시지로 (InvalidTokenException)
 * - 토큰이 아예 없으면 "인증이 필요합니다."
 */
@Component
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        String message = (authException instanceof InvalidTokenException)
                ? authException.getMessage()
                : "인증이 필요합니다.";
        SecurityResponseWriter.write(response, HttpStatus.UNAUTHORIZED, message, objectMapper);
    }
}
