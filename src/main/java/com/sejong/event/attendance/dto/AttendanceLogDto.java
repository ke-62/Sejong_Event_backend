package com.sejong.event.attendance.dto;

import com.sejong.event.attendance.domain.AttendanceLog;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AttendanceLogDto {

    private Long id;
    private String studentId;
    private String name;
    private String department;
    private String gate;
    private String status;
    private LocalDateTime time;

    public static AttendanceLogDto from(AttendanceLog log) {
        return AttendanceLogDto.builder()
                .id(log.getId())
                .studentId(log.getStudentId())
                .name(log.getName())
                .department(log.getDepartment())
                .gate(log.getGate())
                .status(log.getStatus().name().toLowerCase())
                .time(log.getCreatedAt())
                .build();
    }
}
