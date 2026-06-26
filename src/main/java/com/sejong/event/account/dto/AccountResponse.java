package com.sejong.event.account.dto;

import com.sejong.event.user.domain.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AccountResponse {

    private Long id;
    private String username;
    private String password; // 관리자 콘솔 표시용 (평문 저장 별도)
    private String name;
    private String organization;
    private String role;
    private Boolean isActive;

    public static AccountResponse from(User user, String plainPassword) {
        return AccountResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .password(plainPassword)
                .name(user.getName())
                .organization(user.getOrganization())
                .role(user.getRole().name().toLowerCase())
                .isActive(user.getIsActive())
                .build();
    }

    public static AccountResponse from(User user) {
        return from(user, null);
    }
}
