package com.sejong.event.event.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventMode mode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status;

    private Integer maxCapacity; // 정족수 (행사 모드)

    private Integer currentCount; // 현재 참여자 수

    /**
     * 행사 모드 전용 관리 토큰 (UUID).
     * 로그인 없이 방을 만든 사람만 관리할 수 있도록 발급.
     * 프론트에서 localStorage에 보관하여 추첨·종료 시 헤더로 전달.
     */
    @Column(unique = true)
    private String managerToken;

    private LocalDateTime startAt;

    private LocalDateTime endAt;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Event(String title, String description, EventMode mode,
                 Integer maxCapacity, String managerToken,
                 LocalDateTime startAt, LocalDateTime endAt) {
        this.title = title;
        this.description = description;
        this.mode = mode;
        this.status = EventStatus.SCHEDULED;
        this.maxCapacity = maxCapacity;
        this.currentCount = 0;
        this.managerToken = managerToken;
        this.startAt = startAt;
        this.endAt = endAt;
    }

    public void activate() {
        this.status = EventStatus.ACTIVE;
    }

    public void close() {
        this.status = EventStatus.CLOSED;
    }

    public void incrementCount() {
        this.currentCount++;
    }

    public boolean isManagerToken(String token) {
        return this.managerToken != null && this.managerToken.equals(token);
    }
}
