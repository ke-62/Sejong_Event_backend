package com.sejong.event.event.service;

import com.sejong.event.common.exception.CustomException;
import com.sejong.event.common.exception.ErrorCode;
import com.sejong.event.event.domain.Event;
import com.sejong.event.event.domain.EventMode;
import com.sejong.event.event.domain.EventStatus;
import com.sejong.event.event.dto.CreateEventRequest;
import com.sejong.event.event.dto.CreateEventResponse;
import com.sejong.event.event.dto.EventResponse;
import com.sejong.event.event.repository.EventRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @InjectMocks
    private EventService eventService;

    @Mock private EventRepository eventRepository;

    private Event createEvent(Long id, EventMode mode, EventStatus status) {
        Event event = Event.builder()
                .title("개강총회")
                .description("2026 개강총회")
                .mode(mode)
                .maxCapacity(300)
                .managerToken(mode == EventMode.EVENT ? "test-manager-token" : null)
                .startAt(LocalDateTime.now())
                .endAt(LocalDateTime.now().plusHours(3))
                .build();
        ReflectionTestUtils.setField(event, "id", id);
        ReflectionTestUtils.setField(event, "status", status);
        return event;
    }

    // ── 행사 방 생성 (누구나) ──────────────────────────────────────────

    @Test
    @DisplayName("행사 방 생성 성공 — managerToken 반환")
    void createEventRoom_success() {
        CreateEventRequest request = mock(CreateEventRequest.class);
        given(request.getTitle()).willReturn("개강총회");
        given(request.getDescription()).willReturn("2026 개강총회");
        given(request.getMaxCapacity()).willReturn(300);
        given(request.getStartAt()).willReturn(LocalDateTime.now());
        given(request.getEndAt()).willReturn(LocalDateTime.now().plusHours(3));

        Event savedEvent = createEvent(1L, EventMode.EVENT, EventStatus.SCHEDULED);
        given(eventRepository.save(any(Event.class))).willReturn(savedEvent);

        CreateEventResponse response = eventService.createEventRoom(request, "http://localhost:3000");

        assertThat(response.getEventId()).isEqualTo(1L);
        assertThat(response.getManagerToken()).isNotBlank();
        assertThat(response.getQrUrl()).contains("/join/1");
    }

    // ── 축제 생성 (관리자) ──────────────────────────────────────────

    @Test
    @DisplayName("축제 생성 성공")
    void createFestival_success() {
        CreateEventRequest request = mock(CreateEventRequest.class);
        given(request.getTitle()).willReturn("세종연회");
        given(request.getDescription()).willReturn("2026 세종연회");
        given(request.getMaxCapacity()).willReturn(null);
        given(request.getStartAt()).willReturn(LocalDateTime.now());
        given(request.getEndAt()).willReturn(LocalDateTime.now().plusDays(1));

        Event savedEvent = createEvent(2L, EventMode.FESTIVAL, EventStatus.SCHEDULED);
        given(eventRepository.save(any(Event.class))).willReturn(savedEvent);

        EventResponse response = eventService.createFestival(request);

        assertThat(response.getTitle()).isEqualTo("개강총회"); // savedEvent.title
        assertThat(response.getMode()).isEqualTo(EventMode.FESTIVAL);
    }

    // ── 행사 조회 ──────────────────────────────────────────

    @Test
    @DisplayName("행사 상세 조회 성공")
    void getEvent_success() {
        Event event = createEvent(1L, EventMode.EVENT, EventStatus.ACTIVE);
        given(eventRepository.findById(1L)).willReturn(Optional.of(event));

        EventResponse response = eventService.getEvent(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getStatus()).isEqualTo(EventStatus.ACTIVE);
    }

    @Test
    @DisplayName("행사 조회 실패 - 존재하지 않는 행사")
    void getEvent_notFound() {
        given(eventRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.getEvent(99L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.EVENT_NOT_FOUND);
    }

    @Test
    @DisplayName("진행 중인 행사 방 목록 조회")
    void getActiveEventRooms_success() {
        List<Event> events = List.of(
                createEvent(1L, EventMode.EVENT, EventStatus.ACTIVE),
                createEvent(2L, EventMode.EVENT, EventStatus.ACTIVE)
        );
        given(eventRepository.findByModeAndStatus(EventMode.EVENT, EventStatus.ACTIVE))
                .willReturn(events);

        List<EventResponse> responses = eventService.getActiveEventRooms();

        assertThat(responses).hasSize(2);
        assertThat(responses).allMatch(r -> r.getMode() == EventMode.EVENT);
    }

    // ── 행사 종료 (managerToken) ──────────────────────────────────────────

    @Test
    @DisplayName("행사 종료 성공 - managerToken 일치")
    void closeEvent_success() {
        Event event = createEvent(1L, EventMode.EVENT, EventStatus.ACTIVE);
        given(eventRepository.findById(1L)).willReturn(Optional.of(event));

        EventResponse response = eventService.closeEvent(1L, "test-manager-token");

        assertThat(response.getStatus()).isEqualTo(EventStatus.CLOSED);
    }

    @Test
    @DisplayName("행사 종료 실패 - managerToken 불일치")
    void closeEvent_wrongToken() {
        Event event = createEvent(1L, EventMode.EVENT, EventStatus.ACTIVE);
        given(eventRepository.findById(1L)).willReturn(Optional.of(event));

        assertThatThrownBy(() -> eventService.closeEvent(1L, "wrong-token"))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.FORBIDDEN);
    }

    @Test
    @DisplayName("managerToken 검증 실패 - 행사 없음")
    void validateManagerToken_eventNotFound() {
        given(eventRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.validateManagerToken(99L, "any-token"))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.EVENT_NOT_FOUND);
    }
}
