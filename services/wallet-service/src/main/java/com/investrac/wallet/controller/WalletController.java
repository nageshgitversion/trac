package com.investrac.wallet.controller;

import com.investrac.common.response.ApiResponse;
import com.investrac.wallet.dto.*;
import com.investrac.wallet.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
@Tag(name = "Wallet", description = "Monthly wallet management and envelope budgeting")
public class WalletController {

    private final WalletService walletService;

    @PostMapping
    @Operation(summary = "Setup monthly wallet")
    public ResponseEntity<ApiResponse<WalletResponse>> createWallet(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody CreateWalletRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(walletService.createWallet(userId, request),
                "Wallet activated successfully"));
    }

    @GetMapping("/current")
    @Operation(summary = "Get current month's wallet")
    public ResponseEntity<ApiResponse<WalletResponse>> getCurrentWallet(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(ApiResponse.success(walletService.getCurrentWallet(userId)));
    }

    @PostMapping("/topup")
    @Operation(summary = "Top up wallet with additional funds")
    public ResponseEntity<ApiResponse<WalletResponse>> topUp(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody TopUpRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
            walletService.topUp(userId, request), "Wallet topped up successfully"));
    }
}
