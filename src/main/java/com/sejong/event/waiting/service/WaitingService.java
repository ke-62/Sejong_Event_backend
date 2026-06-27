package com.sejong.event.waiting.service;

import com.sejong.event.booth.domain.Booth;
import com.sejong.event.booth.service.BoothService;
import com.sejong.event.common.exception.CustomException;
import com.sejong.event.common.exception.ErrorCode;
import com.sejong.event.waiting.domain.WaitingEntry;
import com.sejong.event.waiting.domain.WaitingStatus;
import com.sejong.event.waiting.dto.*;
import com.sejong.event.waiting.repository.WaitingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WaitingService {

    private static final double AVG_MINUTES_PER_PARTY = 1.5;

    private final WaitingRepository waitingRepository;
    private final BoothService boothService;

    /** GET /booths/{boothId}/waiting — 웨이팅 목록 */
    public List<WaitingEntryResponse> getWaitingList(Long boothId) {
        return waitingRepository.findByBoothIdOrderByOrderAsc(boothId).stream()
                .map(WaitingEntryResponse::from)
                .toList();
    }

    /** GET /booths/{boothId}/waiting/queue — 대기열 공개 조회 (이름·순번·인원만 반환) */
    public List<WaitingQueueItem> getWaitingQueue(Long boothId) {
        return waitingRepository.findByBoothIdAndStatusOrderByOrderAsc(boothId, WaitingStatus.WAITING)
                .stream()
                .map(WaitingQueueItem::from)
                .toList();
    }

    /** POST /booths/{boothId}/waiting — 웨이팅 등록 (인증 불필요) */
    @Transactional
    public WaitingRegisterResponse register(Long boothId, WaitingRegisterRequest request) {
        Booth booth = boothService.findBooth(boothId);

        int nextOrder = waitingRepository.countByBoothId(boothId) + 1;

        WaitingEntry entry = WaitingEntry.builder()
                .booth(booth)
                .order(nextOrder)
                .name(request.getName())
                .phone(request.getPhone())
                .partySize(request.getPartySize())
                .build();

        entry = waitingRepository.save(entry);

        int waitingAhead = waitingRepository.countByBoothIdAndStatus(boothId, WaitingStatus.WAITING) - 1;
        double estimatedMinutes = waitingAhead * AVG_MINUTES_PER_PARTY;

        // 부스 waiting 카운트 업데이트
        int totalWaiting = waitingRepository.countByBoothIdAndStatus(boothId, WaitingStatus.WAITING);
        booth.setWaiting(totalWaiting);
        boothService.broadcastBoothWaiting(boothId, booth);

        return WaitingRegisterResponse.builder()
                .id(entry.getId())
                .order(entry.getOrder())
                .name(entry.getName())
                .partySize(entry.getPartySize())
                .status(entry.getStatus().name())
                .waitingAhead(waitingAhead)
                .estimatedMinutes(estimatedMinutes)
                .build();
    }

    /** POST /booths/{boothId}/waiting/next — 다음 팀 호출 */
    @Transactional
    public NextWaitingResponse callNext(Long boothId) {
        WaitingEntry next = waitingRepository
                .findFirstByBoothIdAndStatusOrderByOrderAsc(boothId, WaitingStatus.WAITING)
                .orElseThrow(() -> new CustomException(ErrorCode.WAITING_NOT_FOUND));

        next.call();

        Booth booth = boothService.findBooth(boothId);
        int remaining = waitingRepository.countByBoothIdAndStatus(boothId, WaitingStatus.WAITING);
        booth.setWaiting(remaining);
        boothService.broadcastBoothWaiting(boothId, booth);

        return NextWaitingResponse.builder()
                .calledId(next.getId())
                .remainingCount(remaining)
                .build();
    }

    /** DELETE /booths/{boothId}/waiting/{waitingId} — 웨이팅 취소 */
    @Transactional
    public void cancel(Long boothId, Long waitingId) {
        WaitingEntry entry = waitingRepository.findById(waitingId)
                .orElseThrow(() -> new CustomException(ErrorCode.WAITING_NOT_FOUND));

        if (!entry.getBooth().getId().equals(boothId)) {
            throw new CustomException(ErrorCode.BOOTH_ACCESS_DENIED);
        }

        entry.cancel();

        Booth booth = boothService.findBooth(boothId);
        int remaining = waitingRepository.countByBoothIdAndStatus(boothId, WaitingStatus.WAITING);
        booth.setWaiting(remaining);
        boothService.broadcastBoothWaiting(boothId, booth);
    }

    /** GET /booths/{boothId}/waiting/{waitingId} — 내 웨이팅 상태 */
    public WaitingStatusResponse getStatus(Long boothId, Long waitingId) {
        WaitingEntry entry = waitingRepository.findById(waitingId)
                .orElseThrow(() -> new CustomException(ErrorCode.WAITING_NOT_FOUND));

        Integer calledOrder = waitingRepository
                .findFirstByBoothIdAndStatusOrderByOrderDesc(boothId, WaitingStatus.CALLED)
                .map(WaitingEntry::getOrder)
                .orElse(0);

        int ahead = waitingRepository.countByBoothIdAndStatusAndOrderLessThan(
                boothId, WaitingStatus.WAITING, entry.getOrder());

        return WaitingStatusResponse.builder()
                .id(entry.getId())
                .myOrder(entry.getOrder())
                .calledOrder(calledOrder)
                .ahead(ahead)
                .estimatedMinutes(ahead * AVG_MINUTES_PER_PARTY)
                .status(entry.getStatus().name())
                .build();
    }
}
