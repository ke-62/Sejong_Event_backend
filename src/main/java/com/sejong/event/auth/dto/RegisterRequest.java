package com.sejong.event.auth.dto;

import com.sejong.event.user.domain.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

/**
 * 관리자 계정 직접 생성용 (내부 운영용).
 * 부스 운영자 계정은 부스 신청 승인을 통해 자동 생성된다.
 */
@Getter
public class RegisterRequest {

    @NotBlank(message = "학번(또는 관리자 ID)은 필수입니다.")
    private String studentId;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 4, message = "비밀번호는 4자 이상이어야 합니다.")
    private String password;

    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    @NotNull(message = "역할은 필수입니다.")
    private Role role;
}
