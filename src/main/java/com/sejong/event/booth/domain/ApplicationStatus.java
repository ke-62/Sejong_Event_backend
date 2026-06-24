package com.sejong.event.booth.domain;

public enum ApplicationStatus {
    PENDING,   // 신청 대기
    APPROVED,  // 승인 완료 → 계정 생성됨
    REJECTED   // 반려
}
