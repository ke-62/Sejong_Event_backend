package com.sejong.event.attendance.controller;

import com.sejong.event.attendance.dto.*;
import com.sejong.event.attendance.service.AttendanceService;
import com.sejong.event.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Attendance", description = "입장 관리 API")
@RestController
@RequestMapping("/api/events/{eventId}")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @Operation(summary = "운영진 QR 토큰 발급 (organizer 이상)")
    @GetMapping("/qr-token")
    public ResponseEntity<ApiResponse<QrTokenResponse>> getQrToken(@PathVariable Long eventId) {
        return ResponseEntity.ok(ApiResponse.ok("성공", attendanceService.generateQrToken(eventId)));
    }

    @Operation(summary = "QR 스캔 입장 처리 (인증 불필요)")
    @PostMapping("/attendance/scan")
    public ResponseEntity<ApiResponse<ScanResponse>> scan(
            @PathVariable Long eventId,
            @Valid @RequestBody ScanRequest request) {
        ScanResponse response = attendanceService.scan(eventId, request);
        String message = switch (response.getStatus()) {
            case "success" -> "입장이 확인되었습니다.";
            case "duplicate" -> "이미 입장한 학생입니다.";
            default -> "유효하지 않은 QR입니다.";
        };
        return ResponseEntity.ok(ApiResponse.ok(message, response));
    }

    @Operation(summary = "수동 번호표 발급 (organizer 이상)")
    @PostMapping("/attendance/manual")
    public ResponseEntity<ApiResponse<ManualTicketResponse>> manualIssue(
            @PathVariable Long eventId,
            @Valid @RequestBody ManualTicketRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("번호표가 발급되었습니다.", attendanceService.manualIssue(eventId, request)));
    }

    @Operation(summary = "입장 로그 조회 (organizer 이상)")
    @GetMapping("/attendance")
    public ResponseEntity<ApiResponse<AttendanceListResponse>> getAttendanceLogs(
            @PathVariable Long eventId) {
        return ResponseEntity.ok(ApiResponse.ok("성공", attendanceService.getAttendanceLogs(eventId)));
    }

    @Operation(summary = "추첨 실행 (organizer 이상)")
    @PostMapping("/lottery")
    public ResponseEntity<ApiResponse<LotteryResponse>> lottery(@PathVariable Long eventId) {
        return ResponseEntity.ok(ApiResponse.ok("추첨이 완료되었습니다.", attendanceService.lottery(eventId)));
    }
}
