package com.sejong.event.user.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 로그인 식별자 — 부스 운영자는 학번, 관리자는 관리자 ID */
    @Column(unique = true, nullable = false)
    private String studentId;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public User(String studentId, String password, String name, Role role) {
        this.studentId = studentId;
        this.password = password;
        this.name = name;
        this.role = role;
    }

    public void updateRole(Role role) {
        this.role = role;
    }

    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }
}
