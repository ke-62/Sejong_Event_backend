package com.sejong.event.booth.controller;

import com.sejong.event.booth.dto.*;
import com.sejong.event.booth.service.BoothService;
import com.sejong.event.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Booth (부스)", description = "부스 신청(누구나) + 부스 관리(운영자 로그인 필요)")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BoothController {

    private final BoothService boothService;

    // ── 부스 신청 (로그인 불필요) ──────────────────────────────────────────

    @Operation(summary = "부스 신청 (누구나, 로그인 불필요)",
               description = "학번 + 비밀번호를 포함하여 신청. 관리자 승인 시 해당 학번+비밀번호로 로그인 가능.")
    @PostMapping("/festivals/{festivalId}/applications")
    public ResponseEntity<ApiResponse<BoothApplicationResponse>> applyBooth(
            @PathVariable Long festivalId,
            @Valid @RequestBody ApplyBoothRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("신청이 완료되었습니다. 관리자 승인을 기다려주세요.",
                boothService.applyBooth(festivalId, request)));
    }

    @Operation(summary = "축제 부스 신청 목록 조회 (공개)")
    @GetMapping("/festivals/{festivalId}/applications")
    public ResponseEntity<ApiResponse<List<BoothApplicationResponse>>> getApplications(
            @PathVariable Long festivalId) {
        return ResponseEntity.ok(ApiResponse.ok(boothService.getApplicationsByFestival(festivalId)));
    }

    // ── 부스 조회 (공개) ──────────────────────────────────────────

    @Operation(summary = "행사별 부스 목록 조회 (공개)")
    @GetMapping("/events/{eventId}/booths")
    public ResponseEntity<ApiResponse<List<BoothResponse>>> getBoothsByEvent(
            @PathVariable Long eventId) {
        return ResponseEntity.ok(ApiResponse.ok(boothService.getBoothsByEvent(eventId)));
    }

    @Operation(summary = "부스 상세 조회 (공개)")
    @GetMapping("/booths/{boothId}")
    public ResponseEntity<ApiResponse<BoothResponse>> getBooth(@PathVariable Long boothId) {
        return ResponseEntity.ok(ApiResponse.ok(boothService.getBooth(boothId)));
    }

    // ── 부스 관리 (BOOTH_OPERATOR 로그인 필요) ──────────────────────────────────────────

    @Operation(summary = "부스 정보 수정 (운영자 로그인 필요)")
    @PatchMapping("/booths/{boothId}")
    @PreAuthorize("hasAnyRole('BOOTH_OPERATOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<BoothResponse>> updateBooth(
            @PathVariable Long boothId,
            @AuthenticationPrincipal Long userId,
            @RequestBody UpdateBoothRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(boothService.updateBooth(boothId, userId, request)));
    }

    @Operation(summary = "혼잡도 업데이트 (운영자 로그인 필요) — WebSocket 브로드캐스트")
    @PatchMapping("/booths/{boothId}/congestion")
    @PreAuthorize("hasAnyRole('BOOTH_OPERATOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<BoothResponse>> updateCongestion(
            @PathVariable Long boothId,
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody UpdateCongestionRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(boothService.updateCongestion(boothId, userId, request)));
    }

    @Operation(summary = "영업 상태 토글 (운영자 로그인 필요) — WebSocket 브로드캐스트")
    @PatchMapping("/booths/{boothId}/toggle")
    @PreAuthorize("hasAnyRole('BOOTH_OPERATOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<BoothResponse>> toggleOpen(
            @PathVariable Long boothId,
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(boothService.toggleOpen(boothId, userId)));
    }

    @Operation(summary = "부스 삭제 (운영자 로그인 필요)")
    @DeleteMapping("/booths/{boothId}")
    @PreAuthorize("hasAnyRole('BOOTH_OPERATOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteBooth(
            @PathVariable Long boothId,
            @AuthenticationPrincipal Long userId) {
        boothService.deleteBooth(boothId, userId);
        return ResponseEntity.ok(ApiResponse.ok("부스가 삭제되었습니다.", null));
    }
}
