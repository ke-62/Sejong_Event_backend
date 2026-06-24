package com.sejong.event.booth.dto;

import com.sejong.event.booth.domain.ApplicationStatus;
import com.sejong.event.booth.domain.BoothApplication;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class BoothApplicationResponse {

    private Long id;
    private Long eventId;
    private String eventTitle;
    private String applicantName;
    private String studentId;
    private String boothName;
    private String description;
    private String location;
    private String menu;
    private ApplicationStatus status;
    private Long approvedBoothId;
    private String rejectReason;
    private LocalDateTime createdAt;

    public static BoothApplicationResponse from(BoothApplication app) {
        return BoothApplicationResponse.builder()
                .id(app.getId())
                .eventId(app.getEvent().getId())
                .eventTitle(app.getEvent().getTitle())
                .applicantName(app.getApplicantName())
                .studentId(app.getStudentId())
                .boothName(app.getBoothName())
                .description(app.getDescription())
                .location(app.getLocation())
                .menu(app.getMenu())
                .status(app.getStatus())
                .approvedBoothId(app.getApprovedBoothId())
                .rejectReason(app.getRejectReason())
                .createdAt(app.getCreatedAt())
                .build();
    }
}
