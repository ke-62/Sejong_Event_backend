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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository eventRepository;

    /**
     * 행사 모드 방 생성 — 누구나 가능, 로그인 불필요.
     * 반환된 managerToken 을 분실하면 관리 불가하므로 프론트에서 안전하게 보관해야 한다.
     */
    @Transactional
    public CreateEventResponse createEventRoom(CreateEventRequest request, String baseUrl) {
        if (request.getMaxCapacity() == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT); // EVENT 모드는 정족수 필수
        }
        String managerToken = UUID.randomUUID().toString();

        Event event = Event.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .mode(EventMode.EVENT)
                .maxCapacity(request.getMaxCapacity())
                .managerToken(managerToken)
                .startAt(request.getStartAt())
                .endAt(request.getEndAt())
                .build();

        event = eventRepository.save(event);
        event.activate(); // 생성과 동시에 즉시 활성화

        String qrUrl = baseUrl + "/join/" + event.getId();

        return CreateEventResponse.builder()
                .eventId(event.getId())
                .title(event.getTitle())
                .maxCapacity(event.getMaxCapacity())
                .qrUrl(qrUrl)
                .managerToken(managerToken)
                .build();
    }

    /**
     * 축제(FESTIVAL) 생성 — 관리자 전용.
     * managerToken 없음 (관리자가 JWT로 관리).
     */
    @Transactional
    public EventResponse createFestival(CreateEventRequest request) {
        Event event = Event.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .mode(EventMode.FESTIVAL)
                .maxCapacity(request.getMaxCapacity())
                .managerToken(null)
                .startAt(request.getStartAt())
                .endAt(request.getEndAt())
                .build();

        event = eventRepository.save(event);
        event.activate(); // 생성과 동시에 활성화

        return EventResponse.from(event);
    }

    public EventResponse getEvent(Long eventId) {
        return EventResponse.from(findEvent(eventId));
    }

    public List<EventResponse> getActiveEventRooms() {
        return eventRepository.findByModeAndStatus(EventMode.EVENT, EventStatus.ACTIVE).stream()
                .map(EventResponse::from)
                .toList();
    }

    public List<EventResponse> getActiveFestivals() {
        return eventRepository.findByModeAndStatus(EventMode.FESTIVAL, EventStatus.ACTIVE).stream()
                .map(EventResponse::from)
                .toList();
    }

    /** 행사 종료 — X-Manager-Token 검증 */
    @Transactional
    public EventResponse closeEvent(Long eventId, String managerToken) {
        Event event = findEvent(eventId);
        validateManagerToken(event, managerToken);
        event.close();
        return EventResponse.from(event);
    }

    /** managerToken 검증 (스태프용 — 토큰이 맞으면 관리 권한 부여) */
    public void validateManagerToken(Long eventId, String managerToken) {
        Event event = findEvent(eventId);
        validateManagerToken(event, managerToken);
    }

    private void validateManagerToken(Event event, String managerToken) {
        if (!event.isManagerToken(managerToken)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
    }

    private Event findEvent(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new CustomException(ErrorCode.EVENT_NOT_FOUND));
    }
}
