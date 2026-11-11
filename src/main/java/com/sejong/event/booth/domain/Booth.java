package com.sejong.event.booth.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "booths",
        indexes = {
                // 공개 목록: findByStatusOrderByWaitingDesc
                @Index(name = "idx_booth_status_waiting", columnList = "status, waiting"),
                // 카테고리 필터: findByCategoryAndStatusOrderByWaitingDesc
                @Index(name = "idx_booth_category_status", columnList = "category, status"),
                // 내 기관 부스: findByOrganization
                @Index(name = "idx_booth_organization", columnList = "organization")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Booth {

    private static final List<String> GRADIENT_PRESETS = List.of(
            "from-[#FDE68A] to-[#FCD34D]",
            "from-[#BBF7D0] to-[#86EFAC]",
            "from-[#BAE6FD] to-[#7DD3FC]",
            "from-[#FCA5A5] to-[#F87171]",
            "from-[#DDD6FE] to-[#A78BFA]",
            "from-[#FED7AA] to-[#FB923C]"
    );

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String category; // 주점, 체험

    private String location;

    private Double latitude;

    private Double longitude;

    private String emoji;

    private String gradient;

    @Column(nullable = false)
    private Integer waiting;

    @Column(nullable = false)
    private String organization;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BoothStatus status;

    @Column(nullable = false)
    private Boolean isOpen;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Builder
    public Booth(String name, String category, String location, Double latitude, Double longitude,
                 String emoji, String organization, String description, int createdIndex) {
        this.name = name;
        this.category = category;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
        this.emoji = emoji;
        this.organization = organization;
        this.description = description;
        this.gradient = GRADIENT_PRESETS.get(createdIndex % GRADIENT_PRESETS.size());
        this.waiting = 0;
        this.status = BoothStatus.PENDING;
        this.isOpen = true;
    }

    public void approve() {
        this.status = BoothStatus.APPROVED;
    }

    public void reject() {
        this.status = BoothStatus.REJECTED;
    }

    public void updateStatus(boolean isOpen) {
        this.isOpen = isOpen;
    }

    public void updateInfo(String name, String category, String location, String emoji,
                           String description, String organization) {
        if (name != null) this.name = name;
        if (category != null) this.category = category;
        if (location != null) this.location = location;
        if (emoji != null) this.emoji = emoji;
        if (description != null) this.description = description;
        if (organization != null) this.organization = organization;
    }

    public void updateCoordinates(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) return;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void setWaiting(int waiting) {
        this.waiting = waiting;
    }

    public boolean isManagedBy(String organization) {
        return this.organization.equals(organization);
    }
}
