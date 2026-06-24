package com.sejong.event.booth.dto;

import com.sejong.event.booth.domain.Booth;
import com.sejong.event.booth.domain.CongestionStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BoothResponse {

    private Long id;
    private Long eventId;
    private String name;
    private String description;
    private String location;
    private String menu;
    private Integer waitingCount;
    private CongestionStatus congestionStatus;
    private Boolean isOpen;
    private String operatorName;

    public static BoothResponse from(Booth booth) {
        return BoothResponse.builder()
                .id(booth.getId())
                .eventId(booth.getEvent().getId())
                .name(booth.getName())
                .description(booth.getDescription())
                .location(booth.getLocation())
                .menu(booth.getMenu())
                .waitingCount(booth.getWaitingCount())
                .congestionStatus(booth.getCongestionStatus())
                .isOpen(booth.getIsOpen())
                .operatorName(booth.getOperator() != null ? booth.getOperator().getName() : null)
                .build();
    }
}
