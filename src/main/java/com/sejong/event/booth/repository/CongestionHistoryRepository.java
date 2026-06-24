package com.sejong.event.booth.repository;

import com.sejong.event.booth.domain.CongestionHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CongestionHistoryRepository extends JpaRepository<CongestionHistory, Long> {

    List<CongestionHistory> findByBoothIdOrderByRecordedAtDesc(Long boothId);
}
