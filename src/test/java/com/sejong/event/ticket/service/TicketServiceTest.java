package com.sejong.event.ticket.service;

import com.sejong.event.auth.jwt.JwtProvider;
import com.sejong.event.common.exception.CustomException;
import com.sejong.event.common.exception.ErrorCode;
import com.sejong.event.event.domain.Event;
import com.sejong.event.event.domain.EventMode;
import com.sejong.event.event.domain.EventStatus;
import com.sejong.event.event.repository.EventRepository;
import com.sejong.event.event.service.EventService;
import com.sejong.event.ticket.domain.Ticket;
import com.sejong.event.ticket.domain.TicketStatus;
import com.sejong.event.ticket.dto.IssueTicketRequest;
import com.sejong.event.ticket.dto.QrScanRequest;
import com.sejong.event.ticket.dto.QrScanResponse;
import com.sejong.event.ticket.dto.TicketResponse;
import com.sejong.event.ticket.repository.TicketRepository;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @InjectMocks
    private TicketService ticketService;

    @Mock private TicketRepository ticketRepository;
    @Mock private EventRepository eventRepository;
    @Mock private EventService eventService;
    @Mock private JwtProvider jwtProvider;
    @Mock private SimpMessagingTemplate messagingTemplate;

    private static final String MANAGER_TOKEN = "test-manager-token";

    private Event createActiveEvent(Long id) {
        Event event = Event.builder()
                .title("개강총회")
                .description("테스트")
                .mode(EventMode.EVENT)
                .maxCapacity(300)
                .managerToken(MANAGER_TOKEN)
                .startAt(LocalDateTime.now())
                .endAt(LocalDateTime.now().plusHours(3))
                .build();
        ReflectionTestUtils.setField(event, "id", id);
        ReflectionTestUtils.setField(event, "status", EventStatus.ACTIVE);
        return event;
    }

    private Ticket createTicket(Long id, Event event, String studentId, boolean entered) {
        Ticket ticket = Ticket.builder()
                .event(event)
                .studentId(studentId)
                .studentName("테스트학생")
                .ticketNumber("EVT1-0001")
                .qrToken("qrToken")
                .build();
        ReflectionTestUtils.setField(ticket, "id", id);
        ReflectionTestUtils.setField(ticket, "isEntered", entered);
        ReflectionTestUtils.setField(ticket, "status", entered ? TicketStatus.ENTERED : TicketStatus.ISSUED);
        ReflectionTestUtils.setField(ticket, "issuedAt", LocalDateTime.now());
        return ticket;
    }

    // ── 번호표 발급 ──────────────────────────────────────────

    @Test
    @DisplayName("번호표 발급 성공")
    void issueTicket_success() {
        Event event = createActiveEvent(1L);
        given(eventRepository.findById(1L)).willReturn(Optional.of(event));
        given(ticketRepository.existsByEventIdAndStudentId(1L, "23011679")).willReturn(false);
        given(ticketRepository.countByEventId(1L)).willReturn(0);

        Ticket savedTicket = createTicket(1L, event, "23011679", false);
        given(ticketRepository.saveAndFlush(any(Ticket.class))).willReturn(savedTicket);
        given(jwtProvider.generateQrToken(anyLong(), anyLong(), anyString(), anyString()))
                .willReturn("generatedQrToken");

        IssueTicketRequest request = mock(IssueTicketRequest.class);
        given(request.getStudentId()).willReturn("23011679");
        given(request.getStudentName()).willReturn("테스트학생");

        TicketResponse response = ticketService.issueTicket(1L, request);

        assertThat(response).isNotNull();
        assertThat(response.getStudentId()).isEqualTo("23011679");
        assertThat(response.getStatus()).isEqualTo(TicketStatus.ISSUED);
    }

    @Test
    @DisplayName("번호표 발급 실패 - 이미 발급된 학번 (1인 1장 원칙)")
    void issueTicket_duplicate() {
        Event event = createActiveEvent(1L);
        given(eventRepository.findById(1L)).willReturn(Optional.of(event));
        given(ticketRepository.existsByEventIdAndStudentId(1L, "23011679")).willReturn(true);

        IssueTicketRequest request = mock(IssueTicketRequest.class);
        given(request.getStudentId()).willReturn("23011679");

        assertThatThrownBy(() -> ticketService.issueTicket(1L, request))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.DUPLICATE_TICKET);
    }

    @Test
    @DisplayName("번호표 발급 실패 - 행사가 진행 중이 아님")
    void issueTicket_eventNotActive() {
        Event event = Event.builder()
                .title("예정 행사").description("").mode(EventMode.EVENT)
                .maxCapacity(300).managerToken(MANAGER_TOKEN)
                .startAt(LocalDateTime.now().plusDays(1))
                .endAt(LocalDateTime.now().plusDays(1).plusHours(3))
                .build();
        ReflectionTestUtils.setField(event, "id", 1L);
        // SCHEDULED 상태 (기본값)

        given(eventRepository.findById(1L)).willReturn(Optional.of(event));

        assertThatThrownBy(() -> ticketService.issueTicket(1L, mock(IssueTicketRequest.class)))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.EVENT_NOT_ACTIVE);
    }

    @Test
    @DisplayName("번호표 발급 실패 - 존재하지 않는 행사")
    void issueTicket_eventNotFound() {
        given(eventRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.issueTicket(99L, mock(IssueTicketRequest.class)))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.EVENT_NOT_FOUND);
    }

    // ── QR 스캔 입장 ──────────────────────────────────────────

    @Test
    @DisplayName("QR 스캔 입장 성공")
    void scanQr_success() {
        Event event = createActiveEvent(1L);
        Ticket ticket = createTicket(1L, event, "23011679", false);

        // managerToken 검증은 EventService에 위임 — 성공 시 아무것도 안 함
        willDoNothing().given(eventService).validateManagerToken(1L, MANAGER_TOKEN);

        Claims claims = mock(Claims.class);
        given(claims.get("type", String.class)).willReturn("QR");
        given(claims.get("eventId", Long.class)).willReturn(1L);
        given(claims.getSubject()).willReturn("1");

        given(jwtProvider.validateToken("validQrToken")).willReturn(claims);
        given(ticketRepository.findById(1L)).willReturn(Optional.of(ticket));
        given(ticketRepository.countByEventIdAndIsEntered(1L, true)).willReturn(1);

        QrScanRequest request = mock(QrScanRequest.class);
        given(request.getQrToken()).willReturn("validQrToken");

        QrScanResponse response = ticketService.scanQr(1L, MANAGER_TOKEN, request);

        assertThat(response.getResult()).isEqualTo(QrScanResponse.ScanResult.SUCCESS);
        assertThat(response.getTotalEntered()).isEqualTo(1);
    }

    @Test
    @DisplayName("QR 스캔 실패 - 이미 입장한 번호표 (중복 입장)")
    void scanQr_alreadyEntered() {
        Event event = createActiveEvent(1L);
        Ticket ticket = createTicket(1L, event, "23011679", true); // 이미 입장

        willDoNothing().given(eventService).validateManagerToken(1L, MANAGER_TOKEN);

        Claims claims = mock(Claims.class);
        given(claims.get("type", String.class)).willReturn("QR");
        given(claims.get("eventId", Long.class)).willReturn(1L);
        given(claims.getSubject()).willReturn("1");

        given(jwtProvider.validateToken("validQrToken")).willReturn(claims);
        given(ticketRepository.findById(1L)).willReturn(Optional.of(ticket));

        QrScanRequest request = mock(QrScanRequest.class);
        given(request.getQrToken()).willReturn("validQrToken");

        QrScanResponse response = ticketService.scanQr(1L, MANAGER_TOKEN, request);

        assertThat(response.getResult()).isEqualTo(QrScanResponse.ScanResult.DUPLICATE);
    }

    @Test
    @DisplayName("QR 스캔 실패 - 위조 토큰")
    void scanQr_invalidToken() {
        willDoNothing().given(eventService).validateManagerToken(1L, MANAGER_TOKEN);
        given(jwtProvider.validateToken("fakeToken"))
                .willThrow(new CustomException(ErrorCode.INVALID_TOKEN));

        QrScanRequest request = mock(QrScanRequest.class);
        given(request.getQrToken()).willReturn("fakeToken");

        QrScanResponse response = ticketService.scanQr(1L, MANAGER_TOKEN, request);

        assertThat(response.getResult()).isEqualTo(QrScanResponse.ScanResult.INVALID);
    }

    @Test
    @DisplayName("QR 스캔 실패 - 다른 행사 QR")
    void scanQr_wrongEvent() {
        willDoNothing().given(eventService).validateManagerToken(1L, MANAGER_TOKEN);

        Claims claims = mock(Claims.class);
        given(claims.get("type", String.class)).willReturn("QR");
        given(claims.get("eventId", Long.class)).willReturn(99L); // 다른 행사
        given(jwtProvider.validateToken("otherEventQr")).willReturn(claims);

        QrScanRequest request = mock(QrScanRequest.class);
        given(request.getQrToken()).willReturn("otherEventQr");

        QrScanResponse response = ticketService.scanQr(1L, MANAGER_TOKEN, request);

        assertThat(response.getResult()).isEqualTo(QrScanResponse.ScanResult.INVALID);
        assertThat(response.getMessage()).contains("다른 행사");
    }

    // ── 추첨 ──────────────────────────────────────────

    @Test
    @DisplayName("추첨 성공")
    void drawLottery_success() {
        Event event = createActiveEvent(1L);
        Ticket ticket1 = createTicket(1L, event, "23011679", true);
        Ticket ticket2 = createTicket(2L, event, "24010964", true);

        willDoNothing().given(eventService).validateManagerToken(1L, MANAGER_TOKEN);
        given(ticketRepository.findByEventIdAndStatus(1L, TicketStatus.ENTERED))
                .willReturn(List.of(ticket1, ticket2));

        TicketResponse response = ticketService.drawLottery(1L, MANAGER_TOKEN);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(TicketStatus.DRAWN);
    }

    @Test
    @DisplayName("추첨 실패 - 입장자 없음")
    void drawLottery_noEnteredTickets() {
        willDoNothing().given(eventService).validateManagerToken(1L, MANAGER_TOKEN);
        given(ticketRepository.findByEventIdAndStatus(1L, TicketStatus.ENTERED))
                .willReturn(List.of());

        assertThatThrownBy(() -> ticketService.drawLottery(1L, MANAGER_TOKEN))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.TICKET_NOT_FOUND);
    }
}
