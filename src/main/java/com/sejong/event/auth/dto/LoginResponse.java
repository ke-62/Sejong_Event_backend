package com.sejong.event.auth.dto;

import com.sejong.event.user.domain.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {

    private String token;
    private UserDto user;

    @Getter
    @Builder
    public static class UserDto {
        private Long id;
        private String username;
        private String name;
        private String organization;
        private String role;
    }

    public static LoginResponse of(String token, User user) {
        return LoginResponse.builder()
                .token(token)
                .user(UserDto.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .name(user.getName())
                        .organization(user.getOrganization())
                        .role(user.getRole().name().toLowerCase())
                        .build())
                .build();
    }
}
