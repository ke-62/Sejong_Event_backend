package com.sejong.event.waiting.domain;

import com.sejong.event.booth.domain.Booth;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "waiting_entries",
        indexes = {
                // 대기열 조회·집계: findByBoothIdAndStatusOrderByOrderAsc / countByBoothIdAndStatus
                @Index(name = "idx_waiting_booth_status", columnList = "booth_id, status"),
                // 부스별 순번 조회: findByBoothIdOrderByOrderAsc
                @Index(name = "idx_waiting_booth_order", columnList = "booth_id, wait_order")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class WaitingEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booth_id", nullable = false)
    private Booth booth;

    @Column(name = "wait_order", nullable = false)
    private Integer order;

    private String name;
    private String phone;
    private Integer partySize;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WaitingStatus status;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public WaitingEntry(Booth booth, Integer order, String name, String phone, Integer partySize) {
        this.booth = booth;
        this.order = order;
        this.name = name;
        this.phone = phone;
        this.partySize = partySize;
        this.status = WaitingStatus.WAITING;
    }

    public void call() {
        this.status = WaitingStatus.CALLED;
    }

    public void cancel() {
        this.status = WaitingStatus.CANCELLED;
    }
}
