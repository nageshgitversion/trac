package com.investrac.portfolio.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class PortfolioException extends RuntimeException {
    private final String errorCode;
    private final HttpStatus httpStatus;

    public PortfolioException(String errorCode, String message, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }
}
