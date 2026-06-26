package com.sejong.event.waiting.repository;

import com.sejong.event.waiting.domain.WaitingEntry;
import com.sejong.event.waiting.domain.WaitingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WaitingRepository extends JpaRepository<WaitingEntry, Long> {

    List<WaitingEntry> findByBoothIdOrderByOrderAsc(Long boothId);

    List<WaitingEntry> findByBoothIdAndStatusOrderByOrderAsc(Long boothId, WaitingStatus status);

    Optional<WaitingEntry> findFirstByBoothIdAndStatusOrderByOrderAsc(Long boothId, WaitingStatus status);

    int countByBoothIdAndStatus(Long boothId, WaitingStatus status);

    int countByBoothId(Long boothId);

    Optional<WaitingEntry> findFirstByBoothIdAndStatusOrderByOrderDesc(Long boothId, WaitingStatus status);

    int countByBoothIdAndStatusAndOrderLessThan(Long boothId, WaitingStatus status, Integer order);
}
