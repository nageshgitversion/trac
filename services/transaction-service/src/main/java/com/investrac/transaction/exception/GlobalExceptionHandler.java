package com.investrac.transaction.exception;

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
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(e ->
            errors.put(((FieldError) e).getField(), e.getDefaultMessage()));
        log.warn("Validation failed: {}", errors);
        return ResponseEntity.badRequest().body(ApiResponse.validationError(errors));
    }

    @ExceptionHandler(TransactionException.class)
    public ResponseEntity<ApiResponse<Void>> handleTransactionException(
            TransactionException ex, HttpServletRequest req) {
        log.warn("Transaction error [{}] path={}: {}", ex.getErrorCode(),
            req.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(ex.getHttpStatus())
            .body(ApiResponse.error(ex.getErrorCode(), ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleAll(Exception ex, HttpServletRequest req) {
        String traceId = tracer.currentSpan() != null
            ? tracer.currentSpan().context().traceId() : "unknown";
        log.error("Unhandled error [traceId={}] path={}: {}", traceId,
            req.getRequestURI(), ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error(ErrorCodes.INTERNAL_ERROR,
                "An unexpected error occurred. TraceId: " + traceId));
    }
}
