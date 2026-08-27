package com.sejong.event.attendance.service;

import com.sejong.event.attendance.domain.AttendanceLog;
import com.sejong.event.attendance.domain.AttendanceStatus;
import com.sejong.event.attendance.dto.AttendanceListResponse;
import com.sejong.event.attendance.dto.LotteryResponse;
import com.sejong.event.attendance.dto.ManualTicketResponse;
import com.sejong.event.attendance.dto.ScanResponse;
import com.sejong.event.attendance.repository.AttendanceLogRepository;
import com.sejong.event.auth.jwt.JwtProvider;
import com.sejong.event.common.exception.CustomException;
import com.sejong.event.common.exception.ErrorCode;
import com.sejong.event.event.domain.Event;
import com.sejong.event.event.service.EventService;
import com.sejong.event.support.TestFixtures;
import com.sejong.event.support.TestRequests;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    private AttendanceLogRepository attendanceLogRepository;
    @Mock
    private EventService eventService;
    @Mock
    private JwtProvider jwtProvider;
    @Mock
    private SimpMessagingTemplate messagingTemplate;
    @InjectMocks
    private AttendanceService attendanceService;

    @Test
    @DisplayName("이미 등록된 학번이면 status=duplicate")
    void scanDuplicateStudent() {
        given(attendanceLogRepository.existsByEventIdAndStudentId(1L, "20211234")).willReturn(true);

        ScanResponse response = attendanceService.scan(1L, TestRequests.scan("20211234", "김세종", "qr"));

        assertThat(response.getStatus()).isEqualTo("duplicate");
    }

    @Test
    @DisplayName("QR 토큰 검증에 실패하면 status=error")
    void scanInvalidToken() {
        given(attendanceLogRepository.existsByEventIdAndStudentId(anyLong(), anyString())).willReturn(false);
        given(jwtProvider.validateToken("bad")).willThrow(new CustomException(ErrorCode.INVALID_TOKEN));

        ScanResponse response = attendanceService.scan(1L, TestRequests.scan("20211234", "김세종", "bad"));

        assertThat(response.getStatus()).isEqualTo("error");
    }

    @Test
    @DisplayName("운영진 QR 유형이 아니면 status=error")
    void scanWrongTokenType() {
        given(attendanceLogRepository.existsByEventIdAndStudentId(anyLong(), anyString())).willReturn(false);
        Claims claims = mock(Claims.class);
        given(claims.get("type", String.class)).willReturn("ACCESS");
        given(jwtProvider.validateToken("qr")).willReturn(claims);

        ScanResponse response = attendanceService.scan(1L, TestRequests.scan("20211234", "김세종", "qr"));

        assertThat(response.getStatus()).isEqualTo("error");
    }

    @Test
    @DisplayName("다른 행사의 토큰이면 status=error")
    void scanTokenForAnotherEvent() {
        given(attendanceLogRepository.existsByEventIdAndStudentId(anyLong(), anyString())).willReturn(false);
        Claims claims = mock(Claims.class);
        given(claims.get("type", String.class)).willReturn("ORGANIZER_QR");
        given(claims.get("eventId", Long.class)).willReturn(999L);
        given(jwtProvider.validateToken("qr")).willReturn(claims);

        ScanResponse response = attendanceService.scan(1L, TestRequests.scan("20211234", "김세종", "qr"));

        assertThat(response.getStatus()).isEqualTo("error");
    }

    @Test
    @DisplayName("정상 스캔이면 번호표를 발급하고 입장자 수를 브로드캐스트한다")
    void scanSuccess() {
        given(attendanceLogRepository.existsByEventIdAndStudentId(1L, "20211234")).willReturn(false);
        Claims claims = mock(Claims.class);
        given(claims.get("type", String.class)).willReturn("ORGANIZER_QR");
        given(claims.get("eventId", Long.class)).willReturn(1L);
        given(jwtProvider.validateToken("qr")).willReturn(claims);
        Event event = TestFixtures.event("컴퓨터공학과 학생회", 100);
        given(eventService.findEvent(1L)).willReturn(event);
        given(attendanceLogRepository.countByEventId(1L)).willReturn(4);

        ScanResponse response = attendanceService.scan(1L, TestRequests.scan("20211234", "김세종", "qr"));

        assertThat(response.getStatus()).isEqualTo("success");
        assertThat(response.getTicketNumber()).isEqualTo(5);
        assertThat(response.getAttendanceCount()).isEqualTo(1);
        then(attendanceLogRepository).should().save(any(AttendanceLog.class));
        then(messagingTemplate).should().convertAndSend(eq("/topic/events/1"), any(Object.class));
    }

    @Test
    @DisplayName("수동 발급 시 중복 학번이면 DUPLICATE_TICKET")
    void manualIssueDuplicate() {
        given(attendanceLogRepository.existsByEventIdAndStudentId(1L, "20211234")).willReturn(true);

        assertThatThrownBy(() -> attendanceService.manualIssue(1L, TestRequests.manualTicket("20211234", "김세종")))
                .isInstanceOfSatisfying(CustomException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_TICKET));
    }

    @Test
    @DisplayName("수동 발급 성공 시 번호표 번호를 매겨 저장하고 입장자 수를 올린다")
    void manualIssueSuccess() {
        given(attendanceLogRepository.existsByEventIdAndStudentId(1L, "20211234")).willReturn(false);
        Event event = TestFixtures.event("컴퓨터공학과 학생회", 100);
        given(eventService.findEvent(1L)).willReturn(event);
        given(attendanceLogRepository.countByEventId(1L)).willReturn(9);

        ManualTicketResponse response =
                attendanceService.manualIssue(1L, TestRequests.manualTicket("20211234", "김세종"));

        assertThat(response.getTicketNumber()).isEqualTo(10);
        assertThat(event.getAttendanceCount()).isEqualTo(1);
        then(attendanceLogRepository).should().save(any(AttendanceLog.class));
    }

    @Test
    @DisplayName("입장 로그 조회는 누적 인원·목표 인원과 로그 목록을 반환한다")
    void getAttendanceLogs() {
        Event event = TestFixtures.event("컴퓨터공학과 학생회", 200);
        given(eventService.findEvent(1L)).willReturn(event);
        given(attendanceLogRepository.findByEventIdOrderByCreatedAtDesc(1L)).willReturn(List.of(
                AttendanceLog.builder().studentId("20211111").name("A").ticketNumber(1)
                        .status(AttendanceStatus.SUCCESS).build()));

        AttendanceListResponse response = attendanceService.getAttendanceLogs(1L);

        assertThat(response.getTargetCount()).isEqualTo(200);
        assertThat(response.getLogs()).hasSize(1);
    }

    @Test
    @DisplayName("추첨 후보가 없으면 TICKET_NOT_FOUND")
    void lotteryWithoutCandidates() {
        given(attendanceLogRepository.findByEventIdAndStatus(1L, AttendanceStatus.SUCCESS))
                .willReturn(List.of());

        assertThatThrownBy(() -> attendanceService.lottery(1L))
                .isInstanceOfSatisfying(CustomException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.TICKET_NOT_FOUND));
    }

    @Test
    @DisplayName("추첨은 정상 입장자 중 한 명을 반환한다")
    void lotteryPicksOneCandidate() {
        AttendanceLog first = AttendanceLog.builder()
                .studentId("20211111").name("A").ticketNumber(1).status(AttendanceStatus.SUCCESS).build();
        AttendanceLog second = AttendanceLog.builder()
                .studentId("20212222").name("B").ticketNumber(2).status(AttendanceStatus.SUCCESS).build();
        given(attendanceLogRepository.findByEventIdAndStatus(1L, AttendanceStatus.SUCCESS))
                .willReturn(List.of(first, second));

        LotteryResponse response = attendanceService.lottery(1L);

        assertThat(response.getStudentId()).isIn("20211111", "20212222");
        assertThat(response.getTicketNumber()).isIn(1, 2);
    }
}
