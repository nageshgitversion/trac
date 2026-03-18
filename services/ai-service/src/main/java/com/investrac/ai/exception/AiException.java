package com.investrac.ai.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AiException extends RuntimeException {
    private final String errorCode;
    private final HttpStatus httpStatus;
    public AiException(String errorCode, String message, HttpStatus httpStatus) {
        super(message); this.errorCode = errorCode; this.httpStatus = httpStatus;
    }
}
