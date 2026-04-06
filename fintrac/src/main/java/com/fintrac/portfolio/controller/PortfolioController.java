package com.fintrac.portfolio.controller;

import com.fintrac.common.response.ApiResponse;
import com.fintrac.portfolio.dto.*;
import com.fintrac.portfolio.service.PortfolioService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/portfolio")
@RequiredArgsConstructor
@Tag(name = "Portfolio")
public class PortfolioController {

    private final PortfolioService portfolioService;

    @PostMapping("/holdings")
    public ResponseEntity<ApiResponse<HoldingResponse>> add(Authentication auth,
            @Valid @RequestBody CreateHoldingRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(portfolioService.addHolding(uid(auth), req)));
    }

    @GetMapping("/holdings")
    public ResponseEntity<ApiResponse<List<HoldingResponse>>> list(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(portfolioService.listHoldings(uid(auth))));
    }

    @PutMapping("/holdings/{id}")
    public ResponseEntity<ApiResponse<HoldingResponse>> update(Authentication auth,
            @PathVariable Long id, @RequestBody UpdateHoldingRequest req) {
        return ResponseEntity.ok(ApiResponse.success(portfolioService.updateHolding(uid(auth), id, req)));
    }

    @DeleteMapping("/holdings/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(Authentication auth, @PathVariable Long id) {
        portfolioService.deleteHolding(uid(auth), id);
        return ResponseEntity.ok(ApiResponse.success(null, "Holding deleted"));
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<PortfolioSummaryResponse>> summary(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(portfolioService.summary(uid(auth))));
    }

    private Long uid(Authentication auth) { return (Long) auth.getPrincipal(); }
}
