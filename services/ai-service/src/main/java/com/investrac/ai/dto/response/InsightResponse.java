package com.investrac.ai.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class InsightResponse {
    private Long    id;
    private String  content;
    private String  type;
    private int     priority;
    private boolean read;
    private Instant generatedAt;
}
