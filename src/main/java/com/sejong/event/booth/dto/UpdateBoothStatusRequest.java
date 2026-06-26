package com.sejong.event.booth.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class UpdateBoothStatusRequest {

    @NotNull(message = "isOpen은 필수입니다.")
    private Boolean isOpen;
}
