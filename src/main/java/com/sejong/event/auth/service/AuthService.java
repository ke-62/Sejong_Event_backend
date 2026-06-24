package com.sejong.event.auth.service;

import com.sejong.event.auth.dto.LoginRequest;
import com.sejong.event.auth.dto.RegisterRequest;
import com.sejong.event.auth.dto.TokenResponse;
import com.sejong.event.auth.jwt.JwtProvider;
import com.sejong.event.common.exception.CustomException;
import com.sejong.event.common.exception.ErrorCode;
import com.sejong.event.user.domain.RefreshToken;
import com.sejong.event.user.domain.User;
import com.sejong.event.user.repository.RefreshTokenRepository;
import com.sejong.event.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    /** 관리자 계정 직접 생성 (내부 운영용) */
    @Transactional
    public TokenResponse register(RegisterRequest request) {
        if (userRepository.existsByStudentId(request.getStudentId())) {
            throw new CustomException(ErrorCode.DUPLICATE_STUDENT_ID);
        }

        User user = User.builder()
                .studentId(request.getStudentId())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .role(request.getRole())
                .build();

        return generateTokenResponse(userRepository.save(user));
    }

    /** 로그인 — 학번 + 비밀번호 */
    @Transactional
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByStudentId(request.getStudentId())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }

        return generateTokenResponse(user);
    }

    @Transactional
    public TokenResponse reissue(String refreshToken) {
        if (!jwtProvider.isTokenValid(refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        Long userId = jwtProvider.getUserId(refreshToken);

        RefreshToken stored = refreshTokenRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_TOKEN));

        if (!stored.getToken().equals(refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        return generateTokenResponse(
                userRepository.findById(userId)
                        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND))
        );
    }

    @Transactional
    public void logout(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

    private TokenResponse generateTokenResponse(User user) {
        String accessToken = jwtProvider.generateAccessToken(
                user.getId(), user.getStudentId(), user.getRole().name());
        String refreshToken = jwtProvider.generateRefreshToken(user.getId());

        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(refreshTokenExpiration / 1000);

        refreshTokenRepository.findByUserId(user.getId())
                .ifPresentOrElse(
                        token -> token.updateToken(refreshToken, expiresAt),
                        () -> refreshTokenRepository.save(RefreshToken.builder()
                                .userId(user.getId())
                                .token(refreshToken)
                                .expiresAt(expiresAt)
                                .build())
                );

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .role(user.getRole().name())
                .userId(user.getId())
                .name(user.getName())
                .build();
    }
}
