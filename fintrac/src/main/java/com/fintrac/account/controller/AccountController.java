package com.fintrac.account.controller;

import com.fintrac.account.dto.*;
import com.fintrac.account.service.AccountService;
import com.fintrac.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Tag(name = "Accounts")
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<ApiResponse<AccountResponse>> create(Authentication auth,
            @Valid @RequestBody CreateAccountRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(accountService.create(uid(auth), req)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AccountResponse>>> list(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(accountService.listAll(uid(auth))));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountResponse>> update(Authentication auth,
            @PathVariable Long id, @RequestBody UpdateAccountRequest req) {
        return ResponseEntity.ok(ApiResponse.success(accountService.update(uid(auth), id, req)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(Authentication auth, @PathVariable Long id) {
        accountService.delete(uid(auth), id);
        return ResponseEntity.ok(ApiResponse.success(null, "Account deleted"));
    }

    @GetMapping("/{id}/projection")
    public ResponseEntity<ApiResponse<AccountProjectionResponse>> projection(Authentication auth,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(accountService.projection(uid(auth), id)));
    }

    private Long uid(Authentication auth) { return (Long) auth.getPrincipal(); }
}
