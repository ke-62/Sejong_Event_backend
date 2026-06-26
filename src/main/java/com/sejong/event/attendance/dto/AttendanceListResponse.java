package com.sejong.event.attendance.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AttendanceListResponse {

    private Integer attendanceCount;
    private Integer targetCount;
    private List<AttendanceLogDto> logs;
}
