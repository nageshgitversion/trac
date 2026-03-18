package com.investrac.gateway.fallback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

/**
 * Fallback responses when a downstream service is unavailable.
 * Circuit breaker routes here when service fails.
 */
@RestController
@RequestMapping("/fallback")
@Slf4j
public class GatewayFallbackController {

    @GetMapping("/auth")
    public Mono<ResponseEntity<Map<String, Object>>> authFallback() {
        log.error("auth-service is unavailable — circuit open");
        return Mono.just(serviceUnavailable("auth-service", "WLTH-5001"));
    }

    @GetMapping("/wallet")
    public Mono<ResponseEntity<Map<String, Object>>> walletFallback() {
        log.error("wallet-service is unavailable — circuit open");
        return Mono.just(serviceUnavailable("wallet-service", "WLTH-5002"));
    }

    @GetMapping("/transaction")
    public Mono<ResponseEntity<Map<String, Object>>> transactionFallback() {
        log.error("transaction-service is unavailable — circuit open");
        return Mono.just(serviceUnavailable("transaction-service", "WLTH-5003"));
    }

    @GetMapping("/portfolio")
    public Mono<ResponseEntity<Map<String, Object>>> portfolioFallback() {
        log.error("portfolio-service is unavailable — circuit open");
        return Mono.just(serviceUnavailable("portfolio-service", "WLTH-5004"));
    }

    @GetMapping("/account")
    public Mono<ResponseEntity<Map<String, Object>>> accountFallback() {
        log.error("account-service is unavailable — circuit open");
        return Mono.just(serviceUnavailable("account-service", "WLTH-5005"));
    }

    @GetMapping("/ai")
    public Mono<ResponseEntity<Map<String, Object>>> aiFallback() {
        log.error("ai-service is unavailable — circuit open");
        return Mono.just(serviceUnavailable("ai-service", "WLTH-5006"));
    }

    @GetMapping("/notification")
    public Mono<ResponseEntity<Map<String, Object>>> notificationFallback() {
        log.error("notification-service is unavailable — circuit open");
        return Mono.just(serviceUnavailable("notification-service", "WLTH-5007"));
    }

    private ResponseEntity<Map<String, Object>> serviceUnavailable(String service, String code) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(Map.of(
                "success", false,
                "errorCode", code,
                "message", service + " is temporarily unavailable. Please try again in a few moments.",
                "timestamp", Instant.now().toString(),
                "service", service
            ));
    }
}
