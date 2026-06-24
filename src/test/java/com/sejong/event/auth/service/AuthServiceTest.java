package com.sejong.event.auth.service;

import com.sejong.event.auth.dto.LoginRequest;
import com.sejong.event.auth.dto.RegisterRequest;
import com.sejong.event.auth.dto.TokenResponse;
import com.sejong.event.auth.jwt.JwtProvider;
import com.sejong.event.common.exception.CustomException;
import com.sejong.event.common.exception.ErrorCode;
import com.sejong.event.user.domain.RefreshToken;
import com.sejong.event.user.domain.Role;
import com.sejong.event.user.domain.User;
import com.sejong.event.user.repository.RefreshTokenRepository;
import com.sejong.event.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock private UserRepository userRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "refreshTokenExpiration", 604800000L);
    }

    private User createUser(Long id, String studentId, Role role) {
        User user = User.builder()
                .studentId(studentId)
                .password("encoded")
                .name("테스트")
                .role(role)
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    // ── 회원가입 ──────────────────────────────────────────

    @Test
    @DisplayName("관리자 계정 생성 성공")
    void register_success() {
        RegisterRequest request = mock(RegisterRequest.class);
        given(request.getStudentId()).willReturn("admin01");
        given(request.getPassword()).willReturn("pass1234");
        given(request.getName()).willReturn("관리자");
        given(request.getRole()).willReturn(Role.ADMIN);

        given(userRepository.existsByStudentId("admin01")).willReturn(false);
        given(passwordEncoder.encode(anyString())).willReturn("encoded");

        User savedUser = createUser(1L, "admin01", Role.ADMIN);
        given(userRepository.save(any(User.class))).willReturn(savedUser);
        given(jwtProvider.generateAccessToken(anyLong(), anyString(), anyString()))
                .willReturn("accessToken");
        given(jwtProvider.generateRefreshToken(anyLong())).willReturn("refreshToken");
        given(refreshTokenRepository.findByUserId(anyLong())).willReturn(Optional.empty());

        TokenResponse response = authService.register(request);

        assertThat(response.getAccessToken()).isEqualTo("accessToken");
        assertThat(response.getRefreshToken()).isEqualTo("refreshToken");
        assertThat(response.getRole()).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("관리자 계정 생성 실패 - 학번 중복")
    void register_duplicateStudentId() {
        RegisterRequest request = mock(RegisterRequest.class);
        given(request.getStudentId()).willReturn("23011679");
        given(userRepository.existsByStudentId("23011679")).willReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.DUPLICATE_STUDENT_ID);
    }

    // ── 로그인 ──────────────────────────────────────────

    @Test
    @DisplayName("로그인 성공 (부스 운영자)")
    void login_success() {
        LoginRequest request = mock(LoginRequest.class);
        given(request.getStudentId()).willReturn("23011679");
        given(request.getPassword()).willReturn("password123");

        User user = createUser(1L, "23011679", Role.BOOTH_OPERATOR);
        given(userRepository.findByStudentId("23011679")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("password123", "encoded")).willReturn(true);
        given(jwtProvider.generateAccessToken(anyLong(), anyString(), anyString()))
                .willReturn("accessToken");
        given(jwtProvider.generateRefreshToken(anyLong())).willReturn("refreshToken");
        given(refreshTokenRepository.findByUserId(anyLong())).willReturn(Optional.empty());

        TokenResponse response = authService.login(request);

        assertThat(response.getAccessToken()).isEqualTo("accessToken");
        assertThat(response.getUserId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("로그인 실패 - 존재하지 않는 학번")
    void login_studentIdNotFound() {
        LoginRequest request = mock(LoginRequest.class);
        given(request.getStudentId()).willReturn("00000000");
        given(userRepository.findByStudentId("00000000")).willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_CREDENTIALS);
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void login_wrongPassword() {
        LoginRequest request = mock(LoginRequest.class);
        given(request.getStudentId()).willReturn("23011679");
        given(request.getPassword()).willReturn("wrongPassword");

        User user = createUser(1L, "23011679", Role.BOOTH_OPERATOR);
        given(userRepository.findByStudentId("23011679")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("wrongPassword", "encoded")).willReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_CREDENTIALS);
    }

    // ── 토큰 재발급 ──────────────────────────────────────────

    @Test
    @DisplayName("토큰 재발급 성공")
    void reissue_success() {
        String refreshToken = "validRefreshToken";
        given(jwtProvider.isTokenValid(refreshToken)).willReturn(true);
        given(jwtProvider.getUserId(refreshToken)).willReturn(1L);

        RefreshToken stored = RefreshToken.builder()
                .userId(1L)
                .token(refreshToken)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        given(refreshTokenRepository.findByUserId(1L)).willReturn(Optional.of(stored));

        User user = createUser(1L, "23011679", Role.BOOTH_OPERATOR);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(jwtProvider.generateAccessToken(anyLong(), anyString(), anyString()))
                .willReturn("newAccessToken");
        given(jwtProvider.generateRefreshToken(anyLong())).willReturn("newRefreshToken");

        TokenResponse response = authService.reissue(refreshToken);

        assertThat(response.getAccessToken()).isEqualTo("newAccessToken");
    }

    @Test
    @DisplayName("토큰 재발급 실패 - 유효하지 않은 토큰")
    void reissue_invalidToken() {
        given(jwtProvider.isTokenValid("badToken")).willReturn(false);

        assertThatThrownBy(() -> authService.reissue("badToken"))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_TOKEN);
    }

    // ── 로그아웃 ──────────────────────────────────────────

    @Test
    @DisplayName("로그아웃 성공")
    void logout_success() {
        authService.logout(1L);
        then(refreshTokenRepository).should().deleteByUserId(1L);
    }
}
