package com.sejong.event.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sejong.event.event.domain.Event;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class EventResponse {

    private Long id;
    private String name;
    private String organization;
    private String location;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime startAt;

    private Integer attendanceCount;
    private Integer targetCount;
    private Boolean isActive;

    public static EventResponse from(Event event) {
        return EventResponse.builder()
                .id(event.getId())
                .name(event.getName())
                .organization(event.getOrganization())
                .location(event.getLocation())
                .startAt(event.getStartAt())
                .attendanceCount(event.getAttendanceCount())
                .targetCount(event.getTargetCount())
                .isActive(event.getIsActive())
                .build();
    }
}
