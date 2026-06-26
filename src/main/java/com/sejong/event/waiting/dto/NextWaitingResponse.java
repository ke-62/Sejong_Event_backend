package com.sejong.event.waiting.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NextWaitingResponse {

    private Long calledId;
    private Integer remainingCount;
}
