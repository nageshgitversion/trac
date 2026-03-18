package com.investrac.notification.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdatePreferenceRequest {
    private Boolean pushEnabled;
    private Boolean emailEnabled;

    @Size(max = 512, message = "FCM token too long")
    private String fcmToken;

    @Email(message = "Invalid email format")
    @Size(max = 150)
    private String email;

    private Boolean transactionNotif;
    private Boolean emiNotif;
    private Boolean walletAlertNotif;
    private Boolean portfolioNotif;
    private Boolean aiInsightNotif;
}
