package com.sejong.event.booth.controller;

import com.sejong.event.booth.dto.*;
import com.sejong.event.booth.service.BoothService;
import com.sejong.event.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Booths", description = "부스 API")
@RestController
@RequestMapping("/api/booths")
@RequiredArgsConstructor
public class BoothController {

    private final BoothService boothService;

    @Operation(summary = "부스 목록 조회 (인증 불필요), status=all 이면 전체 반환")
    @GetMapping
    public ResponseEntity<ApiResponse<List<BoothResponse>>> getBooths(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(ApiResponse.ok("성공", boothService.getBooths(category, status)));
    }

    @Operation(summary = "내 기관 부스 전체 조회 — PENDING 포함 (organizer 이상)")
    @GetMapping("/mine")
    public ResponseEntity<ApiResponse<List<BoothResponse>>> getMyBooths(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.ok("성공", boothService.getMyBooths(userId)));
    }

    @Operation(summary = "부스 상세 조회 (인증 불필요)")
    @GetMapping("/{boothId}")
    public ResponseEntity<ApiResponse<BoothResponse>> getBooth(@PathVariable Long boothId) {
        return ResponseEntity.ok(ApiResponse.ok("성공", boothService.getBooth(boothId)));
    }

    @Operation(summary = "부스 신청 (organizer 이상)")
    @PostMapping
    public ResponseEntity<ApiResponse<BoothResponse>> createBooth(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody CreateBoothRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("부스 신청이 완료되었습니다. 승인 후 목록에 표시됩니다.",
                        boothService.createBooth(userId, request)));
    }

    @Operation(summary = "부스 수정")
    @PutMapping("/{boothId}")
    public ResponseEntity<ApiResponse<BoothResponse>> updateBooth(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long boothId,
            @RequestBody UpdateBoothRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("성공", boothService.updateBooth(userId, boothId, request)));
    }

    @Operation(summary = "부스 삭제")
    @DeleteMapping("/{boothId}")
    public ResponseEntity<ApiResponse<Void>> deleteBooth(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long boothId) {
        boothService.deleteBooth(userId, boothId);
        return ResponseEntity.ok(ApiResponse.ok("부스가 삭제되었습니다.", null));
    }

    @Operation(summary = "부스 신청 승인 (superuser)")
    @PatchMapping("/{boothId}/approve")
    public ResponseEntity<ApiResponse<Map<String, Object>>> approveBooth(@PathVariable Long boothId) {
        return ResponseEntity.ok(ApiResponse.ok("부스 신청이 승인되었습니다.", boothService.approveBooth(boothId)));
    }

    @Operation(summary = "부스 신청 거부 (superuser)")
    @PatchMapping("/{boothId}/reject")
    public ResponseEntity<ApiResponse<Map<String, Object>>> rejectBooth(@PathVariable Long boothId) {
        return ResponseEntity.ok(ApiResponse.ok("부스 신청이 거부되었습니다.", boothService.rejectBooth(boothId)));
    }

    @Operation(summary = "영업 상태 변경 (organizer 이상)")
    @PatchMapping("/{boothId}/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateStatus(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long boothId,
            @Valid @RequestBody UpdateBoothStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("영업 상태가 변경되었습니다.",
                boothService.updateBoothStatus(userId, boothId, request)));
    }
}
