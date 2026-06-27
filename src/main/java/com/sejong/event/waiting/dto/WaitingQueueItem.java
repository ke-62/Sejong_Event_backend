package com.sejong.event.waiting.dto;

import com.sejong.event.waiting.domain.WaitingEntry;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WaitingQueueItem {

    private int order;
    private String name;
    private int partySize;

    public static WaitingQueueItem from(WaitingEntry entry) {
        return WaitingQueueItem.builder()
                .order(entry.getOrder())
                .name(entry.getName())
                .partySize(entry.getPartySize())
                .build();
    }
}
