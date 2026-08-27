package com.sejong.event.event.service;

import com.sejong.event.attendance.repository.AttendanceLogRepository;
import com.sejong.event.common.exception.CustomException;
import com.sejong.event.common.exception.ErrorCode;
import com.sejong.event.event.domain.Event;
import com.sejong.event.event.dto.EventResponse;
import com.sejong.event.event.repository.EventRepository;
import com.sejong.event.support.TestFixtures;
import com.sejong.event.support.TestRequests;
import com.sejong.event.user.domain.User;
import com.sejong.event.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AttendanceLogRepository attendanceLogRepository;
    @InjectMocks
    private EventService eventService;

    @Test
    @DisplayName("행사 생성 시 주최 기관은 로그인 사용자의 소속으로 설정된다")
    void createEventUsesUserOrganization() {
        User user = TestFixtures.organizer("전자정보통신공학과 학생회");
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(eventRepository.save(any(Event.class))).willAnswer(invocation -> invocation.getArgument(0));

        EventResponse response = eventService.createEvent(1L, TestRequests.createEvent("개강총회", "대양홀", 200));

        assertThat(response.getOrganization()).isEqualTo("전자정보통신공학과 학생회");
        assertThat(response.getName()).isEqualTo("개강총회");
        assertThat(response.getAttendanceCount()).isZero();
    }

    @Test
    @DisplayName("비로그인 조회는 활성 행사만 반환한다")
    void getEventsAnonymous() {
        given(eventRepository.findByIsActiveTrue()).willReturn(List.of(TestFixtures.event("A", 10)));

        List<EventResponse> response = eventService.getEvents(null);

        assertThat(response).hasSize(1);
        then(eventRepository).should(never()).findAll();
    }

    @Test
    @DisplayName("슈퍼유저 조회는 전체 행사를 반환한다")
    void getEventsAsSuperuser() {
        given(userRepository.findById(9L)).willReturn(Optional.of(TestFixtures.superuser()));
        given(eventRepository.findAll())
                .willReturn(List.of(TestFixtures.event("A", 10), TestFixtures.event("B", 20)));

        List<EventResponse> response = eventService.getEvents(9L);

        assertThat(response).hasSize(2);
    }

    @Test
    @DisplayName("운영진 조회는 자기 기관 행사만 반환한다")
    void getEventsAsOrganizer() {
        User user = TestFixtures.organizer("컴퓨터공학과 학생회");
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(eventRepository.findByOrganization("컴퓨터공학과 학생회"))
                .willReturn(List.of(TestFixtures.event("컴퓨터공학과 학생회", 10)));

        List<EventResponse> response = eventService.getEvents(1L);

        assertThat(response).hasSize(1);
    }

    @Test
    @DisplayName("다른 기관 행사를 수정하려 하면 EVENT_ACCESS_DENIED")
    void updateOtherOrganizationEvent() {
        User user = TestFixtures.organizer("컴퓨터공학과 학생회");
        Event other = TestFixtures.event("전자공학과 학생회", 10);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(eventRepository.findById(5L)).willReturn(Optional.of(other));

        assertThatThrownBy(() -> eventService.updateEvent(1L, 5L, TestRequests.updateEvent("새 이름")))
                .isInstanceOfSatisfying(CustomException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.EVENT_ACCESS_DENIED));
    }

    @Test
    @DisplayName("행사 삭제 시 입장 로그를 먼저 제거한다")
    void deleteEventClearsAttendanceLogs() {
        Event event = TestFixtures.event("개발팀", 10);
        given(userRepository.findById(9L)).willReturn(Optional.of(TestFixtures.superuser()));
        given(eventRepository.findById(5L)).willReturn(Optional.of(event));

        eventService.deleteEvent(9L, 5L);

        then(attendanceLogRepository).should().deleteByEventId(5L);
        then(eventRepository).should().delete(event);
    }
}
