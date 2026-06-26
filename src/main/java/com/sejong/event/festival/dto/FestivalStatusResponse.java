package com.sejong.event.festival.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FestivalStatusResponse {

    private Boolean isActive;
}
