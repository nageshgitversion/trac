package com.investrac.auth.exception;

import com.investrac.common.dto.ErrorCodes;
import com.investrac.common.response.ApiResponse;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final Tracer tracer;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = ((FieldError) error).getField();
            errors.put(field, error.getDefaultMessage());
        });

        log.warn("Validation failed: {}", errors);
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.validationError(errors));
    }

    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccountLocked(
            AccountLockedException ex) {
        log.warn("Account locked attempt: {}", ex.getMessage());
        return ResponseEntity
            .status(HttpStatus.LOCKED)
            .body(ApiResponse.error(ex.getErrorCode(), ex.getMessage()));
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthException(
            AuthException ex, HttpServletRequest request) {
        log.warn("Auth error [{}] path={}: {}", ex.getErrorCode(), request.getRequestURI(), ex.getMessage());
        HttpStatus status = ex.getHttpStatus() != null ? ex.getHttpStatus() : HttpStatus.UNAUTHORIZED;
        return ResponseEntity
            .status(status)
            .body(ApiResponse.error(ex.getErrorCode(), ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleAll(
            Exception ex, HttpServletRequest request) {
        String traceId = tracer.currentSpan() != null
            ? tracer.currentSpan().context().traceId()
            : "unknown";

        log.error("Unhandled error [traceId={}] path={}: {}",
            traceId, request.getRequestURI(), ex.getMessage(), ex);

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error(ErrorCodes.INTERNAL_ERROR,
                "An unexpected error occurred. TraceId: " + traceId));
    }
}
