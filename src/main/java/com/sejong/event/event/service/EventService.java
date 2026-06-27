package com.sejong.event.event.service;

import com.sejong.event.common.exception.CustomException;
import com.sejong.event.common.exception.ErrorCode;
import com.sejong.event.event.domain.Event;
import com.sejong.event.event.dto.CreateEventRequest;
import com.sejong.event.event.dto.EventResponse;
import com.sejong.event.event.dto.UpdateEventRequest;
import com.sejong.event.attendance.repository.AttendanceLogRepository;
import com.sejong.event.event.repository.EventRepository;
import com.sejong.event.user.domain.Role;
import com.sejong.event.user.domain.User;
import com.sejong.event.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final AttendanceLogRepository attendanceLogRepository;

    /** GET /events — 인증 선택. 미인증: 전체 활성, organizer: 본인 org, superuser: 전체 */
    public List<EventResponse> getEvents(Long userId) {
        if (userId == null) {
            return eventRepository.findByIsActiveTrue().stream()
                    .map(EventResponse::from)
                    .toList();
        }
        User user = findUser(userId);
        if (user.getRole() == Role.SUPERUSER) {
            return eventRepository.findAll().stream()
                    .map(EventResponse::from)
                    .toList();
        }
        return eventRepository.findByOrganization(user.getOrganization()).stream()
                .map(EventResponse::from)
                .toList();
    }

    public EventResponse getEvent(Long eventId) {
        return EventResponse.from(findEvent(eventId));
    }

    @Transactional
    public EventResponse createEvent(Long userId, CreateEventRequest request) {
        User user = findUser(userId);

        Event event = Event.builder()
                .name(request.getName())
                .organization(user.getOrganization())
                .location(request.getLocation())
                .startAt(request.getStartAt())
                .description(request.getDescription())
                .targetCount(request.getTargetCount())
                .build();

        return EventResponse.from(eventRepository.save(event));
    }

    @Transactional
    public EventResponse updateEvent(Long userId, Long eventId, UpdateEventRequest request) {
        User user = findUser(userId);
        Event event = findEvent(eventId);

        checkEventAccess(user, event);

        String newOrg = (user.getRole() == Role.SUPERUSER) ? request.getOrganization() : null;
        event.update(request.getName(), newOrg, request.getLocation(),
                request.getStartAt(), request.getTargetCount());

        return EventResponse.from(event);
    }

    @Transactional
    public void deleteEvent(Long userId, Long eventId) {
        User user = findUser(userId);
        Event event = findEvent(eventId);
        checkEventAccess(user, event);
        attendanceLogRepository.deleteByEventId(eventId);
        eventRepository.delete(event);
    }

    public Event findEvent(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new CustomException(ErrorCode.EVENT_NOT_FOUND));
    }

    private void checkEventAccess(User user, Event event) {
        if (user.getRole() != Role.SUPERUSER
                && !event.getOrganization().equals(user.getOrganization())) {
            throw new CustomException(ErrorCode.EVENT_ACCESS_DENIED);
        }
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }
}
