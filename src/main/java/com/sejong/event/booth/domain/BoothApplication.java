package com.sejong.event.booth.domain;

import com.sejong.event.event.domain.Event;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 축제 부스 신청 엔티티.
 * 관리자가 승인하면 User(BOOTH_OPERATOR) + Booth 가 생성되고 status = APPROVED 로 변경된다.
 */
@Entity
@Table(name = "booth_applications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class BoothApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event; // 어느 축제에 신청하는지

    @Column(nullable = false)
    private String applicantName; // 신청자 이름 (부스 운영자 이름)

    @Column(nullable = false)
    private String studentId; // 학번 → 승인 시 로그인 ID로 사용

    @Column(nullable = false)
    private String password; // 원하는 비밀번호 (승인 시 bcrypt 해시 후 계정 생성)

    @Column(nullable = false)
    private String boothName;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String location;

    private String menu;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status;

    /** 승인 시 생성된 부스 ID (참조용) */
    private Long approvedBoothId;

    @Column(columnDefinition = "TEXT")
    private String rejectReason; // 반려 사유

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public BoothApplication(Event event, String applicantName, String studentId,
                            String password, String boothName, String description,
                            String location, String menu) {
        this.event = event;
        this.applicantName = applicantName;
        this.studentId = studentId;
        this.password = password;
        this.boothName = boothName;
        this.description = description;
        this.location = location;
        this.menu = menu;
        this.status = ApplicationStatus.PENDING;
    }

    public void approve(Long boothId) {
        this.status = ApplicationStatus.APPROVED;
        this.approvedBoothId = boothId;
    }

    public void reject(String reason) {
        this.status = ApplicationStatus.REJECTED;
        this.rejectReason = reason;
    }
}
