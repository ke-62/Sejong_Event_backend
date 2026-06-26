package com.sejong.event.booth.dto;

import com.sejong.event.booth.domain.Booth;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BoothResponse {

    private Long id;
    private String name;
    private String category;
    private String location;
    private String emoji;
    private String gradient;
    private Integer waiting;
    private String organization;
    private String description;
    private String status;
    private Boolean isOpen;

    // 상세 조회용 추가 필드
    private Double avgMinutes;

    public static BoothResponse from(Booth booth) {
        return BoothResponse.builder()
                .id(booth.getId())
                .name(booth.getName())
                .category(booth.getCategory())
                .location(booth.getLocation())
                .emoji(booth.getEmoji())
                .gradient(booth.getGradient())
                .waiting(booth.getWaiting())
                .organization(booth.getOrganization())
                .description(booth.getDescription())
                .status(booth.getStatus().name().toLowerCase())
                .isOpen(booth.getIsOpen())
                .build();
    }

    public static BoothResponse withAvgMinutes(Booth booth, double avgMinutes) {
        BoothResponse response = from(booth);
        return BoothResponse.builder()
                .id(response.id)
                .name(response.name)
                .category(response.category)
                .location(response.location)
                .emoji(response.emoji)
                .gradient(response.gradient)
                .waiting(response.waiting)
                .organization(response.organization)
                .description(response.description)
                .status(response.status)
                .isOpen(response.isOpen)
                .avgMinutes(avgMinutes)
                .build();
    }
}
