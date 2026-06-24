package com.sejong.event.booth.domain;

import com.sejong.event.event.domain.Event;
import com.sejong.event.user.domain.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "booths")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Booth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id")
    private User operator;

    @Column(nullable = false)
    private String name;

    private String description;

    private String location; // 위치

    private String menu; // 판매 메뉴

    private Integer waitingCount; // 대기 인원 수

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CongestionStatus congestionStatus;

    @Column(nullable = false)
    private Boolean isOpen;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Builder
    public Booth(Event event, User operator, String name,
                 String description, String location, String menu) {
        this.event = event;
        this.operator = operator;
        this.name = name;
        this.description = description;
        this.location = location;
        this.menu = menu;
        this.waitingCount = 0;
        this.congestionStatus = CongestionStatus.LOW;
        this.isOpen = true;
    }

    public void updateCongestion(int waitingCount) {
        this.waitingCount = waitingCount;
        if (waitingCount < 10) {
            this.congestionStatus = CongestionStatus.LOW;
        } else if (waitingCount < 30) {
            this.congestionStatus = CongestionStatus.MEDIUM;
        } else {
            this.congestionStatus = CongestionStatus.HIGH;
        }
    }

    public void toggleOpen() {
        this.isOpen = !this.isOpen;
    }

    public void updateInfo(String name, String description, String location, String menu) {
        if (name != null) this.name = name;
        if (description != null) this.description = description;
        if (location != null) this.location = location;
        if (menu != null) this.menu = menu;
    }

    public boolean isOperator(Long userId) {
        return this.operator != null && this.operator.getId().equals(userId);
    }
}
