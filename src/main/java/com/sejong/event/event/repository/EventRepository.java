package com.sejong.event.event.repository;

import com.sejong.event.event.domain.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByIsActiveTrue();

    List<Event> findByOrganization(String organization);
}
