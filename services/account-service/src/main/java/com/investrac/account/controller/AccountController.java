package com.investrac.account.controller;

import com.investrac.account.dto.request.CreateAccountRequest;
import com.investrac.account.dto.request.UpdateAccountRequest;
import com.investrac.account.dto.response.AccountSummaryResponse;
import com.investrac.account.dto.response.MaturityCalculationResponse;
import com.investrac.account.dto.response.VirtualAccountResponse;
import com.investrac.account.service.AccountService;
import com.investrac.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Tag(name = "Virtual Accounts",
     description = "Manage FD, RD, Loan and Savings virtual accounts with maturity calculations")
public class AccountController {

    private final AccountService accountService;

    // ── CREATE ──────────────────────────────────────────────
    @PostMapping
    @Operation(
        summary     = "Create a new virtual account",
        description = "Supports SAVINGS, FD, RD and LOAN types. " +
                      "FD/RD maturity amount is auto-calculated from rate and dates."
    )
    public ResponseEntity<ApiResponse<VirtualAccountResponse>> createAccount(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody CreateAccountRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(
                accountService.createAccount(userId, request),
                "Account created successfully"));
    }

    // ── GET SUMMARY ─────────────────────────────────────────
    @GetMapping
    @Operation(
        summary     = "Get all accounts with portfolio summary",
        description = "Returns total savings, FD corpus, RD corpus, loan outstanding, " +
                      "monthly EMI committed, and full account list."
    )
    public ResponseEntity<ApiResponse<AccountSummaryResponse>> getSummary(
            @RequestHeader("X-User-Id") Long userId) {

        return ResponseEntity.ok(ApiResponse.success(
            accountService.getAccountSummary(userId)));
    }

    // ── GET BY ID ────────────────────────────────────────────
    @GetMapping("/{id}")
    @Operation(summary = "Get a specific virtual account by ID")
    public ResponseEntity<ApiResponse<VirtualAccountResponse>> getById(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {

        return ResponseEntity.ok(ApiResponse.success(
            accountService.getById(id, userId)));
    }

    // ── UPDATE ───────────────────────────────────────────────
    @PutMapping("/{id}")
    @Operation(
        summary     = "Update a virtual account",
        description = "Partial update — only provided fields are updated. " +
                      "Maturity amount recalculated if rate or dates change."
    )
    public ResponseEntity<ApiResponse<VirtualAccountResponse>> update(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody UpdateAccountRequest request) {

        return ResponseEntity.ok(ApiResponse.success(
            accountService.updateAccount(id, userId, request),
            "Account updated successfully"));
    }

    // ── DELETE ───────────────────────────────────────────────
    @DeleteMapping("/{id}")
    @Operation(
        summary     = "Deactivate a virtual account",
        description = "Soft delete — account is marked inactive but data is retained for history."
    )
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {

        accountService.deleteAccount(id, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Account deactivated"));
    }

    // ── MATURITY CALCULATION ─────────────────────────────────
    @GetMapping("/{id}/maturity")
    @Operation(
        summary     = "Calculate maturity for an FD or RD account",
        description = "Returns principal, interest earned, maturity amount, " +
                      "tenure in months/years, and calculation method used."
    )
    public ResponseEntity<ApiResponse<MaturityCalculationResponse>> calculateMaturity(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {

        return ResponseEntity.ok(ApiResponse.success(
            accountService.calculateMaturity(id, userId)));
    }

    // ── TOTAL EMI (internal use by wallet-service) ────────────
    @GetMapping("/emi/total")
    @Operation(
        summary     = "Get total monthly EMI committed",
        description = "Sum of all active LOAN + RD EMI amounts. " +
                      "Used by wallet-service to calculate 'committed' amount."
    )
    public ResponseEntity<ApiResponse<BigDecimal>> getTotalEmi(
            @RequestHeader("X-User-Id") Long userId) {

        return ResponseEntity.ok(ApiResponse.success(
            accountService.getTotalMonthlyEmi(userId),
            "Total monthly EMI commitment"));
    }
}
