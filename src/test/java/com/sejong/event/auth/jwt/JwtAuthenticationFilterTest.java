package com.sejong.event.auth.jwt;

import com.sejong.event.common.exception.CustomException;
import com.sejong.event.common.exception.ErrorCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtProvider jwtProvider;
    @InjectMocks
    private JwtAuthenticationFilter filter;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("유효한 토큰이면 인증을 설정하고 체인을 계속 진행한다")
    void validToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer good");
        MockFilterChain chain = new MockFilterChain();
        given(jwtProvider.getUserId("good")).willReturn(5L);
        given(jwtProvider.getRole("good")).willReturn("ORGANIZER");

        filter.doFilter(request, new MockHttpServletResponse(), chain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth.getPrincipal()).isEqualTo(5L);
        assertThat(auth.getAuthorities()).extracting("authority").containsExactly("ROLE_ORGANIZER");
        assertThat(chain.getRequest()).as("체인 계속 진행").isNotNull();
    }

    @Test
    @DisplayName("토큰이 없으면 인증 없이 체인을 계속 진행한다")
    void noToken() throws Exception {
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(chain.getRequest()).isNotNull();
    }

    @Test
    @DisplayName("만료 토큰이면 InvalidTokenException 을 던지고 체인을 진행하지 않는다")
    void expiredToken() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer expired");
        MockFilterChain chain = new MockFilterChain();
        given(jwtProvider.getUserId("expired")).willThrow(new CustomException(ErrorCode.EXPIRED_TOKEN));

        assertThatThrownBy(() -> filter.doFilter(request, new MockHttpServletResponse(), chain))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("만료된 토큰입니다.");

        assertThat(chain.getRequest()).as("체인 미진행").isNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
