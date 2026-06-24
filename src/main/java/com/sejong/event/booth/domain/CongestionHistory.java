package com.sejong.event.booth.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "congestion_histories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class CongestionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booth_id", nullable = false)
    private Booth booth;

    private Integer waitingCount;

    @Enumerated(EnumType.STRING)
    private CongestionStatus status;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime recordedAt;

    @Builder
    public CongestionHistory(Booth booth, Integer waitingCount, CongestionStatus status) {
        this.booth = booth;
        this.waitingCount = waitingCount;
        this.status = status;
    }
}
