package com.sejong.event.auth.jwt;

import com.sejong.event.common.exception.CustomException;
import com.sejong.event.common.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtProviderTest {

    private static final String SECRET =
            "test-secret-key-for-testing-purposes-must-be-at-least-256-bits-long";

    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider();
        ReflectionTestUtils.setField(jwtProvider, "secret", SECRET);
        ReflectionTestUtils.setField(jwtProvider, "accessTokenExpiration", 1_800_000L);
    }

    @Test
    @DisplayName("액세스 토큰을 발급하고 검증하면 userId·role 클레임을 돌려준다")
    void issueAndValidateAccessToken() {
        String token = jwtProvider.generateAccessToken(7L, "organizer", "컴퓨터공학과 학생회", "ORGANIZER");

        assertThat(jwtProvider.isTokenValid(token)).isTrue();
        assertThat(jwtProvider.getUserId(token)).isEqualTo(7L);
        assertThat(jwtProvider.getRole(token)).isEqualTo("ORGANIZER");
    }

    @Test
    @DisplayName("QR 토큰은 type=ORGANIZER_QR 과 eventId 클레임을 담는다")
    void organizerQrTokenClaims() {
        String token = jwtProvider.generateOrganizerQrToken(42L);

        Claims claims = jwtProvider.validateToken(token);
        assertThat(claims.get("type", String.class)).isEqualTo("ORGANIZER_QR");
        assertThat(claims.get("eventId", Long.class)).isEqualTo(42L);
    }

    @Test
    @DisplayName("만료된 토큰은 isTokenValid=false 이고 validateToken 은 EXPIRED_TOKEN")
    void expiredToken() {
        ReflectionTestUtils.setField(jwtProvider, "accessTokenExpiration", -1_000L);
        String expired = jwtProvider.generateAccessToken(1L, "u", "o", "ORGANIZER");

        assertThat(jwtProvider.isTokenValid(expired)).isFalse();
        assertThatThrownBy(() -> jwtProvider.validateToken(expired))
                .isInstanceOfSatisfying(CustomException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.EXPIRED_TOKEN));
    }

    @Test
    @DisplayName("형식이 잘못된 토큰은 INVALID_TOKEN")
    void malformedToken() {
        assertThatThrownBy(() -> jwtProvider.validateToken("not.a.jwt"))
                .isInstanceOfSatisfying(CustomException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.INVALID_TOKEN));
    }

    @Test
    @DisplayName("다른 키로 서명된 토큰은 INVALID_TOKEN")
    void foreignlySignedToken() {
        JwtProvider other = new JwtProvider();
        ReflectionTestUtils.setField(other, "secret",
                "another-completely-different-secret-key-also-256-bits-long-xyz");
        ReflectionTestUtils.setField(other, "accessTokenExpiration", 1_800_000L);
        String foreign = other.generateAccessToken(1L, "u", "o", "ORGANIZER");

        assertThatThrownBy(() -> jwtProvider.validateToken(foreign))
                .isInstanceOfSatisfying(CustomException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.INVALID_TOKEN));
    }
}
