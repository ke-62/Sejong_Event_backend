package com.sejong.event.booth.service;

import com.sejong.event.booth.domain.ApplicationStatus;
import com.sejong.event.booth.domain.Booth;
import com.sejong.event.booth.domain.BoothApplication;
import com.sejong.event.booth.domain.CongestionHistory;
import com.sejong.event.booth.dto.ApplyBoothRequest;
import com.sejong.event.booth.dto.BoothApplicationResponse;
import com.sejong.event.booth.dto.BoothResponse;
import com.sejong.event.booth.dto.UpdateBoothRequest;
import com.sejong.event.booth.dto.UpdateCongestionRequest;
import com.sejong.event.booth.repository.BoothApplicationRepository;
import com.sejong.event.booth.repository.BoothRepository;
import com.sejong.event.booth.repository.CongestionHistoryRepository;
import com.sejong.event.common.exception.CustomException;
import com.sejong.event.common.exception.ErrorCode;
import com.sejong.event.event.domain.Event;
import com.sejong.event.event.domain.EventMode;
import com.sejong.event.event.repository.EventRepository;
import com.sejong.event.user.domain.Role;
import com.sejong.event.user.domain.User;
import com.sejong.event.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoothService {

    private final BoothRepository boothRepository;
    private final BoothApplicationRepository applicationRepository;
    private final CongestionHistoryRepository congestionHistoryRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SimpMessagingTemplate messagingTemplate;

    // ── 부스 신청 (로그인 불필요) ────────────────────────────────

    /**
     * 부스 신청 — 누구나 가능, 로그인 불필요.
     * 관리자가 승인하면 studentId + password 로 로그인 가능한 계정이 생성된다.
     */
    @Transactional
    public BoothApplicationResponse applyBooth(Long festivalId, ApplyBoothRequest request) {
        Event festival = eventRepository.findById(festivalId)
                .orElseThrow(() -> new CustomException(ErrorCode.EVENT_NOT_FOUND));

        if (festival.getMode() != EventMode.FESTIVAL) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }

        // 동일 축제에 동일 학번 중복 신청 방지
        if (applicationRepository.existsByEventIdAndStudentId(festivalId, request.getStudentId())) {
            throw new CustomException(ErrorCode.DUPLICATE_STUDENT_ID);
        }

        BoothApplication application = BoothApplication.builder()
                .event(festival)
                .applicantName(request.getApplicantName())
                .studentId(request.getStudentId())
                .password(request.getPassword()) // 평문 보관 → 승인 시 bcrypt 적용
                .boothName(request.getBoothName())
                .description(request.getDescription())
                .location(request.getLocation())
                .menu(request.getMenu())
                .build();

        return BoothApplicationResponse.from(applicationRepository.save(application));
    }

    public List<BoothApplicationResponse> getApplicationsByFestival(Long festivalId) {
        return applicationRepository.findByEventId(festivalId).stream()
                .map(BoothApplicationResponse::from)
                .toList();
    }

    /**
     * 신청 승인 — 관리자 전용.
     * User(BOOTH_OPERATOR) + Booth 생성 후 application.status = APPROVED
     */
    @Transactional
    public BoothApplicationResponse approveApplication(Long applicationId) {
        BoothApplication application = findApplication(applicationId);

        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new CustomException(ErrorCode.APPLICATION_ALREADY_PROCESSED);
        }

        // 학번 중복 계정 방지
        if (userRepository.existsByStudentId(application.getStudentId())) {
            throw new CustomException(ErrorCode.DUPLICATE_STUDENT_ID);
        }

        // 부스 운영자 계정 생성
        User operator = User.builder()
                .studentId(application.getStudentId())
                .password(passwordEncoder.encode(application.getPassword()))
                .name(application.getApplicantName())
                .role(Role.BOOTH_OPERATOR)
                .build();
        operator = userRepository.save(operator);

        // 부스 생성
        Booth booth = Booth.builder()
                .event(application.getEvent())
                .operator(operator)
                .name(application.getBoothName())
                .description(application.getDescription())
                .location(application.getLocation())
                .menu(application.getMenu())
                .build();
        booth = boothRepository.save(booth);

        application.approve(booth.getId());
        return BoothApplicationResponse.from(application);
    }

    /** 신청 반려 — 관리자 전용 */
    @Transactional
    public BoothApplicationResponse rejectApplication(Long applicationId, String reason) {
        BoothApplication application = findApplication(applicationId);
        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new CustomException(ErrorCode.APPLICATION_ALREADY_PROCESSED);
        }
        application.reject(reason);
        return BoothApplicationResponse.from(application);
    }

    // ── 부스 조회 ────────────────────────────────

    public List<BoothResponse> getBoothsByEvent(Long eventId) {
        return boothRepository.findByEventId(eventId).stream()
                .map(BoothResponse::from)
                .toList();
    }

    public BoothResponse getBooth(Long boothId) {
        return BoothResponse.from(findBooth(boothId));
    }

    // ── 부스 관리 (BOOTH_OPERATOR 로그인 후) ────────────────────────────────

    @Transactional
    public BoothResponse updateBooth(Long boothId, Long operatorId, UpdateBoothRequest request) {
        Booth booth = getBoothWithOwnerCheck(boothId, operatorId);
        booth.updateInfo(request.getName(), request.getDescription(),
                request.getLocation(), request.getMenu());
        return BoothResponse.from(booth);
    }

    /** 혼잡도 업데이트 + WebSocket 브로드캐스트 (/topic/booth/{id}) */
    @Transactional
    public BoothResponse updateCongestion(Long boothId, Long operatorId, UpdateCongestionRequest request) {
        Booth booth = getBoothWithOwnerCheck(boothId, operatorId);
        booth.updateCongestion(request.getWaitingCount());

        congestionHistoryRepository.save(CongestionHistory.builder()
                .booth(booth)
                .waitingCount(request.getWaitingCount())
                .status(booth.getCongestionStatus())
                .build());

        broadcastBoothStatus(boothId, booth);
        return BoothResponse.from(booth);
    }

    /** 영업 상태 토글 + WebSocket 브로드캐스트 */
    @Transactional
    public BoothResponse toggleOpen(Long boothId, Long operatorId) {
        Booth booth = getBoothWithOwnerCheck(boothId, operatorId);
        booth.toggleOpen();
        broadcastBoothStatus(boothId, booth);
        return BoothResponse.from(booth);
    }

    @Transactional
    public void deleteBooth(Long boothId, Long operatorId) {
        boothRepository.delete(getBoothWithOwnerCheck(boothId, operatorId));
    }

    /** 관리자 전용 — 전체 부스 목록 */
    public List<BoothResponse> getAllBooths() {
        return boothRepository.findAll().stream()
                .map(BoothResponse::from)
                .toList();
    }

    /** 관리자 전용 — 부스 강제 삭제 */
    @Transactional
    public void forceDeleteBooth(Long boothId) {
        boothRepository.delete(findBooth(boothId));
    }

    private void broadcastBoothStatus(Long boothId, Booth booth) {
        messagingTemplate.convertAndSend(
                "/topic/booth/" + boothId,
                Map.of(
                        "boothId", boothId,
                        "waitingCount", booth.getWaitingCount(),
                        "congestionStatus", booth.getCongestionStatus().name(),
                        "isOpen", booth.getIsOpen()
                )
        );
    }

    private Booth getBoothWithOwnerCheck(Long boothId, Long userId) {
        Booth booth = findBooth(boothId);
        if (!booth.isOperator(userId)) {
            throw new CustomException(ErrorCode.BOOTH_ACCESS_DENIED);
        }
        return booth;
    }

    private Booth findBooth(Long boothId) {
        return boothRepository.findById(boothId)
                .orElseThrow(() -> new CustomException(ErrorCode.BOOTH_NOT_FOUND));
    }

    private BoothApplication findApplication(Long applicationId) {
        return applicationRepository.findById(applicationId)
                .orElseThrow(() -> new CustomException(ErrorCode.APPLICATION_NOT_FOUND));
    }
}
