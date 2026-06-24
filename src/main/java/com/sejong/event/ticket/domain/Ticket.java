package com.sejong.event.ticket.domain;

import com.sejong.event.event.domain.Event;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "tickets",
        uniqueConstraints = @UniqueConstraint(columnNames = {"event_id", "student_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(nullable = false)
    private String studentId; // 학번

    private String studentName;

    @Column(unique = true, nullable = false)
    private String ticketNumber; // 발급 고유 번호 (예: EVENT-001-0042)

    @Column(length = 1024)
    private String qrToken; // 서버 서명된 JWT QR 토큰

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status;

    private Boolean isEntered;

    private LocalDateTime enteredAt;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime issuedAt;

    @Builder
    public Ticket(Event event, String studentId, String studentName,
                  String ticketNumber, String qrToken) {
        this.event = event;
        this.studentId = studentId;
        this.studentName = studentName;
        this.ticketNumber = ticketNumber;
        this.qrToken = qrToken;
        this.status = TicketStatus.ISSUED;
        this.isEntered = false;
    }

    public void enter() {
        this.status = TicketStatus.ENTERED;
        this.isEntered = true;
        this.enteredAt = LocalDateTime.now();
    }

    public void markAsDrawn() {
        this.status = TicketStatus.DRAWN;
    }

    public void updateQrToken(String qrToken) {
        this.qrToken = qrToken;
    }
}
