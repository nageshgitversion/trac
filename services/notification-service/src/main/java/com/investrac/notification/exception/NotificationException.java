package com.investrac.notification.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class NotificationException extends RuntimeException {
    private final String errorCode;
    private final HttpStatus httpStatus;

    public NotificationException(String errorCode, String message, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }
}
