package com.sejong.event.booth.dto;

import lombok.Getter;

@Getter
public class RejectApplicationRequest {

    private String reason; // 반려 사유 (선택)
}
