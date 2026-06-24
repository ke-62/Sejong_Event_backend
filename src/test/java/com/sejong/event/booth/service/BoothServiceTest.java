package com.sejong.event.booth.service;

import com.sejong.event.booth.domain.*;
import com.sejong.event.booth.dto.*;
import com.sejong.event.booth.repository.BoothApplicationRepository;
import com.sejong.event.booth.repository.BoothRepository;
import com.sejong.event.booth.repository.CongestionHistoryRepository;
import com.sejong.event.common.exception.CustomException;
import com.sejong.event.common.exception.ErrorCode;
import com.sejong.event.event.domain.Event;
import com.sejong.event.event.domain.EventMode;
import com.sejong.event.event.domain.EventStatus;
import com.sejong.event.event.repository.EventRepository;
import com.sejong.event.user.domain.Role;
import com.sejong.event.user.domain.User;
import com.sejong.event.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class BoothServiceTest {

    @InjectMocks
    private BoothService boothService;

    @Mock private BoothRepository boothRepository;
    @Mock private BoothApplicationRepository applicationRepository;
    @Mock private CongestionHistoryRepository congestionHistoryRepository;
    @Mock private EventRepository eventRepository;
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private SimpMessagingTemplate messagingTemplate;

    private User createOperator(Long id) {
        User user = User.builder()
                .studentId("23011679")
                .password("encoded")
                .name("부스운영자")
                .role(Role.BOOTH_OPERATOR)
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Event createFestivalEvent(Long id) {
        Event event = Event.builder()
                .title("세종연회").description("축제").mode(EventMode.FESTIVAL)
                .maxCapacity(null).managerToken(null)
                .startAt(LocalDateTime.now()).endAt(LocalDateTime.now().plusDays(1))
                .build();
        ReflectionTestUtils.setField(event, "id", id);
        ReflectionTestUtils.setField(event, "status", EventStatus.ACTIVE);
        return event;
    }

    private Booth createBooth(Long id, Event event, User operator) {
        Booth booth = Booth.builder()
                .event(event).operator(operator)
                .name("떡볶이부스").description("매운 떡볶이")
                .location("A구역").menu("떡볶이, 순대")
                .build();
        ReflectionTestUtils.setField(booth, "id", id);
        return booth;
    }

    private BoothApplication createPendingApplication(Long id, Event event) {
        BoothApplication app = BoothApplication.builder()
                .event(event)
                .applicantName("부스운영자")
                .studentId("23011679")
                .password("pass1234")
                .boothName("떡볶이부스")
                .description("매운 떡볶이")
                .location("A구역")
                .menu("떡볶이, 순대")
                .build();
        ReflectionTestUtils.setField(app, "id", id);
        return app;
    }

    // ── 부스 신청 ──────────────────────────────────────────

    @Test
    @DisplayName("부스 신청 성공")
    void applyBooth_success() {
        Event festival = createFestivalEvent(1L);
        given(eventRepository.findById(1L)).willReturn(Optional.of(festival));
        given(applicationRepository.existsByEventIdAndStudentId(1L, "23011679")).willReturn(false);

        BoothApplication saved = createPendingApplication(1L, festival);
        given(applicationRepository.save(any(BoothApplication.class))).willReturn(saved);

        ApplyBoothRequest request = mock(ApplyBoothRequest.class);
        given(request.getApplicantName()).willReturn("부스운영자");
        given(request.getStudentId()).willReturn("23011679");
        given(request.getPassword()).willReturn("pass1234");
        given(request.getBoothName()).willReturn("떡볶이부스");
        given(request.getDescription()).willReturn("매운 떡볶이");
        given(request.getLocation()).willReturn("A구역");
        given(request.getMenu()).willReturn("떡볶이, 순대");

        BoothApplicationResponse response = boothService.applyBooth(1L, request);

        assertThat(response.getStudentId()).isEqualTo("23011679");
        assertThat(response.getStatus()).isEqualTo(ApplicationStatus.PENDING);
    }

    @Test
    @DisplayName("부스 신청 실패 - 동일 학번 중복 신청")
    void applyBooth_duplicate() {
        Event festival = createFestivalEvent(1L);
        given(eventRepository.findById(1L)).willReturn(Optional.of(festival));
        given(applicationRepository.existsByEventIdAndStudentId(1L, "23011679")).willReturn(true);

        ApplyBoothRequest request = mock(ApplyBoothRequest.class);
        given(request.getStudentId()).willReturn("23011679");

        assertThatThrownBy(() -> boothService.applyBooth(1L, request))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.DUPLICATE_STUDENT_ID);
    }

    @Test
    @DisplayName("부스 신청 실패 - 행사 모드가 FESTIVAL이 아님")
    void applyBooth_notFestivalMode() {
        Event eventMode = Event.builder()
                .title("개강총회").description("").mode(EventMode.EVENT)
                .maxCapacity(300).managerToken("token")
                .startAt(LocalDateTime.now()).endAt(LocalDateTime.now().plusHours(3))
                .build();
        ReflectionTestUtils.setField(eventMode, "id", 1L);
        given(eventRepository.findById(1L)).willReturn(Optional.of(eventMode));

        assertThatThrownBy(() -> boothService.applyBooth(1L, mock(ApplyBoothRequest.class)))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_INPUT);
    }

    // ── 신청 승인 ──────────────────────────────────────────

    @Test
    @DisplayName("부스 신청 승인 성공 — 운영자 계정 + 부스 생성")
    void approveApplication_success() {
        Event festival = createFestivalEvent(1L);
        BoothApplication app = createPendingApplication(1L, festival);

        given(applicationRepository.findById(1L)).willReturn(Optional.of(app));
        given(userRepository.existsByStudentId("23011679")).willReturn(false);
        given(passwordEncoder.encode("pass1234")).willReturn("encodedPass");

        User savedOperator = createOperator(10L);
        given(userRepository.save(any(User.class))).willReturn(savedOperator);

        Booth savedBooth = createBooth(5L, festival, savedOperator);
        given(boothRepository.save(any(Booth.class))).willReturn(savedBooth);

        BoothApplicationResponse response = boothService.approveApplication(1L);

        assertThat(response.getStatus()).isEqualTo(ApplicationStatus.APPROVED);
        then(userRepository).should().save(any(User.class));
        then(boothRepository).should().save(any(Booth.class));
    }

    @Test
    @DisplayName("부스 신청 승인 실패 - 이미 처리된 신청")
    void approveApplication_alreadyProcessed() {
        Event festival = createFestivalEvent(1L);
        BoothApplication app = createPendingApplication(1L, festival);
        app.approve(5L); // 이미 승인됨

        given(applicationRepository.findById(1L)).willReturn(Optional.of(app));

        assertThatThrownBy(() -> boothService.approveApplication(1L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.APPLICATION_ALREADY_PROCESSED);
    }

    // ── 부스 조회 ──────────────────────────────────────────

    @Test
    @DisplayName("행사별 부스 목록 조회 성공")
    void getBoothsByEvent_success() {
        Event event = createFestivalEvent(1L);
        User operator = createOperator(1L);
        List<Booth> booths = List.of(
                createBooth(1L, event, operator),
                createBooth(2L, event, operator)
        );
        given(boothRepository.findByEventId(1L)).willReturn(booths);

        List<BoothResponse> responses = boothService.getBoothsByEvent(1L);

        assertThat(responses).hasSize(2);
    }

    @Test
    @DisplayName("부스 상세 조회 실패 - 존재하지 않는 부스")
    void getBooth_notFound() {
        given(boothRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> boothService.getBooth(99L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.BOOTH_NOT_FOUND);
    }

    // ── 혼잡도 업데이트 ──────────────────────────────────────────

    @Test
    @DisplayName("혼잡도 업데이트 성공 - WebSocket 브로드캐스트 포함")
    void updateCongestion_success() {
        Event event = createFestivalEvent(1L);
        User operator = createOperator(1L);
        Booth booth = createBooth(1L, event, operator);
        given(boothRepository.findById(1L)).willReturn(Optional.of(booth));
        given(congestionHistoryRepository.save(any(CongestionHistory.class)))
                .willReturn(mock(CongestionHistory.class));

        UpdateCongestionRequest request = mock(UpdateCongestionRequest.class);
        given(request.getWaitingCount()).willReturn(25);

        BoothResponse response = boothService.updateCongestion(1L, 1L, request);

        assertThat(response.getWaitingCount()).isEqualTo(25);
        assertThat(response.getCongestionStatus()).isEqualTo(CongestionStatus.MEDIUM);
        then(messagingTemplate).should().convertAndSend(eq("/topic/booth/1"), anyMap());
    }

    @Test
    @DisplayName("혼잡도 업데이트 실패 - 운영자가 아님")
    void updateCongestion_noPermission() {
        Event event = createFestivalEvent(1L);
        User operator = createOperator(1L);
        Booth booth = createBooth(1L, event, operator);
        given(boothRepository.findById(1L)).willReturn(Optional.of(booth));

        assertThatThrownBy(() -> boothService.updateCongestion(1L, 99L, mock(UpdateCongestionRequest.class)))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.BOOTH_ACCESS_DENIED);
    }

    @Test
    @DisplayName("혼잡도 - 대기 인원에 따른 상태 자동 분류")
    void updateCongestion_statusClassification() {
        Event event = createFestivalEvent(1L);
        User operator = createOperator(1L);

        // LOW (0~9명)
        Booth boothLow = createBooth(1L, event, operator);
        given(boothRepository.findById(1L)).willReturn(Optional.of(boothLow));
        given(congestionHistoryRepository.save(any())).willReturn(mock(CongestionHistory.class));
        UpdateCongestionRequest lowReq = mock(UpdateCongestionRequest.class);
        given(lowReq.getWaitingCount()).willReturn(5);
        assertThat(boothService.updateCongestion(1L, 1L, lowReq).getCongestionStatus())
                .isEqualTo(CongestionStatus.LOW);

        // HIGH (30명 이상)
        Booth boothHigh = createBooth(1L, event, operator);
        given(boothRepository.findById(1L)).willReturn(Optional.of(boothHigh));
        UpdateCongestionRequest highReq = mock(UpdateCongestionRequest.class);
        given(highReq.getWaitingCount()).willReturn(50);
        assertThat(boothService.updateCongestion(1L, 1L, highReq).getCongestionStatus())
                .isEqualTo(CongestionStatus.HIGH);
    }

    // ── 영업 상태 토글 ──────────────────────────────────────────

    @Test
    @DisplayName("영업 상태 토글 성공 (오픈 → 마감)")
    void toggleOpen_success() {
        Event event = createFestivalEvent(1L);
        User operator = createOperator(1L);
        Booth booth = createBooth(1L, event, operator); // 기본 isOpen = true
        given(boothRepository.findById(1L)).willReturn(Optional.of(booth));

        BoothResponse response = boothService.toggleOpen(1L, 1L);

        assertThat(response.getIsOpen()).isFalse();
        then(messagingTemplate).should().convertAndSend(eq("/topic/booth/1"), anyMap());
    }

    // ── 부스 삭제 ──────────────────────────────────────────

    @Test
    @DisplayName("부스 삭제 성공")
    void deleteBooth_success() {
        Event event = createFestivalEvent(1L);
        User operator = createOperator(1L);
        Booth booth = createBooth(1L, event, operator);
        given(boothRepository.findById(1L)).willReturn(Optional.of(booth));

        boothService.deleteBooth(1L, 1L);

        then(boothRepository).should().delete(booth);
    }

    @Test
    @DisplayName("부스 삭제 실패 - 운영자가 아님")
    void deleteBooth_noPermission() {
        Event event = createFestivalEvent(1L);
        User operator = createOperator(1L);
        Booth booth = createBooth(1L, event, operator);
        given(boothRepository.findById(1L)).willReturn(Optional.of(booth));

        assertThatThrownBy(() -> boothService.deleteBooth(1L, 99L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.BOOTH_ACCESS_DENIED);
    }

    // ── 부스 정보 수정 ──────────────────────────────────────────

    @Test
    @DisplayName("부스 정보 수정 성공")
    void updateBooth_success() {
        Event event = createFestivalEvent(1L);
        User operator = createOperator(1L);
        Booth booth = createBooth(1L, event, operator);
        given(boothRepository.findById(1L)).willReturn(Optional.of(booth));

        UpdateBoothRequest request = mock(UpdateBoothRequest.class);
        given(request.getName()).willReturn("새 부스 이름");
        given(request.getDescription()).willReturn(null);
        given(request.getLocation()).willReturn("B구역");
        given(request.getMenu()).willReturn(null);

        BoothResponse response = boothService.updateBooth(1L, 1L, request);

        assertThat(response.getName()).isEqualTo("새 부스 이름");
        assertThat(response.getLocation()).isEqualTo("B구역");
    }
}
