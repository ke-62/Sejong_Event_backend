package com.sejong.event.ticket.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class QrScanRequest {

    @NotBlank(message = "QR 토큰은 필수입니다.")
    private String qrToken;
}
