package com.fintrac.wallet.controller;

import com.fintrac.common.response.ApiResponse;
import com.fintrac.wallet.dto.*;
import com.fintrac.wallet.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
@Tag(name = "Wallet")
public class WalletController {

    private final WalletService walletService;

    @GetMapping
    @Operation(summary = "Get wallet balance")
    public ResponseEntity<ApiResponse<WalletResponse>> getWallet(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(walletService.getWallet(userId(auth))));
    }

    @PostMapping("/credit")
    @Operation(summary = "Add funds")
    public ResponseEntity<ApiResponse<WalletResponse>> credit(Authentication auth,
            @Valid @RequestBody WalletOperationRequest req) {
        return ResponseEntity.ok(ApiResponse.success(walletService.credit(userId(auth), req), "Funds added"));
    }

    @PostMapping("/debit")
    @Operation(summary = "Deduct funds")
    public ResponseEntity<ApiResponse<WalletResponse>> debit(Authentication auth,
            @Valid @RequestBody WalletOperationRequest req) {
        return ResponseEntity.ok(ApiResponse.success(walletService.debit(userId(auth), req), "Funds deducted"));
    }

    private Long userId(Authentication auth) {
        return (Long) auth.getPrincipal();
    }
}
