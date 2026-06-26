package com.sejong.event.festival.repository;

import com.sejong.event.festival.domain.FestivalSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FestivalSettingRepository extends JpaRepository<FestivalSetting, Long> {
}
