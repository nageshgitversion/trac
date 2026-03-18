package com.investrac.portfolio.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class SyncResultResponse {
    private int         totalHoldings;
    private int         syncedCount;
    private int         failedCount;
    private List<String> failedSymbols;
    private Instant     syncedAt;
}
