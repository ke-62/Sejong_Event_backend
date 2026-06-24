package com.sejong.event.event.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CreateEventRequest {

    @NotBlank(message = "행사 제목은 필수입니다.")
    private String title;

    private String description;

    @Min(value = 1, message = "정족수는 1 이상이어야 합니다.")
    private Integer maxCapacity; // 행사 모드 전용 정족수 (축제 모드는 null 허용)

    private LocalDateTime startAt;

    private LocalDateTime endAt;
}
