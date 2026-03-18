package com.investrac.account.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AccountException extends RuntimeException {
    private final String errorCode;
    private final HttpStatus httpStatus;

    public AccountException(String errorCode, String message, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }
}
