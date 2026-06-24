package com.sejong.event.event.controller;

import com.sejong.event.common.response.ApiResponse;
import com.sejong.event.event.dto.CreateEventRequest;
import com.sejong.event.event.dto.CreateEventResponse;
import com.sejong.event.event.dto.EventResponse;
import com.sejong.event.event.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    // ── 행사 모드 (EVENT) — 로그인 불필요 ────────────────────────────────

    @Tag(name = "Event (행사 모드)")
    @Operation(summary = "행사 방 생성 (누구나, 로그인 불필요)",
               description = "반환된 managerToken은 분실 시 복구 불가. localStorage에 저장 필수.")
    @PostMapping("/api/events")
    public ResponseEntity<ApiResponse<CreateEventResponse>> createEventRoom(
            @Valid @RequestBody CreateEventRequest request,
            HttpServletRequest httpRequest) {
        String baseUrl = httpRequest.getScheme() + "://" + httpRequest.getServerName()
                + ":" + httpRequest.getServerPort();
        return ResponseEntity.ok(ApiResponse.ok("행사 방이 생성되었습니다.",
                eventService.createEventRoom(request, baseUrl)));
    }

    @Tag(name = "Event (행사 모드)")
    @Operation(summary = "진행 중인 행사 방 목록 조회 (EVENT 모드)")
    @GetMapping("/api/events")
    public ResponseEntity<ApiResponse<List<EventResponse>>> getActiveEventRooms() {
        return ResponseEntity.ok(ApiResponse.ok(eventService.getActiveEventRooms()));
    }

    @Tag(name = "Event (행사 모드)")
    @Operation(summary = "행사 상세 조회")
    @GetMapping("/api/events/{eventId}")
    public ResponseEntity<ApiResponse<EventResponse>> getEvent(@PathVariable Long eventId) {
        return ResponseEntity.ok(ApiResponse.ok(eventService.getEvent(eventId)));
    }

    @Tag(name = "Event (행사 모드)")
    @Operation(summary = "행사 종료 (managerToken 필요)",
               description = "방 생성 시 받은 managerToken을 X-Manager-Token 헤더로 전달.")
    @PatchMapping("/api/events/{eventId}/close")
    public ResponseEntity<ApiResponse<EventResponse>> closeEvent(
            @PathVariable Long eventId,
            @RequestHeader("X-Manager-Token") String managerToken) {
        return ResponseEntity.ok(ApiResponse.ok(eventService.closeEvent(eventId, managerToken)));
    }

    // ── 축제 모드 (FESTIVAL) — 공개 조회 ────────────────────────────────

    @Tag(name = "Festival (축제 모드)")
    @Operation(summary = "진행 중인 축제 목록 조회 (FESTIVAL 모드)")
    @GetMapping("/api/festivals")
    public ResponseEntity<ApiResponse<List<EventResponse>>> getActiveFestivals() {
        return ResponseEntity.ok(ApiResponse.ok(eventService.getActiveFestivals()));
    }

    @Tag(name = "Festival (축제 모드)")
    @Operation(summary = "축제 상세 조회")
    @GetMapping("/api/festivals/{festivalId}")
    public ResponseEntity<ApiResponse<EventResponse>> getFestival(@PathVariable Long festivalId) {
        return ResponseEntity.ok(ApiResponse.ok(eventService.getEvent(festivalId)));
    }
}
