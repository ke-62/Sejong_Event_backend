package com.sejong.event.auth;

import com.sejong.event.auth.jwt.JwtProvider;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthExceptionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JwtProvider jwtProvider;

    @Test
    @DisplayName("토큰 없이 보호된 엔드포인트 → 401")
    void noTokenOnProtectedEndpoint() throws Exception {
        mockMvc.perform(post("/api/events/1/lottery"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("인증이 필요합니다."));
    }

    @Test
    @DisplayName("만료 토큰으로 보호된 엔드포인트 → 401 + 사유")
    void expiredTokenOnProtectedEndpoint() throws Exception {
        mockMvc.perform(post("/api/events/1/lottery")
                        .header("Authorization", "Bearer " + expiredToken()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(Matchers.containsString("만료")));
    }

    @Test
    @DisplayName("형식이 잘못된 토큰 → 401")
    void malformedToken() throws Exception {
        mockMvc.perform(post("/api/events/1/lottery")
                        .header("Authorization", "Bearer not.a.jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("공개 엔드포인트는 토큰 없이 200")
    void publicEndpointWithoutToken() throws Exception {
        mockMvc.perform(get("/api/festival/status"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("운영진 토큰으로 관리자 전용 엔드포인트 → 403")
    void organizerTokenOnSuperuserEndpoint() throws Exception {
        String token = jwtProvider.generateAccessToken(1L, "organizer", "학생회", "ORGANIZER");
        mockMvc.perform(get("/api/accounts").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("접근 권한이 없습니다."));
    }

    @Test
    @DisplayName("슈퍼유저 토큰으로 관리자 전용 엔드포인트 → 200")
    void superuserTokenOnSuperuserEndpoint() throws Exception {
        String token = jwtProvider.generateAccessToken(9L, "admin", "개발팀", "SUPERUSER");
        mockMvc.perform(get("/api/accounts").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    /** 실행 중인 JwtProvider 와 동일한 서명 키로 만료된 토큰을 만든다. */
    private String expiredToken() {
        String secret = (String) ReflectionTestUtils.getField(jwtProvider, "secret");
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject("1")
                .claim("role", "ORGANIZER")
                .issuedAt(new Date(System.currentTimeMillis() - 120_000))
                .expiration(new Date(System.currentTimeMillis() - 60_000))
                .signWith(key)
                .compact();
    }
}
