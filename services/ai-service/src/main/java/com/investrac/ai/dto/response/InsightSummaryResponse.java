package com.investrac.ai.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class InsightSummaryResponse {
    private long                 unreadCount;
    private List<InsightResponse> insights;
}
