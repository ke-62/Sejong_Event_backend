package com.sejong.event.admin.dto;

import com.sejong.event.user.domain.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class UpdateRoleRequest {

    @NotNull(message = "역할은 필수입니다.")
    private Role role;
}
