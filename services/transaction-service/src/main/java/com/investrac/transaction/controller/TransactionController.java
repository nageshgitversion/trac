package com.investrac.transaction.controller;

import com.investrac.common.response.ApiResponse;
import com.investrac.common.response.PagedResponse;
import com.investrac.transaction.dto.*;
import com.investrac.transaction.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Transactions", description = "Transaction CRUD with SAGA-based wallet integration")
public class TransactionController {

    private final TransactionService transactionService;

    // ── CREATE ──
    @PostMapping
    @Operation(summary = "Create a new transaction",
               description = "Saves transaction and triggers SAGA to debit/credit wallet if walletId is provided")
    public ResponseEntity<ApiResponse<TransactionResponse>> create(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody CreateTransactionRequest request) {

        TransactionResponse response = transactionService.createTransaction(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(response, "Transaction created successfully"));
    }

    // ── LIST (paged + filtered) ──
    @GetMapping
    @Operation(summary = "Get transactions with filters and pagination")
    public ResponseEntity<ApiResponse<PagedResponse<TransactionResponse>>> list(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "txDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        TransactionFilterRequest filter = new TransactionFilterRequest();
        if (type     != null) filter.setType(
            com.investrac.transaction.entity.Transaction.TransactionType.valueOf(type.toUpperCase()));
        filter.setCategory(category);
        filter.setFrom(from);
        filter.setTo(to);
        filter.setSearch(search);
        filter.setPage(page);
        filter.setSize(Math.min(size, 100));   // Cap at 100
        filter.setSortBy(sortBy);
        filter.setSortDir(sortDir);

        return ResponseEntity.ok(ApiResponse.success(
            transactionService.getTransactions(userId, filter)));
    }

    // ── GET ONE ──
    @GetMapping("/{id}")
    @Operation(summary = "Get a single transaction by ID")
    public ResponseEntity<ApiResponse<TransactionResponse>> getById(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
            transactionService.getById(userId, id)));
    }

    // ── RECENT (home screen) ──
    @GetMapping("/recent")
    @Operation(summary = "Get recent transactions for home screen")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getRecent(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(ApiResponse.success(
            transactionService.getRecent(userId, Math.min(limit, 50))));
    }

    // ── MONTHLY SUMMARY ──
    @GetMapping("/summary")
    @Operation(summary = "Get monthly income/expense/investment summary with category breakdown")
    public ResponseEntity<ApiResponse<MonthSummaryResponse>> getMonthlySummary(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {

        LocalDate now  = LocalDate.now();
        int targetYear  = year  != null ? year  : now.getYear();
        int targetMonth = month != null ? month : now.getMonthValue();

        return ResponseEntity.ok(ApiResponse.success(
            transactionService.getMonthSummary(userId, targetYear, targetMonth)));
    }

    // ── UPDATE ──
    @PutMapping("/{id}")
    @Operation(summary = "Update a transaction")
    public ResponseEntity<ApiResponse<TransactionResponse>> update(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id,
            @Valid @RequestBody UpdateTransactionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
            transactionService.updateTransaction(userId, id, request),
            "Transaction updated successfully"));
    }

    // ── DELETE ──
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a transaction (soft delete, compensates wallet)")
    public ResponseEntity<ApiResponse<Void>> delete(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        transactionService.deleteTransaction(userId, id);
        return ResponseEntity.ok(ApiResponse.success(null, "Transaction deleted successfully"));
    }
}
