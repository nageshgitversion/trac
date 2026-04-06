package com.fintrac.portfolio;

import com.fintrac.portfolio.dto.*;
import com.fintrac.portfolio.entity.*;
import com.fintrac.portfolio.repository.HoldingRepository;
import com.fintrac.portfolio.service.PortfolioService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PortfolioServiceTest {

    @Mock
    private HoldingRepository holdingRepository;

    @InjectMocks
    private PortfolioService portfolioService;

    @Test
    void addHolding_savesAndReturnsResponse() {
        CreateHoldingRequest req = new CreateHoldingRequest();
        req.setType(HoldingType.STOCK);
        req.setName("Infosys");
        req.setSymbol("INFY");
        req.setUnits(BigDecimal.valueOf(10));
        req.setBuyPrice(BigDecimal.valueOf(1500));
        req.setCurrentPrice(BigDecimal.valueOf(1700));

        Holding saved = Holding.builder()
            .id(1L).userId(1L).type(HoldingType.STOCK).name("Infosys")
            .symbol("INFY").units(BigDecimal.valueOf(10))
            .buyPrice(BigDecimal.valueOf(1500)).currentPrice(BigDecimal.valueOf(1700))
            .active(true).createdAt(Instant.now())
            .build();

        when(holdingRepository.save(any(Holding.class))).thenReturn(saved);

        HoldingResponse response = portfolioService.addHolding(1L, req);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Infosys");
        assertThat(response.getSymbol()).isEqualTo("INFY");
    }

    @Test
    void listHoldings_returnsAllActiveHoldings() {
        Holding h1 = Holding.builder()
            .id(1L).userId(1L).type(HoldingType.MF).name("HDFC Mutual Fund")
            .units(BigDecimal.valueOf(100)).buyPrice(BigDecimal.valueOf(50))
            .currentPrice(BigDecimal.valueOf(60)).active(true).createdAt(Instant.now()).build();

        Holding h2 = Holding.builder()
            .id(2L).userId(1L).type(HoldingType.STOCK).name("TCS")
            .units(BigDecimal.valueOf(5)).buyPrice(BigDecimal.valueOf(3000))
            .currentPrice(BigDecimal.valueOf(3500)).active(true).createdAt(Instant.now()).build();

        when(holdingRepository.findByUserIdAndActiveTrue(1L)).thenReturn(List.of(h1, h2));

        List<HoldingResponse> holdings = portfolioService.listHoldings(1L);

        assertThat(holdings).hasSize(2);
    }

    @Test
    void summary_calculatesCorrectTotals() {
        // MF: 100 units * 50 buy = 5000 invested; 100 * 60 = 6000 current
        Holding mf = Holding.builder()
            .id(1L).userId(1L).type(HoldingType.MF).name("HDFC MF")
            .units(BigDecimal.valueOf(100)).buyPrice(BigDecimal.valueOf(50))
            .currentPrice(BigDecimal.valueOf(60)).active(true).createdAt(Instant.now()).build();

        // STOCK: 5 units * 3000 buy = 15000 invested; 5 * 3500 = 17500 current
        Holding stock = Holding.builder()
            .id(2L).userId(1L).type(HoldingType.STOCK).name("TCS")
            .units(BigDecimal.valueOf(5)).buyPrice(BigDecimal.valueOf(3000))
            .currentPrice(BigDecimal.valueOf(3500)).active(true).createdAt(Instant.now()).build();

        when(holdingRepository.findByUserIdAndActiveTrue(1L)).thenReturn(List.of(mf, stock));

        PortfolioSummaryResponse summary = portfolioService.summary(1L);

        // totalInvested = 5000 + 15000 = 20000
        // totalCurrentValue = 6000 + 17500 = 23500
        // returnPct = (23500 - 20000) / 20000 * 100 = 17.5%
        assertThat(summary.getTotalInvested()).isEqualByComparingTo(BigDecimal.valueOf(20000));
        assertThat(summary.getTotalCurrentValue()).isEqualByComparingTo(BigDecimal.valueOf(23500));
        assertThat(summary.getTotalReturnPct()).isEqualByComparingTo(new BigDecimal("17.50"));
        assertThat(summary.getByType()).containsKeys("MF", "STOCK");
    }
}
