package com.sejong.event.user.domain;

public enum Role {
    BOOTH_OPERATOR,  // 부스 운영자 (학번 + 비밀번호로 로그인, 관리자 승인 후 계정 생성)
    ADMIN            // 관리자 (축제 활성화, 부스 신청 승인/반려)
}
