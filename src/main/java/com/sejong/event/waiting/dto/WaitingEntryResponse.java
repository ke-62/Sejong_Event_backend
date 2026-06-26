package com.sejong.event.waiting.dto;

import com.sejong.event.waiting.domain.WaitingEntry;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WaitingEntryResponse {

    private Long id;
    private Integer order;
    private String name;
    private String phone;
    private Integer partySize;
    private String status;

    public static WaitingEntryResponse from(WaitingEntry entry) {
        return WaitingEntryResponse.builder()
                .id(entry.getId())
                .order(entry.getOrder())
                .name(entry.getName())
                .phone(entry.getPhone())
                .partySize(entry.getPartySize())
                .status(entry.getStatus().name())
                .build();
    }
}
