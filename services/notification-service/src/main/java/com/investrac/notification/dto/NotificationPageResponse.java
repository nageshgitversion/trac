package com.investrac.notification.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class NotificationPageResponse {
    private List<NotificationResponse> content;
    private int  pageNumber;
    private int  pageSize;
    private long totalElements;
    private int  totalPages;
    private long unreadCount;
    private boolean last;
}
