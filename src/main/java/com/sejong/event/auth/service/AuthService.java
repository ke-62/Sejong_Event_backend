package com.sejong.event.auth.service;

import com.sejong.event.auth.dto.LoginRequest;
import com.sejong.event.auth.dto.LoginResponse;
import com.sejong.event.auth.jwt.JwtProvider;
import com.sejong.event.common.exception.CustomException;
import com.sejong.event.common.exception.ErrorCode;
import com.sejong.event.user.domain.User;
import com.sejong.event.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CREDENTIALS));

        if (!user.getIsActive()) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }

        String token = jwtProvider.generateAccessToken(
                user.getId(), user.getUsername(), user.getOrganization(), user.getRole().name());

        return LoginResponse.of(token, user);
    }

    // 로그아웃은 프론트에서 localStorage 삭제로 처리 (서버 무상태)
    public void logout(Long userId) {
        // no-op
    }
}
