package com.sejong.event.admin.service;

import com.sejong.event.admin.dto.UpdateRoleRequest;
import com.sejong.event.booth.dto.BoothApplicationResponse;
import com.sejong.event.booth.dto.BoothResponse;
import com.sejong.event.booth.dto.RejectApplicationRequest;
import com.sejong.event.booth.service.BoothService;
import com.sejong.event.common.exception.CustomException;
import com.sejong.event.common.exception.ErrorCode;
import com.sejong.event.event.dto.CreateEventRequest;
import com.sejong.event.event.dto.EventResponse;
import com.sejong.event.event.repository.EventRepository;
import com.sejong.event.event.service.EventService;
import com.sejong.event.user.domain.User;
import com.sejong.event.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final EventService eventService;
    private final BoothService boothService;

    // ── 사용자 관리 ──────────────────────────────────────────

    public List<Map<String, Object>> getAllUsers() {
        return userRepository.findAll().stream()
                .map(u -> Map.<String, Object>of(
                        "id", u.getId(),
                        "studentId", u.getStudentId(),
                        "name", u.getName(),
                        "role", u.getRole().name(),
                        "createdAt", u.getCreatedAt()
                ))
                .toList();
    }

    @Transactional
    public Map<String, Object> updateUserRole(Long userId, UpdateRoleRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        user.updateRole(request.getRole());
        return Map.of(
                "id", user.getId(),
                "studentId", user.getStudentId(),
                "name", user.getName(),
                "role", user.getRole().name()
        );
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        userRepository.delete(user);
    }

    // ── 행사 관리 ──────────────────────────────────────────

    public List<EventResponse> getAllEvents() {
        return eventRepository.findAll().stream()
                .map(EventResponse::from)
                .toList();
    }

    /** 축제(FESTIVAL) 생성 — 관리자 전용 */
    @Transactional
    public EventResponse createFestival(CreateEventRequest request) {
        return eventService.createFestival(request);
    }

    @Transactional
    public void forceDeleteEvent(Long eventId) {
        eventRepository.delete(
                eventRepository.findById(eventId)
                        .orElseThrow(() -> new CustomException(ErrorCode.EVENT_NOT_FOUND))
        );
    }

    // ── 부스 신청 관리 ──────────────────────────────────────────

    public List<BoothApplicationResponse> getApplicationsByFestival(Long festivalId) {
        return boothService.getApplicationsByFestival(festivalId);
    }

    @Transactional
    public BoothApplicationResponse approveApplication(Long applicationId) {
        return boothService.approveApplication(applicationId);
    }

    @Transactional
    public BoothApplicationResponse rejectApplication(Long applicationId, RejectApplicationRequest request) {
        return boothService.rejectApplication(applicationId, request.getReason());
    }

    // ── 부스 관리 ──────────────────────────────────────────

    public List<BoothResponse> getAllBooths() {
        return boothService.getAllBooths();
    }

    @Transactional
    public void forceDeleteBooth(Long boothId) {
        boothService.forceDeleteBooth(boothId);
    }
}
