package com.sejong.event.attendance.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ScanResponse {

    private String status;       // "success" | "duplicate" | "error"
    private Integer ticketNumber;
    private String studentId;
    private String name;
    private Integer attendanceCount;
}
