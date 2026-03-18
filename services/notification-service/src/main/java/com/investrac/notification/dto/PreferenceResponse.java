package com.investrac.notification.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class PreferenceResponse {
    private Long    userId;
    private boolean pushEnabled;
    private boolean emailEnabled;
    private boolean hasFcmToken;   // Don't expose actual token
    private String  email;
    private boolean transactionNotif;
    private boolean emiNotif;
    private boolean walletAlertNotif;
    private boolean portfolioNotif;
    private boolean aiInsightNotif;
    private Instant updatedAt;
}
