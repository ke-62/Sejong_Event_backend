package com.sejong.event.attendance.repository;

import com.sejong.event.attendance.domain.AttendanceLog;
import com.sejong.event.attendance.domain.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AttendanceLogRepository extends JpaRepository<AttendanceLog, Long> {

    boolean existsByEventIdAndStudentId(Long eventId, String studentId);

    Optional<AttendanceLog> findByEventIdAndStudentId(Long eventId, String studentId);

    List<AttendanceLog> findByEventIdOrderByCreatedAtDesc(Long eventId);

    List<AttendanceLog> findByEventIdAndStatus(Long eventId, AttendanceStatus status);

    int countByEventIdAndStatus(Long eventId, AttendanceStatus status);

    int countByEventId(Long eventId);

    void deleteByEventId(Long eventId);
}
