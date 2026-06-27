package com.sejong.event.waiting.controller;

import com.sejong.event.common.response.ApiResponse;
import com.sejong.event.waiting.dto.*;
import com.sejong.event.waiting.service.WaitingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Waiting", description = "웨이팅 API")
@RestController
@RequestMapping("/api/booths/{boothId}/waiting")
@RequiredArgsConstructor
public class WaitingController {

    private final WaitingService waitingService;

    @Operation(summary = "웨이팅 목록 조회 (organizer 이상)")
    @GetMapping
    public ResponseEntity<ApiResponse<List<WaitingEntryResponse>>> getWaitingList(
            @PathVariable Long boothId) {
        return ResponseEntity.ok(ApiResponse.ok("성공", waitingService.getWaitingList(boothId)));
    }

    @Operation(summary = "웨이팅 대기열 공개 조회 — 이름·순번만 반환 (인증 불필요)")
    @GetMapping("/queue")
    public ResponseEntity<ApiResponse<List<WaitingQueueItem>>> getQueue(
            @PathVariable Long boothId) {
        return ResponseEntity.ok(ApiResponse.ok("성공", waitingService.getWaitingQueue(boothId)));
    }

    @Operation(summary = "웨이팅 등록 (인증 불필요)")
    @PostMapping
    public ResponseEntity<ApiResponse<WaitingRegisterResponse>> register(
            @PathVariable Long boothId,
            @Valid @RequestBody WaitingRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("웨이팅이 등록되었습니다.", waitingService.register(boothId, request)));
    }

    @Operation(summary = "다음 팀 호출 (organizer 이상)")
    @PostMapping("/next")
    public ResponseEntity<ApiResponse<NextWaitingResponse>> callNext(@PathVariable Long boothId) {
        return ResponseEntity.ok(ApiResponse.ok("다음 손님을 호출했습니다.", waitingService.callNext(boothId)));
    }

    @Operation(summary = "웨이팅 취소 (organizer 이상)")
    @DeleteMapping("/{waitingId}")
    public ResponseEntity<ApiResponse<Void>> cancel(
            @PathVariable Long boothId,
            @PathVariable Long waitingId) {
        waitingService.cancel(boothId, waitingId);
        return ResponseEntity.ok(ApiResponse.ok("웨이팅이 취소되었습니다.", null));
    }

    @Operation(summary = "내 웨이팅 상태 조회 (인증 불필요)")
    @GetMapping("/{waitingId}")
    public ResponseEntity<ApiResponse<WaitingStatusResponse>> getStatus(
            @PathVariable Long boothId,
            @PathVariable Long waitingId) {
        return ResponseEntity.ok(ApiResponse.ok("성공", waitingService.getStatus(boothId, waitingId)));
    }
}
