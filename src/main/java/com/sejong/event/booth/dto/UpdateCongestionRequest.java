package com.sejong.event.booth.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class UpdateCongestionRequest {

    @NotNull(message = "대기 인원은 필수입니다.")
    @Min(value = 0, message = "대기 인원은 0 이상이어야 합니다.")
    private Integer waitingCount;
}
