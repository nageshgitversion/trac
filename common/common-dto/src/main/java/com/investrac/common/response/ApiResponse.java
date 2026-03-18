package com.investrac.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * Standard API response wrapper for all INVESTRAC services.
 * Every endpoint returns this — consistent contract for Angular.
 *
 * Success: { success:true, data:{...}, timestamp:"...", traceId:"..." }
 * Error:   { success:false, message:"...", errorCode:"WLTH-XXXX", timestamp:"..." }
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final String message;
    private final T data;
    private final String errorCode;
    private final Object errors;        // field-level validation errors
    private final String traceId;

    @Builder.Default
    private final Instant timestamp = Instant.now();

    // ── Factory methods ──

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
            .success(true)
            .data(data)
            .build();
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
            .success(true)
            .data(data)
            .message(message)
            .build();
    }

    public static <T> ApiResponse<T> error(String errorCode, String message) {
        return ApiResponse.<T>builder()
            .success(false)
            .errorCode(errorCode)
            .message(message)
            .build();
    }

    public static <T> ApiResponse<T> validationError(Object errors) {
        return ApiResponse.<T>builder()
            .success(false)
            .errorCode("WLTH-0400")
            .message("Validation failed")
            .errors(errors)
            .build();
    }

    public static <T> ApiResponse<T> withTraceId(ApiResponse<T> response, String traceId) {
        return ApiResponse.<T>builder()
            .success(response.success)
            .message(response.message)
            .data(response.data)
            .errorCode(response.errorCode)
            .errors(response.errors)
            .timestamp(response.timestamp)
            .traceId(traceId)
            .build();
    }
}
