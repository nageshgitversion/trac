package com.fintrac.portfolio.service;

import com.fintrac.common.exception.FinTracException;
import com.fintrac.portfolio.dto.*;
import com.fintrac.portfolio.entity.*;
import com.fintrac.portfolio.repository.HoldingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final HoldingRepository holdingRepository;

    @Transactional
    public HoldingResponse addHolding(Long userId, CreateHoldingRequest req) {
        Holding h = Holding.builder()
            .userId(userId).type(req.getType()).name(req.getName())
            .symbol(req.getSymbol()).units(req.getUnits())
            .buyPrice(req.getBuyPrice()).currentPrice(req.getCurrentPrice())
            .note(req.getNote()).build();
        return toDto(holdingRepository.save(h));
    }

    public List<HoldingResponse> listHoldings(Long userId) {
        return holdingRepository.findByUserIdAndActiveTrue(userId)
            .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public HoldingResponse updateHolding(Long userId, Long id, UpdateHoldingRequest req) {
        Holding h = holdingRepository.findByIdAndUserIdAndActiveTrue(id, userId)
            .orElseThrow(() -> new FinTracException("FT-5001", "Holding not found", HttpStatus.NOT_FOUND));
        if (req.getUnits() != null) h.setUnits(req.getUnits());
        if (req.getCurrentPrice() != null) h.setCurrentPrice(req.getCurrentPrice());
        if (req.getNote() != null) h.setNote(req.getNote());
        return toDto(holdingRepository.save(h));
    }

    @Transactional
    public void deleteHolding(Long userId, Long id) {
        Holding h = holdingRepository.findByIdAndUserIdAndActiveTrue(id, userId)
            .orElseThrow(() -> new FinTracException("FT-5001", "Holding not found", HttpStatus.NOT_FOUND));
        h.setActive(false);
        holdingRepository.save(h);
    }

    public PortfolioSummaryResponse summary(Long userId) {
        List<Holding> holdings = holdingRepository.findByUserIdAndActiveTrue(userId);
        BigDecimal totalInvested = BigDecimal.ZERO;
        BigDecimal totalCurrent = BigDecimal.ZERO;
        Map<String, BigDecimal> byType = new LinkedHashMap<>();

        for (Holding h : holdings) {
            BigDecimal invested = safeMultiply(h.getUnits(), h.getBuyPrice());
            BigDecimal current = safeMultiply(h.getUnits(), h.getCurrentPrice());
            totalInvested = totalInvested.add(invested);
            totalCurrent = totalCurrent.add(current);
            String typeKey = h.getType().name();
            byType.merge(typeKey, current, BigDecimal::add);
        }

        BigDecimal returnPct = totalInvested.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO :
            totalCurrent.subtract(totalInvested)
                .divide(totalInvested, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        return PortfolioSummaryResponse.builder()
            .totalInvested(totalInvested.setScale(2, RoundingMode.HALF_UP))
            .totalCurrentValue(totalCurrent.setScale(2, RoundingMode.HALF_UP))
            .totalReturnPct(returnPct.setScale(2, RoundingMode.HALF_UP))
            .byType(byType).build();
    }

    private BigDecimal safeMultiply(BigDecimal a, BigDecimal b) {
        if (a == null || b == null) return BigDecimal.ZERO;
        return a.multiply(b);
    }

    private HoldingResponse toDto(Holding h) {
        BigDecimal invested = safeMultiply(h.getUnits(), h.getBuyPrice());
        BigDecimal current = safeMultiply(h.getUnits(), h.getCurrentPrice());
        BigDecimal returnPct = invested.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO :
            current.subtract(invested).divide(invested, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        return HoldingResponse.builder()
            .id(h.getId()).userId(h.getUserId()).type(h.getType())
            .name(h.getName()).symbol(h.getSymbol()).units(h.getUnits())
            .buyPrice(h.getBuyPrice()).currentPrice(h.getCurrentPrice())
            .invested(invested).currentValue(current)
            .returnPct(returnPct.setScale(2, RoundingMode.HALF_UP))
            .note(h.getNote()).createdAt(h.getCreatedAt()).build();
    }
}
