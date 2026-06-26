package com.sejong.event.waiting.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WaitingStatusResponse {

    private Long id;
    private Integer myOrder;
    private Integer calledOrder;
    private Integer ahead;
    private Double estimatedMinutes;
    private String status;
}
