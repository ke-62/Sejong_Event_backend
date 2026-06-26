package com.sejong.event.waiting.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class WaitingRegisterRequest {

    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    @NotBlank(message = "전화번호는 필수입니다.")
    private String phone;

    @NotNull(message = "인원수는 필수입니다.")
    @Min(value = 1, message = "인원수는 1 이상이어야 합니다.")
    private Integer partySize;
}
