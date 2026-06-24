package com.sejong.event.booth.repository;

import com.sejong.event.booth.domain.Booth;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoothRepository extends JpaRepository<Booth, Long> {

    List<Booth> findByEventId(Long eventId);

    List<Booth> findByOperatorId(Long operatorId);
}
