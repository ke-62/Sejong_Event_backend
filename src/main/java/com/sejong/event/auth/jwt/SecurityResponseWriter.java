package com.sejong.event.auth.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sejong.event.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/** 인증·인가 실패를 다른 API 오류와 같은 {@link ApiResponse} JSON 형식으로 내려준다. */
final class SecurityResponseWriter {

    private SecurityResponseWriter() {
    }

    static void write(HttpServletResponse response, HttpStatus status, String message,
                      ObjectMapper objectMapper) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getWriter(), ApiResponse.fail(message));
    }
}
