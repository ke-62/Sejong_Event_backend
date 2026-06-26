package com.sejong.event.festival.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class UpdateFestivalStatusRequest {

    @NotNull(message = "isActive는 필수입니다.")
    private Boolean isActive;
}
