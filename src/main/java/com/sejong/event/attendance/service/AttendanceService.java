package com.sejong.event.attendance.service;

import com.sejong.event.attendance.domain.AttendanceLog;
import com.sejong.event.attendance.domain.AttendanceStatus;
import com.sejong.event.attendance.dto.*;
import com.sejong.event.attendance.repository.AttendanceLogRepository;
import com.sejong.event.auth.jwt.JwtProvider;
import com.sejong.event.common.exception.CustomException;
import com.sejong.event.common.exception.ErrorCode;
import com.sejong.event.event.domain.Event;
import com.sejong.event.event.service.EventService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceService {

    private static final int QR_TOKEN_EXPIRES_IN = 60; // 초

    private final AttendanceLogRepository attendanceLogRepository;
    private final EventService eventService;
    private final JwtProvider jwtProvider;
    private final SimpMessagingTemplate messagingTemplate;

    /** GET /events/{eventId}/qr-token — 운영진 QR 토큰 발급 */
    public QrTokenResponse generateQrToken(Long eventId) {
        eventService.findEvent(eventId); // 행사 존재 확인
        String token = jwtProvider.generateOrganizerQrToken(eventId);
        return QrTokenResponse.builder()
                .qrToken(token)
                .expiresIn(QR_TOKEN_EXPIRES_IN)
                .build();
    }

    /** POST /events/{eventId}/attendance/scan — 학생 QR 스캔 입장 (인증 불필요) */
    @Transactional
    public ScanResponse scan(Long eventId, ScanRequest request) {
        // 중복 학번 확인
        if (attendanceLogRepository.existsByEventIdAndStudentId(eventId, request.getStudentId())) {
            return ScanResponse.builder()
                    .status("duplicate")
                    .studentId(request.getStudentId())
                    .build();
        }

        // QR 토큰 유효성 검사
        Claims claims;
        try {
            claims = jwtProvider.validateToken(request.getQrValue());
        } catch (CustomException e) {
            return ScanResponse.builder().status("error").build();
        }

        if (!"ORGANIZER_QR".equals(claims.get("type", String.class))) {
            return ScanResponse.builder().status("error").build();
        }

        Long tokenEventId = claims.get("eventId", Long.class);
        if (!eventId.equals(tokenEventId)) {
            return ScanResponse.builder().status("error").build();
        }

        Event event = eventService.findEvent(eventId);
        int ticketNumber = attendanceLogRepository.countByEventId(eventId) + 1;

        AttendanceLog log = AttendanceLog.builder()
                .event(event)
                .studentId(request.getStudentId())
                .name(request.getName())
                .department(request.getDepartment())
                .ticketNumber(ticketNumber)
                .gate("정문") // QR 토큰 기반 기본값
                .status(AttendanceStatus.SUCCESS)
                .build();

        attendanceLogRepository.save(log);
        event.incrementAttendance();

        messagingTemplate.convertAndSend(
                "/topic/events/" + eventId,
                Map.of("eventId", eventId, "attendanceCount", event.getAttendanceCount())
        );

        return ScanResponse.builder()
                .status("success")
                .ticketNumber(ticketNumber)
                .studentId(request.getStudentId())
                .name(request.getName())
                .attendanceCount(event.getAttendanceCount())
                .build();
    }

    /** POST /events/{eventId}/attendance/manual — 운영진 수동 번호표 발급 */
    @Transactional
    public ManualTicketResponse manualIssue(Long eventId, ManualTicketRequest request) {
        if (attendanceLogRepository.existsByEventIdAndStudentId(eventId, request.getStudentId())) {
            throw new CustomException(ErrorCode.DUPLICATE_TICKET);
        }

        Event event = eventService.findEvent(eventId);
        int ticketNumber = attendanceLogRepository.countByEventId(eventId) + 1;

        AttendanceLog log = AttendanceLog.builder()
                .event(event)
                .studentId(request.getStudentId())
                .name(request.getName())
                .department(request.getDepartment())
                .ticketNumber(ticketNumber)
                .gate("수동")
                .status(AttendanceStatus.SUCCESS)
                .build();

        attendanceLogRepository.save(log);
        event.incrementAttendance();

        return ManualTicketResponse.builder()
                .ticketNumber(ticketNumber)
                .studentId(request.getStudentId())
                .name(request.getName())
                .build();
    }

    /** GET /events/{eventId}/attendance — 입장 로그 목록 */
    public AttendanceListResponse getAttendanceLogs(Long eventId) {
        Event event = eventService.findEvent(eventId);
        List<AttendanceLogDto> logs = attendanceLogRepository
                .findByEventIdOrderByCreatedAtDesc(eventId).stream()
                .map(AttendanceLogDto::from)
                .toList();

        return AttendanceListResponse.builder()
                .attendanceCount(event.getAttendanceCount())
                .targetCount(event.getTargetCount())
                .logs(logs)
                .build();
    }

    /** POST /events/{eventId}/lottery — 추첨 */
    @Transactional
    public LotteryResponse lottery(Long eventId) {
        List<AttendanceLog> candidates = new ArrayList<>(
                attendanceLogRepository.findByEventIdAndStatus(eventId, AttendanceStatus.SUCCESS));

        if (candidates.isEmpty()) {
            throw new CustomException(ErrorCode.TICKET_NOT_FOUND);
        }

        Collections.shuffle(candidates);
        AttendanceLog winner = candidates.get(0);

        return LotteryResponse.builder()
                .ticketNumber(winner.getTicketNumber())
                .studentId(winner.getStudentId())
                .name(winner.getName())
                .department(winner.getDepartment())
                .build();
    }
}
