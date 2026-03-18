package com.investrac.transaction.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class TransactionException extends RuntimeException {
    private final String errorCode;
    private final HttpStatus httpStatus;

    public TransactionException(String errorCode, String message, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }
}
