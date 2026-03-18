package com.investrac.portfolio.controller;

import com.investrac.common.response.ApiResponse;
import com.investrac.portfolio.dto.request.CreateHoldingRequest;
import com.investrac.portfolio.dto.request.UpdateHoldingRequest;
import com.investrac.portfolio.dto.response.*;
import com.investrac.portfolio.service.PortfolioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/portfolio")
@RequiredArgsConstructor
@Tag(name = "Portfolio",
     description = "Investment portfolio management with live price sync from mfapi.in and Yahoo Finance")
public class PortfolioController {

    private final PortfolioService portfolioService;

    // ── CREATE ──────────────────────────────────────────────
    @PostMapping("/holdings")
    @Operation(
        summary     = "Add a new holding",
        description = "Add MF, stocks, SGB, NPS/PPF or other investments. " +
                      "Provide symbol for holdings with live price sync."
    )
    public ResponseEntity<ApiResponse<HoldingResponse>> createHolding(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody CreateHoldingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(
                portfolioService.createHolding(userId, request),
                "Holding added successfully"));
    }

    // ── PORTFOLIO SUMMARY ────────────────────────────────────
    @GetMapping
    @Operation(
        summary     = "Get portfolio summary",
        description = "Returns total invested, current value, returns, XIRR, " +
                      "asset allocation breakdown, and all holdings sorted by value."
    )
    public ResponseEntity<ApiResponse<PortfolioSummaryResponse>> getSummary(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(ApiResponse.success(
            portfolioService.getPortfolioSummary(userId)));
    }

    // ── GET BY ID ─────────────────────────────────────────────
    @GetMapping("/holdings/{id}")
    @Operation(summary = "Get a specific holding by ID")
    public ResponseEntity<ApiResponse<HoldingResponse>> getById(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(ApiResponse.success(
            portfolioService.getById(id, userId)));
    }

    // ── UPDATE ────────────────────────────────────────────────
    @PutMapping("/holdings/{id}")
    @Operation(
        summary     = "Update a holding",
        description = "Partial update — only provided fields are updated."
    )
    public ResponseEntity<ApiResponse<HoldingResponse>> update(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody UpdateHoldingRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
            portfolioService.updateHolding(id, userId, request),
            "Holding updated successfully"));
    }

    // ── DELETE ────────────────────────────────────────────────
    @DeleteMapping("/holdings/{id}")
    @Operation(
        summary     = "Remove a holding",
        description = "Soft delete — holding marked inactive, price history retained."
    )
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        portfolioService.deleteHolding(id, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Holding removed"));
    }

    // ── MANUAL SYNC ───────────────────────────────────────────
    @PostMapping("/sync")
    @Operation(
        summary     = "Manually sync prices for all updatable holdings",
        description = "Triggers immediate price fetch from mfapi.in (MF) and Yahoo Finance (stocks). " +
                      "Normally auto-runs at 8 PM on market days."
    )
    public ResponseEntity<ApiResponse<SyncResultResponse>> syncPrices(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(ApiResponse.success(
            portfolioService.syncPricesForUser(userId),
            "Price sync complete"));
    }

    // ── PRICE HISTORY (single holding) ────────────────────────
    @GetMapping("/holdings/{id}/history")
    @Operation(
        summary     = "Get price history for a specific holding",
        description = "Returns daily price snapshots for the last N days. " +
                      "Use for individual holding charts."
    )
    public ResponseEntity<ApiResponse<List<PriceHistoryResponse>>> getPriceHistory(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "90")
            @Parameter(description = "Number of days of history (default: 90, max: 365)")
            int days) {
        return ResponseEntity.ok(ApiResponse.success(
            portfolioService.getPriceHistory(id, userId, Math.min(days, 365))));
    }

    // ── PORTFOLIO VALUE HISTORY (all holdings aggregated) ─────
    @GetMapping("/history")
    @Operation(
        summary     = "Get total portfolio value over time",
        description = "Aggregated daily portfolio value across all holdings. " +
                      "Use for the main portfolio performance chart."
    )
    public ResponseEntity<ApiResponse<PortfolioValueHistoryResponse>> getPortfolioHistory(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "365")
            @Parameter(description = "Number of days of history (default: 365)")
            int days) {
        return ResponseEntity.ok(ApiResponse.success(
            portfolioService.getPortfolioHistory(userId, Math.min(days, 365))));
    }
}
