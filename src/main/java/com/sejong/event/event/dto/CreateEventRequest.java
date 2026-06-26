package com.sejong.event.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CreateEventRequest {

    @NotBlank(message = "행사명은 필수입니다.")
    private String name;

    @NotBlank(message = "장소는 필수입니다.")
    private String location;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime startAt;

    @NotNull(message = "목표 인원은 필수입니다.")
    @Min(value = 1, message = "목표 인원은 1 이상이어야 합니다.")
    private Integer targetCount;

    private String description;
}
