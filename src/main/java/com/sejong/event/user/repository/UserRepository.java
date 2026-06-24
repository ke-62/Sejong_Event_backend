package com.sejong.event.user.repository;

import com.sejong.event.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByStudentId(String studentId);

    boolean existsByStudentId(String studentId);
}
