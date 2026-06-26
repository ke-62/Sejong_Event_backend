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
    private String name;

    @Column(nullable = false)
    private String organization;

    private String location;

    private LocalDateTime startAt;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer targetCount;

    @Column(nullable = false)
    private Integer attendanceCount;

    @Column(nullable = false)
    private Boolean isActive;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Event(String name, String organization, String location, LocalDateTime startAt,
                 String description, Integer targetCount) {
        this.name = name;
        this.organization = organization;
        this.location = location;
        this.startAt = startAt;
        this.description = description;
        this.targetCount = targetCount;
        this.attendanceCount = 0;
        this.isActive = true;
    }

    public void update(String name, String organization, String location,
                       LocalDateTime startAt, Integer targetCount) {
        if (name != null) this.name = name;
        if (organization != null) this.organization = organization;
        if (location != null) this.location = location;
        if (startAt != null) this.startAt = startAt;
        if (targetCount != null) this.targetCount = targetCount;
    }

    public void incrementAttendance() {
        this.attendanceCount++;
    }
}
