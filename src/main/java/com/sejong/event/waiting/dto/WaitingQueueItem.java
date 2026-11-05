package com.sejong.event.waiting.dto;

import com.sejong.event.waiting.domain.WaitingEntry;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WaitingQueueItem {

    private int order;
    private String name;
    private String phoneTail;
    private int partySize;

    public static WaitingQueueItem from(WaitingEntry entry) {
        return WaitingQueueItem.builder()
                .order(entry.getOrder())
                .name(maskName(entry.getName()))
                .phoneTail(phoneTail(entry.getPhone()))
                .partySize(entry.getPartySize())
                .build();
    }

    // 인증 없이 공개되는 대기열이라 이름 중간과 전화번호 앞자리는 절대 내보내지 않는다.
    private static String maskName(String name) {
        if (name == null || name.isBlank()) return name;
        int length = name.length();
        if (length == 1) return name;
        if (length == 2) return name.charAt(0) + "x";
        return name.charAt(0) + "x".repeat(length - 2) + name.charAt(length - 1);
    }

    private static String phoneTail(String phone) {
        if (phone == null) return null;
        String digits = phone.replaceAll("\\D", "");
        return digits.length() <= 4 ? digits : digits.substring(digits.length() - 4);
    }
}
