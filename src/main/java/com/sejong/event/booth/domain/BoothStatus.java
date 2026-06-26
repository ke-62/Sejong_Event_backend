package com.sejong.event.booth.domain;

public enum BoothStatus {
    PENDING,   // 승인 대기
    APPROVED,  // 승인 (목록 노출)
    REJECTED   // 거부
}
