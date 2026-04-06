package com.fintrac.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class FinTracException extends RuntimeException {
    private final String errorCode;
    private final HttpStatus status;

    public FinTracException(String errorCode, String message, HttpStatus status) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }
}
