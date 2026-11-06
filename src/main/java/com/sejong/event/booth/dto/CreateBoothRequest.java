package com.sejong.event.booth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;

@Getter
public class CreateBoothRequest {

    @NotBlank(message = "부스명은 필수입니다.")
    private String name;

    @NotBlank(message = "카테고리는 필수입니다.")
    private String category;

    @NotBlank(message = "위치는 필수입니다.")
    private String location;

    @DecimalMin(value = "37.540", message = "캠퍼스 범위를 벗어난 위도입니다.")
    @DecimalMax(value = "37.562", message = "캠퍼스 범위를 벗어난 위도입니다.")
    private Double latitude;

    @DecimalMin(value = "127.063", message = "캠퍼스 범위를 벗어난 경도입니다.")
    @DecimalMax(value = "127.084", message = "캠퍼스 범위를 벗어난 경도입니다.")
    private Double longitude;

    private String emoji;

    private String description;
}
