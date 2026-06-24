package com.sejong.event.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class LoginRequest {

    @NotBlank(message = "학번은 필수입니다.")
    private String studentId; // 이메일 대신 학번으로 로그인

    @NotBlank(message = "비밀번호는 필수입니다.")
    private String password;
}
