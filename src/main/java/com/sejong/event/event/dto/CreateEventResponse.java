package com.sejong.event.event.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 행사 생성 응답 DTO.
 * managerToken 은 프론트에서 localStorage 에 보관하여
 * 추첨·종료 요청 시 X-Manager-Token 헤더로 전달한다.
 */
@Getter
@Builder
public class CreateEventResponse {

    private Long eventId;
    private String title;
    private Integer maxCapacity;
    private String qrUrl;         // 참여자용 QR 링크 (프론트에서 QR 이미지 생성)
    private String managerToken;  // 주최자 전용 관리 토큰 — 반드시 안전하게 보관!
}
