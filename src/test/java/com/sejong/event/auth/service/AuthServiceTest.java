package com.sejong.event.auth.service;

import com.sejong.event.auth.dto.LoginResponse;
import com.sejong.event.auth.jwt.JwtProvider;
import com.sejong.event.common.exception.CustomException;
import com.sejong.event.common.exception.ErrorCode;
import com.sejong.event.support.TestFixtures;
import com.sejong.event.support.TestRequests;
import com.sejong.event.user.domain.User;
import com.sejong.event.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtProvider jwtProvider;
    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("로그인 성공 시 토큰과 사용자 정보를 반환한다")
    void loginSuccess() {
        User user = TestFixtures.organizer("컴퓨터공학과 학생회");
        given(userRepository.findByUsername("organizer")).willReturn(Optional.of(user));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(true);
        given(jwtProvider.generateAccessToken(any(), any(), any(), any())).willReturn("jwt-token");

        LoginResponse response = authService.login(TestRequests.login("organizer", "pw"));

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getUser().getOrganization()).isEqualTo("컴퓨터공학과 학생회");
        assertThat(response.getUser().getRole()).isEqualTo("organizer");
    }

    @Test
    @DisplayName("비밀번호가 틀리면 INVALID_CREDENTIALS")
    void loginWrongPassword() {
        User user = TestFixtures.organizer("컴퓨터공학과 학생회");
        given(userRepository.findByUsername("organizer")).willReturn(Optional.of(user));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(false);

        assertThatThrownBy(() -> authService.login(TestRequests.login("organizer", "wrong")))
                .isInstanceOfSatisfying(CustomException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.INVALID_CREDENTIALS));
    }

    @Test
    @DisplayName("존재하지 않는 아이디면 INVALID_CREDENTIALS")
    void loginUnknownUsername() {
        given(userRepository.findByUsername("ghost")).willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(TestRequests.login("ghost", "pw")))
                .isInstanceOfSatisfying(CustomException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.INVALID_CREDENTIALS));
    }

    @Test
    @DisplayName("비활성 계정이면 UNAUTHORIZED")
    void loginInactiveAccount() {
        User user = TestFixtures.organizer("컴퓨터공학과 학생회");
        user.setActive(false);
        given(userRepository.findByUsername("organizer")).willReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(TestRequests.login("organizer", "pw")))
                .isInstanceOfSatisfying(CustomException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED));
    }
}
