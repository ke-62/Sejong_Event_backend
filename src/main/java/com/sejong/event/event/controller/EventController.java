package com.sejong.event.event.controller;

import com.sejong.event.common.response.ApiResponse;
import com.sejong.event.event.dto.CreateEventRequest;
import com.sejong.event.event.dto.EventResponse;
import com.sejong.event.event.dto.UpdateEventRequest;
import com.sejong.event.event.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Events", description = "행사 API")
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @Operation(summary = "행사 목록 조회 (인증 선택)")
    @GetMapping
    public ResponseEntity<ApiResponse<List<EventResponse>>> getEvents(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.ok("성공", eventService.getEvents(userId)));
    }

    @Operation(summary = "행사 단건 조회")
    @GetMapping("/{eventId}")
    public ResponseEntity<ApiResponse<EventResponse>> getEvent(@PathVariable Long eventId) {
        return ResponseEntity.ok(ApiResponse.ok("성공", eventService.getEvent(eventId)));
    }

    @Operation(summary = "행사 생성 (organizer 이상)")
    @PostMapping
    public ResponseEntity<ApiResponse<EventResponse>> createEvent(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody CreateEventRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("행사가 생성되었습니다.", eventService.createEvent(userId, request)));
    }

    @Operation(summary = "행사 수정")
    @PutMapping("/{eventId}")
    public ResponseEntity<ApiResponse<EventResponse>> updateEvent(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long eventId,
            @RequestBody UpdateEventRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("성공", eventService.updateEvent(userId, eventId, request)));
    }

    @Operation(summary = "행사 삭제")
    @DeleteMapping("/{eventId}")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long eventId) {
        eventService.deleteEvent(userId, eventId);
        return ResponseEntity.ok(ApiResponse.ok("행사가 삭제되었습니다.", null));
    }
}
