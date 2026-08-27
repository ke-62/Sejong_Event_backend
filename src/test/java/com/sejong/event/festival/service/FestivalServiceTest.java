package com.sejong.event.festival.service;

import com.sejong.event.festival.domain.FestivalSetting;
import com.sejong.event.festival.dto.FestivalStatusResponse;
import com.sejong.event.festival.repository.FestivalSettingRepository;
import com.sejong.event.support.TestRequests;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class FestivalServiceTest {

    @Mock
    private FestivalSettingRepository festivalSettingRepository;
    @InjectMocks
    private FestivalService festivalService;

    @Test
    @DisplayName("설정이 있으면 현재 활성 여부를 반환한다")
    void getStatusWhenExists() {
        FestivalSetting setting = FestivalSetting.defaultSetting();
        setting.setActive(true);
        given(festivalSettingRepository.findById(1L)).willReturn(Optional.of(setting));

        FestivalStatusResponse response = festivalService.getStatus();

        assertThat(response.getIsActive()).isTrue();
    }

    @Test
    @DisplayName("설정이 없으면 기본값(비활성)을 만들어 저장하고 반환한다")
    void getStatusCreatesDefault() {
        given(festivalSettingRepository.findById(1L)).willReturn(Optional.empty());
        given(festivalSettingRepository.save(any(FestivalSetting.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        FestivalStatusResponse response = festivalService.getStatus();

        assertThat(response.getIsActive()).isFalse();
        then(festivalSettingRepository).should().save(any(FestivalSetting.class));
    }

    @Test
    @DisplayName("축제 상태를 토글하면 저장하고 바뀐 값을 반환한다")
    void updateStatus() {
        FestivalSetting setting = FestivalSetting.defaultSetting(); // isActive = false
        given(festivalSettingRepository.findById(1L)).willReturn(Optional.of(setting));
        given(festivalSettingRepository.save(any(FestivalSetting.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        FestivalStatusResponse response = festivalService.updateStatus(TestRequests.festivalStatus(true));

        assertThat(response.getIsActive()).isTrue();
        assertThat(setting.getIsActive()).isTrue();
    }
}
