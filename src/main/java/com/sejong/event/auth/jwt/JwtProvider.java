package com.sejong.event.auth.jwt;

import com.sejong.event.common.exception.CustomException;
import com.sejong.event.common.exception.ErrorCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
public class JwtProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /** 사용자 Access Token 발급 */
    public String generateAccessToken(Long userId, String studentId, String role) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("studentId", studentId)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    /** 사용자 Refresh Token 발급 */
    public String generateRefreshToken(Long userId) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("type", "REFRESH")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * QR 번호표 토큰 발급 (만료 없음 - 행사 종료까지 유효)
     * 위변조 방지를 위해 서버 서명 사용
     */
    public String generateQrToken(Long ticketId, Long eventId, String studentId, String ticketNumber) {
        return Jwts.builder()
                .subject(String.valueOf(ticketId))
                .claim("eventId", eventId)
                .claim("studentId", studentId)
                .claim("ticketNumber", ticketNumber)
                .claim("type", "QR")
                .issuedAt(new Date())
                .signWith(getSigningKey())
                .compact();
    }

    public Claims validateToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new CustomException(ErrorCode.EXPIRED_TOKEN);
        } catch (JwtException | IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
    }

    public boolean isTokenValid(String token) {
        try {
            validateToken(token);
            return true;
        } catch (CustomException e) {
            return false;
        }
    }

    public Long getUserId(String token) {
        return Long.parseLong(validateToken(token).getSubject());
    }

    public String getRole(String token) {
        return validateToken(token).get("role", String.class);
    }
}
