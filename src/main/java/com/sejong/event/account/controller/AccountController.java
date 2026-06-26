package com.sejong.event.account.controller;

import com.sejong.event.account.dto.AccountResponse;
import com.sejong.event.account.dto.CreateAccountRequest;
import com.sejong.event.account.service.AccountService;
import com.sejong.event.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Accounts", description = "계정 관리 API (superuser 전용)")
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @Operation(summary = "운영진 계정 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getAccounts() {
        return ResponseEntity.ok(ApiResponse.ok("성공", accountService.getAccounts()));
    }

    @Operation(summary = "운영진 계정 생성")
    @PostMapping
    public ResponseEntity<ApiResponse<AccountResponse>> createAccount(
            @Valid @RequestBody CreateAccountRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("계정이 생성되었습니다.", accountService.createAccount(request)));
    }

    @Operation(summary = "운영진 계정 삭제")
    @DeleteMapping("/{accountId}")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(@PathVariable Long accountId) {
        accountService.deleteAccount(accountId);
        return ResponseEntity.ok(ApiResponse.ok("계정이 삭제되었습니다.", null));
    }
}
