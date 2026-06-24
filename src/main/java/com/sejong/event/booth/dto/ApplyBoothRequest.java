package com.sejong.event.booth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class ApplyBoothRequest {

    @NotBlank(message = "신청자 이름은 필수입니다.")
    private String applicantName;

    @NotBlank(message = "학번은 필수입니다.")
    private String studentId;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 4, message = "비밀번호는 4자 이상이어야 합니다.")
    private String password; // 승인 시 이 비밀번호로 계정 생성

    @NotBlank(message = "부스 이름은 필수입니다.")
    private String boothName;

    private String description;

    private String location;

    private String menu;
}
