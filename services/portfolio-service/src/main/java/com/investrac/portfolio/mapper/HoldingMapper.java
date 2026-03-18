package com.investrac.portfolio.mapper;

import com.investrac.portfolio.dto.response.HoldingResponse;
import com.investrac.portfolio.entity.Holding;
import org.springframework.stereotype.Component;

@Component
public class HoldingMapper {

    public HoldingResponse toResponse(Holding h) {
        return HoldingResponse.builder()
            .id(h.getId())
            .userId(h.getUserId())
            .type(h.getType().name())
            .name(h.getName())
            .symbol(h.getSymbol())
            .units(h.getUnits())
            .buyPrice(h.getBuyPrice())
            .currentPrice(h.getCurrentPrice())
            .invested(h.getInvested())
            .currentValue(h.getCurrentValue())
            .returnAmount(h.getReturnAmount())
            .returnPercent(h.getReturnPercent())
            .isProfit(h.isProfit())
            .xirr(h.getXirr())
            .sipAmount(h.getSipAmount())
            .updatable(h.isUpdatable())
            .lastSynced(h.getLastSynced())
            .note(h.getNote())
            .createdAt(h.getCreatedAt())
            .build();
    }
}
