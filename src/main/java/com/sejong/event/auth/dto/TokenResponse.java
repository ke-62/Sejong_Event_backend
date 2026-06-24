package com.sejong.event.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TokenResponse {

    private String accessToken;
    private String refreshToken;
    private String role;
    private Long userId;
    private String name;
}
