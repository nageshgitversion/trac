package com.investrac.notification.mapper;

import com.investrac.notification.dto.NotificationResponse;
import com.investrac.notification.dto.PreferenceResponse;
import com.investrac.notification.entity.Notification;
import com.investrac.notification.entity.NotificationPreference;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
            .id(n.getId())
            .userId(n.getUserId())
            .title(n.getTitle())
            .body(n.getBody())
            .type(n.getType().name())
            .channel(n.getChannel().name())
            .dataJson(n.getDataJson())
            .read(n.isRead())
            .sent(n.isSent())
            .sentAt(n.getSentAt())
            .failureReason(n.getFailureReason())
            .createdAt(n.getCreatedAt())
            .build();
    }

    public PreferenceResponse toPreferenceResponse(NotificationPreference p) {
        return PreferenceResponse.builder()
            .userId(p.getUserId())
            .pushEnabled(p.isPushEnabled())
            .emailEnabled(p.isEmailEnabled())
            .hasFcmToken(p.getFcmToken() != null && !p.getFcmToken().isBlank())
            .email(p.getEmail())
            .transactionNotif(p.isTransactionNotif())
            .emiNotif(p.isEmiNotif())
            .walletAlertNotif(p.isWalletAlertNotif())
            .portfolioNotif(p.isPortfolioNotif())
            .aiInsightNotif(p.isAiInsightNotif())
            .updatedAt(p.getUpdatedAt())
            .build();
    }
}
