package com.investrac.common.dto;

/**
 * Centralized error codes for all INVESTRAC services.
 * Format: WLTH-{ServiceCode}{SequentialNumber}
 *
 * 1xxx = Auth errors
 * 2xxx = Wallet errors
 * 3xxx = Transaction errors
 * 4xxx = Account errors
 * 5xxx = System/Infrastructure errors
 */
public final class ErrorCodes {

    private ErrorCodes() {}

    // Auth Service
    public static final String INVALID_CREDENTIALS     = "WLTH-1001";
    public static final String TOKEN_EXPIRED           = "WLTH-1002";
    public static final String TOKEN_INVALID           = "WLTH-1003";
    public static final String ACCOUNT_LOCKED          = "WLTH-1004";
    public static final String EMAIL_ALREADY_EXISTS    = "WLTH-1005";
    public static final String PHONE_ALREADY_EXISTS    = "WLTH-1006";
    public static final String OTP_EXPIRED             = "WLTH-1007";
    public static final String OTP_INVALID             = "WLTH-1008";
    public static final String OTP_MAX_ATTEMPTS        = "WLTH-1009";
    public static final String USER_NOT_FOUND          = "WLTH-1010";
    public static final String EMAIL_NOT_VERIFIED      = "WLTH-1011";

    // Wallet Service
    public static final String WALLET_NOT_FOUND        = "WLTH-2001";
    public static final String WALLET_ALREADY_EXISTS   = "WLTH-2002";
    public static final String INSUFFICIENT_BALANCE    = "WLTH-2003";
    public static final String ENVELOPE_NOT_FOUND      = "WLTH-2004";
    public static final String ENVELOPE_OVER_BUDGET    = "WLTH-2005";

    // Transaction Service
    public static final String TRANSACTION_NOT_FOUND   = "WLTH-3001";
    public static final String TRANSACTION_FAILED      = "WLTH-3002";
    public static final String SAGA_PROCESSING_FAILED  = "WLTH-3003";

    // Account Service
    public static final String ACCOUNT_NOT_FOUND       = "WLTH-4001";
    public static final String ACCOUNT_INACTIVE        = "WLTH-4002";
    public static final String MATURITY_DATE_INVALID   = "WLTH-4003";

    // Portfolio
    public static final String HOLDING_NOT_FOUND       = "WLTH-4501";
    public static final String PRICE_SYNC_FAILED       = "WLTH-4502";

    // System
    public static final String SERVICE_UNAVAILABLE     = "WLTH-5001";
    public static final String INTERNAL_ERROR          = "WLTH-5002";
    public static final String VALIDATION_FAILED       = "WLTH-0400";
    public static final String UNAUTHORIZED            = "WLTH-0401";
    public static final String FORBIDDEN               = "WLTH-0403";
    public static final String NOT_FOUND               = "WLTH-0404";
}
