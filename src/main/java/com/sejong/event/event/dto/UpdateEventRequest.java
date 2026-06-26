package com.sejong.event.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UpdateEventRequest {

    private String name;
    private String organization; // superuser 전용
    private String location;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime startAt;

    private Integer targetCount;
}
