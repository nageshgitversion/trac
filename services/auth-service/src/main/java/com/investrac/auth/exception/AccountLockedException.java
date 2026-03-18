package com.investrac.auth.exception;

import com.investrac.common.dto.ErrorCodes;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
public class AccountLockedException extends AuthException {
    private final LocalDateTime lockedUntil;

    public AccountLockedException(LocalDateTime lockedUntil) {
        super(ErrorCodes.ACCOUNT_LOCKED,
              "Account is locked due to too many failed attempts. Try again after " + lockedUntil,
              HttpStatus.LOCKED);
        this.lockedUntil = lockedUntil;
    }
}
