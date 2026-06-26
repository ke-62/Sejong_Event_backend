package com.sejong.event.waiting.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WaitingRegisterResponse {

    private Long id;
    private Integer order;
    private String name;
    private Integer partySize;
    private String status;
    private Integer waitingAhead;
    private Double estimatedMinutes;
}
