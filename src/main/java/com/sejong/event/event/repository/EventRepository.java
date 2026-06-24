package com.sejong.event.event.repository;

import com.sejong.event.event.domain.Event;
import com.sejong.event.event.domain.EventMode;
import com.sejong.event.event.domain.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByStatus(EventStatus status);

    List<Event> findByMode(EventMode mode);

    List<Event> findByModeAndStatus(EventMode mode, EventStatus status);
}
