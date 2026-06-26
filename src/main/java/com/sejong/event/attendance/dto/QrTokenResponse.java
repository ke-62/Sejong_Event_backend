package com.sejong.event.attendance.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QrTokenResponse {

    private String qrToken;
    private Integer expiresIn; // 초 단위 (60)
}
