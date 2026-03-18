package com.investrac.notification.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class NotificationResponse {
    private Long   id;
    private Long   userId;
    private String title;
    private String body;
    private String type;
    private String channel;
    private String dataJson;
    private boolean read;
    private boolean sent;
    private Instant sentAt;
    private String failureReason;
    private Instant createdAt;
}
