package com.sejong.event.festival.controller;

import com.sejong.event.common.response.ApiResponse;
import com.sejong.event.festival.dto.FestivalStatusResponse;
import com.sejong.event.festival.dto.UpdateFestivalStatusRequest;
import com.sejong.event.festival.service.FestivalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Festival", description = "축제 설정 API")
@RestController
@RequestMapping("/api/festival")
@RequiredArgsConstructor
public class FestivalController {

    private final FestivalService festivalService;

    @Operation(summary = "축제 활성 여부 조회")
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<FestivalStatusResponse>> getStatus() {
        return ResponseEntity.ok(ApiResponse.ok("성공", festivalService.getStatus()));
    }

    @Operation(summary = "축제 기능 ON/OFF (superuser)")
    @PatchMapping("/status")
    public ResponseEntity<ApiResponse<FestivalStatusResponse>> updateStatus(
            @Valid @RequestBody UpdateFestivalStatusRequest request) {
        FestivalStatusResponse response = festivalService.updateStatus(request);
        String message = Boolean.TRUE.equals(response.getIsActive())
                ? "축제 기능이 활성화되었습니다."
                : "축제 기능이 비활성화되었습니다.";
        return ResponseEntity.ok(ApiResponse.ok(message, response));
    }
}
