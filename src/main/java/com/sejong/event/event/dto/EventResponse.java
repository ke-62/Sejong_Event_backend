package com.sejong.event.event.dto;

import com.sejong.event.event.domain.Event;
import com.sejong.event.event.domain.EventMode;
import com.sejong.event.event.domain.EventStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class EventResponse {

    private Long id;
    private String title;
    private String description;
    private EventMode mode;
    private EventStatus status;
    private Integer maxCapacity;
    private Integer currentCount;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private LocalDateTime createdAt;

    public static EventResponse from(Event event) {
        return EventResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .mode(event.getMode())
                .status(event.getStatus())
                .maxCapacity(event.getMaxCapacity())
                .currentCount(event.getCurrentCount())
                .startAt(event.getStartAt())
                .endAt(event.getEndAt())
                .createdAt(event.getCreatedAt())
                .build();
    }
}
