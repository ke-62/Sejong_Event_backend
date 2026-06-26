package com.sejong.event.festival.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "festival_settings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FestivalSetting {

    @Id
    private Long id; // 항상 1 (싱글턴)

    @Column(nullable = false)
    private Boolean isActive;

    public static FestivalSetting defaultSetting() {
        FestivalSetting setting = new FestivalSetting();
        setting.id = 1L;
        setting.isActive = false;
        return setting;
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }
}
