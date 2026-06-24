package com.sejong.event.ticket.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QrScanResponse {

    public enum ScanResult {
        SUCCESS,   // 정상 입장
        DUPLICATE, // 중복 입장
        INVALID    // 위조·유효하지 않은 QR
    }

    private ScanResult result;
    private String message;
    private String studentId;
    private String ticketNumber;
    private Integer totalEntered; // 현재 누적 입장자 수
}
