package com.sejong.event.festival.service;

import com.sejong.event.festival.domain.FestivalSetting;
import com.sejong.event.festival.dto.FestivalStatusResponse;
import com.sejong.event.festival.dto.UpdateFestivalStatusRequest;
import com.sejong.event.festival.repository.FestivalSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FestivalService {

    private final FestivalSettingRepository festivalSettingRepository;

    public FestivalStatusResponse getStatus() {
        FestivalSetting setting = getOrCreate();
        return FestivalStatusResponse.builder().isActive(setting.getIsActive()).build();
    }

    @Transactional
    public FestivalStatusResponse updateStatus(UpdateFestivalStatusRequest request) {
        FestivalSetting setting = getOrCreate();
        setting.setActive(request.getIsActive());
        festivalSettingRepository.save(setting);
        return FestivalStatusResponse.builder().isActive(setting.getIsActive()).build();
    }

    private FestivalSetting getOrCreate() {
        return festivalSettingRepository.findById(1L)
                .orElseGet(() -> festivalSettingRepository.save(FestivalSetting.defaultSetting()));
    }
}
