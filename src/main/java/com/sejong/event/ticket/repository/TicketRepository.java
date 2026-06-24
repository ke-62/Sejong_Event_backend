package com.sejong.event.ticket.repository;

import com.sejong.event.ticket.domain.Ticket;
import com.sejong.event.ticket.domain.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByEventIdAndStudentId(Long eventId, String studentId);

    boolean existsByEventIdAndStudentId(Long eventId, String studentId);

    List<Ticket> findByEventId(Long eventId);

    List<Ticket> findByEventIdAndStatus(Long eventId, TicketStatus status);

    int countByEventId(Long eventId);

    int countByEventIdAndIsEntered(Long eventId, Boolean isEntered);
}
