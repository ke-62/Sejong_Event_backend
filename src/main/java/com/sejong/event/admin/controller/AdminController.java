package com.sejong.event.admin.controller;

import com.sejong.event.admin.dto.UpdateRoleRequest;
import com.sejong.event.admin.service.AdminService;
import com.sejong.event.booth.dto.BoothApplicationResponse;
import com.sejong.event.booth.dto.BoothResponse;
import com.sejong.event.booth.dto.RejectApplicationRequest;
import com.sejong.event.common.response.ApiResponse;
import com.sejong.event.event.dto.CreateEventRequest;
import com.sejong.event.event.dto.EventResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Admin", description = "관리자 전용 API")
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // ── 사용자 관리 ──────────────────────────────────────────

    @Operation(summary = "전체 사용자 목록 조회")
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getAllUsers()));
    }

    @Operation(summary = "사용자 역할 변경")
    @PatchMapping("/users/{userId}/role")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateUserRole(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateRoleRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.updateUserRole(userId, request)));
    }

    @Operation(summary = "사용자 삭제")
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long userId) {
        adminService.deleteUser(userId);
        return ResponseEntity.ok(ApiResponse.ok("사용자가 삭제되었습니다.", null));
    }

    // ── 행사 관리 ──────────────────────────────────────────

    @Operation(summary = "전체 행사 목록 조회 (관리자)")
    @GetMapping("/events")
    public ResponseEntity<ApiResponse<List<EventResponse>>> getAllEvents() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getAllEvents()));
    }

    @Operation(summary = "축제(FESTIVAL) 생성 — 관리자 전용",
               description = "관리자가 축제를 생성하면 부스 신청을 받을 수 있는 상태가 됩니다.")
    @PostMapping("/festivals")
    public ResponseEntity<ApiResponse<EventResponse>> createFestival(
            @Valid @RequestBody CreateEventRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("축제가 생성되었습니다.", adminService.createFestival(request)));
    }

    @Operation(summary = "행사 강제 삭제")
    @DeleteMapping("/events/{eventId}")
    public ResponseEntity<ApiResponse<Void>> forceDeleteEvent(@PathVariable Long eventId) {
        adminService.forceDeleteEvent(eventId);
        return ResponseEntity.ok(ApiResponse.ok("행사가 삭제되었습니다.", null));
    }

    // ── 부스 신청 관리 ──────────────────────────────────────────

    @Operation(summary = "축제별 부스 신청 목록 조회 (관리자)")
    @GetMapping("/festivals/{festivalId}/applications")
    public ResponseEntity<ApiResponse<List<BoothApplicationResponse>>> getApplicationsByFestival(
            @PathVariable Long festivalId) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getApplicationsByFestival(festivalId)));
    }

    @Operation(summary = "부스 신청 승인 — 운영자 계정 + 부스 자동 생성")
    @PostMapping("/applications/{applicationId}/approve")
    public ResponseEntity<ApiResponse<BoothApplicationResponse>> approveApplication(
            @PathVariable Long applicationId) {
        return ResponseEntity.ok(ApiResponse.ok("신청이 승인되었습니다.", adminService.approveApplication(applicationId)));
    }

    @Operation(summary = "부스 신청 반려")
    @PostMapping("/applications/{applicationId}/reject")
    public ResponseEntity<ApiResponse<BoothApplicationResponse>> rejectApplication(
            @PathVariable Long applicationId,
            @RequestBody RejectApplicationRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("신청이 반려되었습니다.", adminService.rejectApplication(applicationId, request)));
    }

    // ── 부스 관리 ──────────────────────────────────────────

    @Operation(summary = "전체 부스 목록 조회 (관리자)")
    @GetMapping("/booths")
    public ResponseEntity<ApiResponse<List<BoothResponse>>> getAllBooths() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getAllBooths()));
    }

    @Operation(summary = "부스 강제 삭제")
    @DeleteMapping("/booths/{boothId}")
    public ResponseEntity<ApiResponse<Void>> forceDeleteBooth(@PathVariable Long boothId) {
        adminService.forceDeleteBooth(boothId);
        return ResponseEntity.ok(ApiResponse.ok("부스가 삭제되었습니다.", null));
    }
}
