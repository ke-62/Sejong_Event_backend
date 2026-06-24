package com.sejong.event.booth.repository;

import com.sejong.event.booth.domain.ApplicationStatus;
import com.sejong.event.booth.domain.BoothApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoothApplicationRepository extends JpaRepository<BoothApplication, Long> {

    List<BoothApplication> findByEventId(Long eventId);

    List<BoothApplication> findByStatus(ApplicationStatus status);

    boolean existsByEventIdAndStudentId(Long eventId, String studentId);
}
