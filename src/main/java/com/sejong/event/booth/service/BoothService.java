package com.sejong.event.booth.service;

import com.sejong.event.booth.domain.Booth;
import com.sejong.event.booth.domain.BoothStatus;
import com.sejong.event.booth.dto.*;
import com.sejong.event.booth.repository.BoothRepository;
import com.sejong.event.common.exception.CustomException;
import com.sejong.event.common.exception.ErrorCode;
import com.sejong.event.user.domain.Role;
import com.sejong.event.user.domain.User;
import com.sejong.event.user.repository.UserRepository;
import com.sejong.event.waiting.repository.WaitingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoothService {

    private static final double AVG_MINUTES_PER_PARTY = 1.5;

    private final BoothRepository boothRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final WaitingRepository waitingRepository;

    /** GET /booths — 승인된 부스 목록 (인증 불필요), status=all 이면 전체 반환 */
    public List<BoothResponse> getBooths(String category, String status) {
        if ("all".equalsIgnoreCase(status)) {
            return boothRepository.findAllByOrderByStatusAscIdDesc()
                    .stream().map(BoothResponse::from).toList();
        }
        List<Booth> booths;
        if (category != null && !category.isBlank()) {
            booths = boothRepository.findByCategoryAndStatusOrderByWaitingDesc(
                    category, BoothStatus.APPROVED);
        } else {
            booths = boothRepository.findByStatusOrderByWaitingDesc(BoothStatus.APPROVED);
        }
        return booths.stream().map(BoothResponse::from).toList();
    }

    /** GET /booths/mine — 내 기관 부스 전체 조회 (PENDING 포함) */
    public List<BoothResponse> getMyBooths(Long userId) {
        User user = findUser(userId);
        return boothRepository.findByOrganization(user.getOrganization())
                .stream().map(BoothResponse::from).toList();
    }

    /** GET /booths/{boothId} — 부스 상세 (인증 불필요) */
    public BoothResponse getBooth(Long boothId) {
        Booth booth = findBooth(boothId);
        return BoothResponse.withAvgMinutes(booth, AVG_MINUTES_PER_PARTY);
    }

    /** POST /booths — 부스 신청 (organizer) */
    @Transactional
    public BoothResponse createBooth(Long userId, CreateBoothRequest request) {
        User user = findUser(userId);
        int count = boothRepository.countByStatus(BoothStatus.APPROVED)
                + boothRepository.countByStatus(BoothStatus.PENDING);

        Booth booth = Booth.builder()
                .name(request.getName())
                .category(request.getCategory())
                .location(request.getLocation())
                .emoji(request.getEmoji())
                .organization(user.getOrganization())
                .description(request.getDescription())
                .createdIndex(count)
                .build();

        return BoothResponse.from(boothRepository.save(booth));
    }

    /** PUT /booths/{boothId} — 부스 수정 */
    @Transactional
    public BoothResponse updateBooth(Long userId, Long boothId, UpdateBoothRequest request) {
        User user = findUser(userId);
        Booth booth = findBooth(boothId);
        checkBoothAccess(user, booth);

        String newOrg = (user.getRole() == Role.SUPERUSER) ? request.getOrganization() : null;
        booth.updateInfo(request.getName(), request.getCategory(), request.getLocation(),
                request.getEmoji(), request.getDescription(), newOrg);

        return BoothResponse.from(booth);
    }

    /** DELETE /booths/{boothId} */
    @Transactional
    public void deleteBooth(Long userId, Long boothId) {
        User user = findUser(userId);
        Booth booth = findBooth(boothId);
        checkBoothAccess(user, booth);
        waitingRepository.deleteByBoothId(boothId);
        boothRepository.delete(booth);
    }

    /** PATCH /booths/{boothId}/approve — 승인 (superuser) */
    @Transactional
    public Map<String, Object> approveBooth(Long boothId) {
        Booth booth = findBooth(boothId);
        booth.approve();
        return Map.of("id", booth.getId(), "status", "approved");
    }

    /** PATCH /booths/{boothId}/reject — 거부 (superuser) */
    @Transactional
    public Map<String, Object> rejectBooth(Long boothId) {
        Booth booth = findBooth(boothId);
        booth.reject();
        return Map.of("id", booth.getId(), "status", "rejected");
    }

    /** PATCH /booths/{boothId}/status — 영업 상태 변경 */
    @Transactional
    public Map<String, Object> updateBoothStatus(Long userId, Long boothId, UpdateBoothStatusRequest request) {
        User user = findUser(userId);
        Booth booth = findBooth(boothId);
        checkBoothAccess(user, booth);

        booth.updateStatus(request.getIsOpen());

        messagingTemplate.convertAndSend(
                "/topic/booths/" + boothId,
                Map.of("boothId", boothId, "waitingCount", booth.getWaiting(), "isOpen", booth.getIsOpen())
        );

        return Map.of("id", booth.getId(), "isOpen", booth.getIsOpen());
    }

    public Booth findBooth(Long boothId) {
        return boothRepository.findById(boothId)
                .orElseThrow(() -> new CustomException(ErrorCode.BOOTH_NOT_FOUND));
    }

    public void broadcastBoothWaiting(Long boothId, Booth booth) {
        messagingTemplate.convertAndSend(
                "/topic/booths/" + boothId,
                Map.of("boothId", boothId, "waitingCount", booth.getWaiting(), "isOpen", booth.getIsOpen())
        );
    }

    private void checkBoothAccess(User user, Booth booth) {
        if (user.getRole() != Role.SUPERUSER
                && !booth.isManagedBy(user.getOrganization())) {
            throw new CustomException(ErrorCode.BOOTH_ACCESS_DENIED);
        }
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }
}
