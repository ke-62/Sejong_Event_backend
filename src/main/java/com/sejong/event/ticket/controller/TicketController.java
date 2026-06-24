package com.sejong.event.ticket.controller;

import com.sejong.event.common.response.ApiResponse;
import com.sejong.event.ticket.dto.IssueTicketRequest;
import com.sejong.event.ticket.dto.QrScanRequest;
import com.sejong.event.ticket.dto.QrScanResponse;
import com.sejong.event.ticket.dto.TicketResponse;
import com.sejong.event.ticket.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Ticket (번호표)", description = "번호표 API — 발급·조회는 로그인 불필요, 스캔·추첨은 managerToken 필요")
@RestController
@RequestMapping("/api/events/{eventId}/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @Operation(summary = "번호표 발급 (누구나, 로그인 불필요)",
               description = "학번 입력 → 고유 번호 + QR 토큰 발급. 학번당 1장 제한.")
    @PostMapping
    public ResponseEntity<ApiResponse<TicketResponse>> issueTicket(
            @PathVariable Long eventId,
            @Valid @RequestBody IssueTicketRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("번호표가 발급되었습니다.",
                ticketService.issueTicket(eventId, request)));
    }

    @Operation(summary = "내 번호표 조회 (로그인 불필요)",
               description = "학번으로 본인 번호표를 조회. ?studentId=학번 쿼리 파라미터 필수.")
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<TicketResponse>> getMyTicket(
            @PathVariable Long eventId,
            @RequestParam String studentId) {
        return ResponseEntity.ok(ApiResponse.ok(ticketService.getMyTicket(eventId, studentId)));
    }

    @Operation(summary = "QR 스캔 입장 처리 (managerToken 필요)",
               description = "X-Manager-Token 헤더로 방 관리자 토큰 전달 필요.")
    @PostMapping("/scan")
    public ResponseEntity<ApiResponse<QrScanResponse>> scanQr(
            @PathVariable Long eventId,
            @RequestHeader("X-Manager-Token") String managerToken,
            @Valid @RequestBody QrScanRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                ticketService.scanQr(eventId, managerToken, request)));
    }

    @Operation(summary = "추첨 실행 (managerToken 필요)",
               description = "입장 확인된 참가자 중 랜덤 1명 선발.")
    @PostMapping("/draw")
    public ResponseEntity<ApiResponse<TicketResponse>> drawLottery(
            @PathVariable Long eventId,
            @RequestHeader("X-Manager-Token") String managerToken) {
        return ResponseEntity.ok(ApiResponse.ok("추첨이 완료되었습니다.",
                ticketService.drawLottery(eventId, managerToken)));
    }

    @Operation(summary = "번호표 전체 목록 조회 (managerToken 필요)",
               description = "행사 관리자용 전체 번호표 목록.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<TicketResponse>>> getEventTickets(
            @PathVariable Long eventId,
            @RequestHeader("X-Manager-Token") String managerToken) {
        return ResponseEntity.ok(ApiResponse.ok(
                ticketService.getEventTickets(eventId, managerToken)));
    }
}
