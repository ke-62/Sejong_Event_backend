package com.sejong.event.booth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;

@Getter
public class CreateBoothRequest {

    @NotBlank(message = "부스명은 필수입니다.")
    private String name;

    @NotBlank(message = "카테고리는 필수입니다.")
    private String category;

    @Size(max = 100, message = "위치 설명은 100자 이하로 입력해주세요.")
    private String location; // 자유 설명 (예: 대양홀 앞 잔디)

    @NotNull(message = "지도에서 부스 위치를 선택해주세요.")
    @DecimalMin(value = "37.540", message = "캠퍼스 범위를 벗어난 위도입니다.")
    @DecimalMax(value = "37.562", message = "캠퍼스 범위를 벗어난 위도입니다.")
    private Double latitude;

    @NotNull(message = "지도에서 부스 위치를 선택해주세요.")
    @DecimalMin(value = "127.063", message = "캠퍼스 범위를 벗어난 경도입니다.")
    @DecimalMax(value = "127.084", message = "캠퍼스 범위를 벗어난 경도입니다.")
    private Double longitude;

    private String emoji;

    private String description;
}
