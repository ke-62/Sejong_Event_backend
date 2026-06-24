package com.sejong.event.user.repository;

import com.sejong.event.user.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByUserId(Long userId);

    Optional<RefreshToken> findByToken(String token);

    void deleteByUserId(Long userId);
}
