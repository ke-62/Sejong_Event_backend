package com.sejong.event.auth.controller;

import com.sejong.event.auth.dto.LoginRequest;
import com.sejong.event.auth.dto.RegisterRequest;
import com.sejong.event.auth.dto.TokenResponse;
import com.sejong.event.auth.service.AuthService;
import com.sejong.event.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "인증 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "회원가입")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<TokenResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("회원가입이 완료되었습니다.", authService.register(request)));
    }

    @Operation(summary = "로그인")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(authService.login(request)));
    }

    @Operation(summary = "토큰 재발급")
    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<TokenResponse>> reissue(
            @RequestHeader("Refresh-Token") String refreshToken) {
        return ResponseEntity.ok(ApiResponse.ok(authService.reissue(refreshToken)));
    }

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal Long userId) {
        authService.logout(userId);
        return ResponseEntity.ok(ApiResponse.ok("로그아웃 되었습니다.", null));
    }
}
