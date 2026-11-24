package com.sejong.event.performance;

import com.sejong.event.attendance.service.AttendanceService;
import com.sejong.event.config.CacheConfig;
import com.sejong.event.event.service.EventService;
import com.sejong.event.festival.service.FestivalService;
import com.sejong.event.support.TestRequests;
import com.sejong.event.user.domain.Role;
import com.sejong.event.user.domain.User;
import com.sejong.event.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CachingIntegrationTest {

    @Autowired
    private FestivalService festivalService;
    @Autowired
    private EventService eventService;
    @Autowired
    private AttendanceService attendanceService;
    @Autowired
    private CacheManager cacheManager;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("축제 상태는 캐시되고 토글 시 무효화된다")
    void festivalStatusCachedAndEvicted() {
        Cache cache = cacheManager.getCache(CacheConfig.FESTIVAL_STATUS);
        assertThat(cache).isNotNull();
        cache.clear();

        festivalService.getStatus();
        assertThat(cache.get(SimpleKey.EMPTY)).as("첫 조회 후 캐시 적재").isNotNull();

        festivalService.getStatus(); // 두 번째 조회는 캐시 히트 (레포 미조회)

        festivalService.updateStatus(TestRequests.festivalStatus(true));
        assertThat(cache.get(SimpleKey.EMPTY)).as("토글 후 무효화").isNull();
    }

    @Test
    @DisplayName("행사 목록은 사용자별로 캐시되고 행사 생성 시 전체 무효화된다")
    void eventListCachedAndEvictedOnCreate() {
        Cache cache = cacheManager.getCache(CacheConfig.EVENT_LIST);
        assertThat(cache).isNotNull();
        cache.clear();

        eventService.getEvents(null);
        assertThat(cache.get("anonymous")).as("비로그인 목록 캐시").isNotNull();

        User organizer = userRepository.save(User.builder()
                .username("perf-organizer")
                .password(passwordEncoder.encode("pw"))
                .name("운영진")
                .organization("성능테스트학생회")
                .role(Role.ORGANIZER)
                .build());

        eventService.createEvent(organizer.getId(), TestRequests.createEvent("행사", "장소", 10));
        assertThat(cache.get("anonymous")).as("행사 생성 후 전체 무효화").isNull();
    }

    @Test
    @DisplayName("행사 단건은 id별로 캐시된다")
    void eventDetailCachedById() {
        Cache cache = cacheManager.getCache(CacheConfig.EVENTS);
        assertThat(cache).isNotNull();
        cache.clear();

        User organizer = userRepository.save(User.builder()
                .username("perf-organizer2")
                .password(passwordEncoder.encode("pw"))
                .name("운영진")
                .organization("성능테스트학생회2")
                .role(Role.ORGANIZER)
                .build());
        Long eventId = eventService.createEvent(organizer.getId(),
                TestRequests.createEvent("행사", "장소", 10)).getId();

        eventService.getEvent(eventId);
        assertThat(cache.get(eventId)).isNotNull();
    }

    @Test
    @DisplayName("입장 처리 후 행사 단건·목록 캐시가 무효화되어 입장자 수가 갱신된다")
    void eventCacheEvictedOnAttendance() {
        cacheManager.getCache(CacheConfig.EVENTS).clear();
        cacheManager.getCache(CacheConfig.EVENT_LIST).clear();

        User organizer = userRepository.save(User.builder()
                .username("perf-organizer3")
                .password(passwordEncoder.encode("pw"))
                .name("운영진")
                .organization("성능테스트학생회3")
                .role(Role.ORGANIZER)
                .build());
        Long eventId = eventService.createEvent(organizer.getId(),
                TestRequests.createEvent("행사", "장소", 10)).getId();

        // 캐시 적재 (입장자 0명)
        assertThat(eventService.getEvent(eventId).getAttendanceCount()).isZero();
        assertThat(attendanceCountInList(organizer.getId(), eventId)).isZero();

        attendanceService.manualIssue(eventId, TestRequests.manualTicket("20260001", "김세종"));

        assertThat(eventService.getEvent(eventId).getAttendanceCount()).as("단건 갱신").isEqualTo(1);
        assertThat(attendanceCountInList(organizer.getId(), eventId)).as("목록 갱신").isEqualTo(1);
    }

    private int attendanceCountInList(Long userId, Long eventId) {
        return eventService.getEvents(userId).stream()
                .filter(e -> e.getId().equals(eventId))
                .findFirst().orElseThrow()
                .getAttendanceCount();
    }
}
