package com.investrac.wallet.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class WalletException extends RuntimeException {
    private final String errorCode;
    private final HttpStatus httpStatus;

    public WalletException(String errorCode, String message, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }
}
