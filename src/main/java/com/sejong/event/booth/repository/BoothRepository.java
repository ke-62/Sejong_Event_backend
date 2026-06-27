package com.sejong.event.booth.repository;

import com.sejong.event.booth.domain.Booth;
import com.sejong.event.booth.domain.BoothStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoothRepository extends JpaRepository<Booth, Long> {

    List<Booth> findByStatusOrderByWaitingDesc(BoothStatus status);

    List<Booth> findByCategoryAndStatusOrderByWaitingDesc(String category, BoothStatus status);

    List<Booth> findByOrganization(String organization);

    int countByStatus(BoothStatus status);

    List<Booth> findAllByOrderByStatusAscIdDesc();
}
