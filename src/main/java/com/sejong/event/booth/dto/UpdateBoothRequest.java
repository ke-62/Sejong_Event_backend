package com.sejong.event.booth.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;

@Getter
public class UpdateBoothRequest {

    private String name;
    private String category;
    private String location;

    @DecimalMin(value = "37.540", message = "캠퍼스 범위를 벗어난 위도입니다.")
    @DecimalMax(value = "37.562", message = "캠퍼스 범위를 벗어난 위도입니다.")
    private Double latitude;

    @DecimalMin(value = "127.063", message = "캠퍼스 범위를 벗어난 경도입니다.")
    @DecimalMax(value = "127.084", message = "캠퍼스 범위를 벗어난 경도입니다.")
    private Double longitude;
    private String emoji;
    private String description;
    private String organization; // superuser 전용
}
