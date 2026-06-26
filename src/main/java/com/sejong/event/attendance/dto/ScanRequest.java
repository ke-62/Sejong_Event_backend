package com.sejong.event.attendance.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class ScanRequest {

    @NotBlank(message = "학번은 필수입니다.")
    private String studentId;

    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    private String department;

    @NotBlank(message = "QR 값은 필수입니다.")
    private String qrValue;
}
