package com.sejong.event.booth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class CreateBoothRequest {

    @NotBlank(message = "부스명은 필수입니다.")
    private String name;

    @NotBlank(message = "카테고리는 필수입니다.")
    private String category;

    @NotBlank(message = "위치는 필수입니다.")
    private String location;

    private String emoji;

    private String description;
}
