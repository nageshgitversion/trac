package com.fintrac.transaction.controller;

import com.fintrac.common.response.ApiResponse;
import com.fintrac.transaction.dto.*;
import com.fintrac.transaction.entity.*;
import com.fintrac.transaction.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions")
public class TransactionController {

    private final TransactionService txService;

    @PostMapping
    public ResponseEntity<ApiResponse<TransactionResponse>> create(Authentication auth,
            @Valid @RequestBody CreateTransactionRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(txService.create(uid(auth), req)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> list(
            Authentication auth,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) TransactionCategory category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("txDate").descending());
        return ResponseEntity.ok(ApiResponse.success(txService.list(uid(auth), type, category, from, to, pageable)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionResponse>> update(Authentication auth,
            @PathVariable Long id, @Valid @RequestBody UpdateTransactionRequest req) {
        return ResponseEntity.ok(ApiResponse.success(txService.update(uid(auth), id, req)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(Authentication auth, @PathVariable Long id) {
        txService.delete(uid(auth), id);
        return ResponseEntity.ok(ApiResponse.success(null, "Transaction deleted"));
    }

    @GetMapping("/summary")
    @Operation(summary = "Monthly summary")
    public ResponseEntity<ApiResponse<TransactionSummaryResponse>> summary(
            Authentication auth,
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(ApiResponse.success(txService.summary(uid(auth), year, month)));
    }

    private Long uid(Authentication auth) { return (Long) auth.getPrincipal(); }
}
