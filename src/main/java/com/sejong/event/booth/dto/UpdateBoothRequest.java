package com.sejong.event.booth.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class UpdateBoothRequest {

    private String name;
    private String category;
    @Size(max = 100, message = "위치 설명은 100자 이하로 입력해주세요.")
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
