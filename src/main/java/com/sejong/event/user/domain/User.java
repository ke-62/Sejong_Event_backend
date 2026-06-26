package com.sejong.event.user.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    // 관리자 콘솔 표시용 평문 비밀번호 (API 명세 요구사항)
    @Column
    private String plainPassword;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String organization;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private Boolean isActive;

    @Builder
    public User(String username, String password, String plainPassword, String name, String organization, Role role) {
        this.username = username;
        this.password = password;
        this.plainPassword = plainPassword;
        this.name = name;
        this.organization = organization;
        this.role = role;
        this.isActive = true;
    }

    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }
}
