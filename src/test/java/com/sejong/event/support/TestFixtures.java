package com.sejong.event.support;

import com.sejong.event.booth.domain.Booth;
import com.sejong.event.event.domain.Event;
import com.sejong.event.user.domain.Role;
import com.sejong.event.user.domain.User;
import com.sejong.event.waiting.domain.WaitingEntry;
import com.sejong.event.waiting.domain.WaitingStatus;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * 서비스 단위 테스트에서 재사용하는 도메인 객체 빌더.
 * 엔티티 id는 실제 운영에서 DB가 채우므로, 테스트에서는 리플렉션으로 채운다.
 */
public final class TestFixtures {

    private TestFixtures() {
    }

    public static User organizer(String organization) {
        User user = User.builder()
                .username("organizer")
                .password("$2a$10$hashed")
                .plainPassword("plain")
                .name("운영진")
                .organization(organization)
                .role(Role.ORGANIZER)
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);
        return user;
    }

    public static User superuser() {
        User user = User.builder()
                .username("admin")
                .password("$2a$10$hashed")
                .plainPassword("plain")
                .name("관리자")
                .organization("개발팀")
                .role(Role.SUPERUSER)
                .build();
        ReflectionTestUtils.setField(user, "id", 9L);
        return user;
    }

    public static Event event(String organization, int targetCount) {
        Event event = Event.builder()
                .name("개강총회")
                .organization(organization)
                .location("대양홀")
                .description("설명")
                .targetCount(targetCount)
                .build();
        ReflectionTestUtils.setField(event, "id", 1L);
        return event;
    }

    public static Booth booth(String organization) {
        Booth booth = Booth.builder()
                .name("감성주점")
                .category("주점")
                .location("잔디마당")
                .emoji("🍺")
                .organization(organization)
                .description("설명")
                .createdIndex(0)
                .build();
        ReflectionTestUtils.setField(booth, "id", 3L);
        return booth;
    }

    public static WaitingEntry waitingEntry(Booth booth, long id, int order, WaitingStatus status) {
        WaitingEntry entry = WaitingEntry.builder()
                .booth(booth)
                .order(order)
                .name("대기자")
                .phone("01000000000")
                .partySize(2)
                .build();
        ReflectionTestUtils.setField(entry, "id", id);
        ReflectionTestUtils.setField(entry, "status", status);
        return entry;
    }
}
