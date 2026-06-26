package com.sejong.event.attendance.domain;

import com.sejong.event.event.domain.Event;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_logs",
        uniqueConstraints = @UniqueConstraint(columnNames = {"event_id", "student_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class AttendanceLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(nullable = false)
    private String studentId;

    @Column(nullable = false)
    private String name;

    private String department;

    @Column(nullable = false)
    private Integer ticketNumber; // 1부터 순차 발급

    private String gate; // QR 토큰 기반 서버 설정

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceStatus status;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public AttendanceLog(Event event, String studentId, String name, String department,
                         Integer ticketNumber, String gate, AttendanceStatus status) {
        this.event = event;
        this.studentId = studentId;
        this.name = name;
        this.department = department;
        this.ticketNumber = ticketNumber;
        this.gate = gate;
        this.status = status;
    }
}
