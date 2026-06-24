package com.sejong.event.ticket.service;

import com.sejong.event.auth.jwt.JwtProvider;
import com.sejong.event.common.exception.CustomException;
import com.sejong.event.common.exception.ErrorCode;
import com.sejong.event.event.domain.Event;
import com.sejong.event.event.domain.EventStatus;
import com.sejong.event.event.repository.EventRepository;
import com.sejong.event.event.service.EventService;
import com.sejong.event.ticket.domain.Ticket;
import com.sejong.event.ticket.domain.TicketStatus;
import com.sejong.event.ticket.dto.IssueTicketRequest;
import com.sejong.event.ticket.dto.QrScanRequest;
import com.sejong.event.ticket.dto.QrScanResponse;
import com.sejong.event.ticket.dto.QrScanResponse.ScanResult;
import com.sejong.event.ticket.dto.TicketResponse;
import com.sejong.event.ticket.repository.TicketRepository;
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
public class TicketService {

    private final TicketRepository ticketRepository;
    private final EventRepository eventRepository;
    private final EventService eventService;
    private final JwtProvider jwtProvider;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 번호표 발급 — 로그인 불필요.
     * 학번 기반 1인 1장 원칙 강제.
     */
    @Transactional
    public TicketResponse issueTicket(Long eventId, IssueTicketRequest request) {
        Event event = findActiveEvent(eventId);

        if (ticketRepository.existsByEventIdAndStudentId(eventId, request.getStudentId())) {
            throw new CustomException(ErrorCode.DUPLICATE_TICKET);
        }

        int seq = ticketRepository.countByEventId(eventId) + 1;
        String ticketNumber = String.format("EVT%d-%04d", eventId, seq);

        Ticket ticket = Ticket.builder()
                .event(event)
                .studentId(request.getStudentId())
                .studentName(request.getStudentName())
                .ticketNumber(ticketNumber)
                .qrToken("PENDING")
                .build();

        ticket = ticketRepository.saveAndFlush(ticket);

        String qrToken = jwtProvider.generateQrToken(
                ticket.getId(), eventId, request.getStudentId(), ticketNumber);
        ticket.updateQrToken(qrToken);

        return TicketResponse.from(ticket);
    }

    /**
     * QR 스캔 입장 처리 — managerToken 검증 필요.
     * 방을 만든 주최자(또는 위임받은 스태프)만 스캔 가능.
     */
    @Transactional
    public QrScanResponse scanQr(Long eventId, String managerToken, QrScanRequest request) {
        // managerToken 검증 (방 관리자만 스캔 가능)
        eventService.validateManagerToken(eventId, managerToken);

        Claims claims;
        try {
            claims = jwtProvider.validateToken(request.getQrToken());
        } catch (CustomException e) {
            return QrScanResponse.builder()
                    .result(ScanResult.INVALID)
                    .message("위조되거나 유효하지 않은 QR입니다.")
                    .build();
        }

        if (!"QR".equals(claims.get("type", String.class))) {
            return QrScanResponse.builder()
                    .result(ScanResult.INVALID)
                    .message("QR 토큰 형식이 올바르지 않습니다.")
                    .build();
        }

        Long tokenEventId = claims.get("eventId", Long.class);
        if (!eventId.equals(tokenEventId)) {
            return QrScanResponse.builder()
                    .result(ScanResult.INVALID)
                    .message("다른 행사의 QR입니다.")
                    .build();
        }

        Long ticketId = Long.parseLong(claims.getSubject());
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new CustomException(ErrorCode.TICKET_NOT_FOUND));

        if (Boolean.TRUE.equals(ticket.getIsEntered())) {
            return QrScanResponse.builder()
                    .result(ScanResult.DUPLICATE)
                    .message("이미 입장 처리된 번호표입니다.")
                    .studentId(ticket.getStudentId())
                    .ticketNumber(ticket.getTicketNumber())
                    .build();
        }

        ticket.enter();

        int totalEntered = ticketRepository.countByEventIdAndIsEntered(eventId, true);

        messagingTemplate.convertAndSend(
                "/topic/event/" + eventId,
                Map.of("eventId", eventId, "currentCount", totalEntered)
        );

        return QrScanResponse.builder()
                .result(ScanResult.SUCCESS)
                .message("입장이 확인되었습니다.")
                .studentId(ticket.getStudentId())
                .ticketNumber(ticket.getTicketNumber())
                .totalEntered(totalEntered)
                .build();
    }

    /**
     * 추첨 — managerToken 검증 필요.
     * 입장 확인된 참가자 중 랜덤 선발.
     */
    @Transactional
    public TicketResponse drawLottery(Long eventId, String managerToken) {
        eventService.validateManagerToken(eventId, managerToken);

        List<Ticket> entered = new ArrayList<>(
                ticketRepository.findByEventIdAndStatus(eventId, TicketStatus.ENTERED));

        if (entered.isEmpty()) {
            throw new CustomException(ErrorCode.TICKET_NOT_FOUND);
        }

        Collections.shuffle(entered);
        Ticket winner = entered.get(0);
        winner.markAsDrawn();

        return TicketResponse.from(winner);
    }

    /** 참여자 본인 번호표 조회 — 로그인 불필요. 학번으로 조회. */
    public TicketResponse getMyTicket(Long eventId, String studentId) {
        Ticket ticket = ticketRepository.findByEventIdAndStudentId(eventId, studentId)
                .orElseThrow(() -> new CustomException(ErrorCode.TICKET_NOT_FOUND));
        return TicketResponse.from(ticket);
    }

    public List<TicketResponse> getEventTickets(Long eventId, String managerToken) {
        eventService.validateManagerToken(eventId, managerToken);
        return ticketRepository.findByEventId(eventId).stream()
                .map(TicketResponse::from)
                .toList();
    }

    private Event findActiveEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new CustomException(ErrorCode.EVENT_NOT_FOUND));
        if (event.getStatus() != EventStatus.ACTIVE) {
            throw new CustomException(ErrorCode.EVENT_NOT_ACTIVE);
        }
        return event;
    }
}
