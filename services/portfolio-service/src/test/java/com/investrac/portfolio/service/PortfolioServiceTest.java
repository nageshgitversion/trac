package com.investrac.portfolio.service;

import com.investrac.portfolio.dto.request.CreateHoldingRequest;
import com.investrac.portfolio.dto.response.HoldingResponse;
import com.investrac.portfolio.dto.response.PortfolioSummaryResponse;
import com.investrac.portfolio.dto.response.SyncResultResponse;
import com.investrac.portfolio.entity.Holding;
import com.investrac.portfolio.entity.Holding.HoldingType;
import com.investrac.portfolio.exception.PortfolioException;
import com.investrac.portfolio.mapper.HoldingMapper;
import com.investrac.portfolio.outbox.PortfolioOutboxService;
import com.investrac.portfolio.repository.HoldingRepository;
import com.investrac.portfolio.repository.PriceHistoryRepository;
import com.investrac.portfolio.sync.PriceSyncService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PortfolioService Unit Tests")
class PortfolioServiceTest {

    @Mock HoldingRepository      holdingRepository;
    @Mock PriceHistoryRepository priceHistoryRepository;
    @Mock PriceSyncService       priceSyncService;
    @Mock HoldingMapper          mapper;
    @Mock PortfolioOutboxService outboxService;

    @InjectMocks
    PortfolioService portfolioService;

    private static final Long USER_ID = 100L;

    private Holding equityMfHolding;
    private Holding stockHolding;
    private HoldingResponse mockResponse;

    @BeforeEach
    void setUp() {
        equityMfHolding = Holding.builder()
            .id(1L).userId(USER_ID)
            .type(HoldingType.EQUITY_MF)
            .name("HDFC Nifty 50 Index")
            .symbol("119598")
            .units(new BigDecimal("4980.0000"))
            .buyPrice(new BigDecimal("96.39"))
            .currentPrice(new BigDecimal("122.89"))
            .invested(new BigDecimal("480000.00"))
            .currentValue(new BigDecimal("612000.00"))
            .xirr(new BigDecimal("16.80"))
            .sipAmount(new BigDecimal("10000.00"))
            .updatable(true)
            .active(true)
            .build();

        stockHolding = Holding.builder()
            .id(2L).userId(USER_ID)
            .type(HoldingType.STOCKS)
            .name("Infosys Ltd")
            .symbol("INFY")
            .units(new BigDecimal("100.0000"))
            .currentPrice(new BigDecimal("1456.75"))
            .invested(new BigDecimal("120000.00"))
            .currentValue(new BigDecimal("145675.00"))
            .xirr(new BigDecimal("14.60"))
            .updatable(true)
            .active(true)
            .build();

        mockResponse = HoldingResponse.builder()
            .id(1L).type("EQUITY_MF").name("HDFC Nifty 50 Index")
            .invested(new BigDecimal("480000.00"))
            .currentValue(new BigDecimal("612000.00"))
            .returnAmount(new BigDecimal("132000.00"))
            .returnPercent(new BigDecimal("27.50"))
            .build();
    }

    // ═══════════════════════════════════════════
    // CREATE
    // ═══════════════════════════════════════════
    @Nested
    @DisplayName("createHolding()")
    class CreateTests {

        @Test
        @DisplayName("equity MF with symbol marked as updatable")
        void create_EquityMf_WithSymbol_MarkedUpdatable() {
            CreateHoldingRequest req = new CreateHoldingRequest();
            req.setType(HoldingType.EQUITY_MF);
            req.setName("HDFC Nifty 50 Index");
            req.setSymbol("119598");
            req.setUnits(new BigDecimal("4980"));
            req.setBuyPrice(new BigDecimal("96.39"));
            req.setInvested(new BigDecimal("480000.00"));

            ArgumentCaptor<Holding> captor = ArgumentCaptor.forClass(Holding.class);
            when(holdingRepository.save(captor.capture())).thenReturn(equityMfHolding);
            when(mapper.toResponse(any())).thenReturn(mockResponse);
            when(priceHistoryRepository.existsByHoldingIdAndRecordedAt(any(), any())).thenReturn(false);
            when(priceHistoryRepository.save(any())).thenReturn(null);

            HoldingResponse result = portfolioService.createHolding(USER_ID, req);

            Holding saved = captor.getValue();
            assertThat(saved.isUpdatable()).isTrue();  // has symbol + updatable type
            assertThat(saved.getSymbol()).isEqualTo("119598");
            assertThat(result.getCurrentValue()).isEqualByComparingTo("612000.00");
        }

        @Test
        @DisplayName("NPS/PPF without symbol — NOT updatable")
        void create_NpsPpf_NoSymbol_NotUpdatable() {
            CreateHoldingRequest req = new CreateHoldingRequest();
            req.setType(HoldingType.NPS_PPF);
            req.setName("NPS Tier 1");
            req.setInvested(new BigDecimal("200000.00"));
            // No symbol

            ArgumentCaptor<Holding> captor = ArgumentCaptor.forClass(Holding.class);
            Holding npsSaved = Holding.builder().id(3L).type(HoldingType.NPS_PPF)
                .updatable(false).active(true).invested(new BigDecimal("200000")).build();
            when(holdingRepository.save(captor.capture())).thenReturn(npsSaved);
            when(mapper.toResponse(any())).thenReturn(HoldingResponse.builder().id(3L).build());

            portfolioService.createHolding(USER_ID, req);

            assertThat(captor.getValue().isUpdatable()).isFalse();
        }

        @Test
        @DisplayName("currentValue defaults to invested when not provided")
        void create_CurrentValueDefaultsToInvested() {
            CreateHoldingRequest req = new CreateHoldingRequest();
            req.setType(HoldingType.NPS_PPF);
            req.setName("PPF Account");
            req.setInvested(new BigDecimal("150000.00"));
            // No currentValue

            ArgumentCaptor<Holding> captor = ArgumentCaptor.forClass(Holding.class);
            when(holdingRepository.save(captor.capture())).thenReturn(equityMfHolding);
            when(mapper.toResponse(any())).thenReturn(mockResponse);

            portfolioService.createHolding(USER_ID, req);

            // currentValue should equal invested when not specified
            assertThat(captor.getValue().getCurrentValue())
                .isEqualByComparingTo("150000.00");
        }
    }

    // ═══════════════════════════════════════════
    // PORTFOLIO SUMMARY
    // ═══════════════════════════════════════════
    @Nested
    @DisplayName("getPortfolioSummary()")
    class SummaryTests {

        @Test
        @DisplayName("returns correct totals and return percentage")
        void summary_CorrectTotalsAndReturnPercent() {
            when(holdingRepository.findByUserIdAndActiveTrueOrderByCurrentValueDesc(USER_ID))
                .thenReturn(List.of(equityMfHolding, stockHolding));
            when(holdingRepository.sumInvested(USER_ID))
                .thenReturn(new BigDecimal("600000.00"));
            when(holdingRepository.sumCurrentValue(USER_ID))
                .thenReturn(new BigDecimal("757675.00"));
            when(holdingRepository.getValueByType(USER_ID))
                .thenReturn(List.of());
            when(mapper.toResponse(any())).thenReturn(mockResponse);

            PortfolioSummaryResponse result = portfolioService.getPortfolioSummary(USER_ID);

            assertThat(result.getTotalInvested()).isEqualByComparingTo("600000.00");
            assertThat(result.getTotalCurrentValue()).isEqualByComparingTo("757675.00");
            assertThat(result.getTotalReturn()).isEqualByComparingTo("157675.00");
            // Return %: 157675 / 600000 × 100 = 26.28%
            assertThat(result.getTotalReturnPercent()).isGreaterThan(BigDecimal.valueOf(26));
            assertThat(result.getHoldingCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("returns empty summary when no holdings exist")
        void summary_NoHoldings_ReturnsZeros() {
            when(holdingRepository.findByUserIdAndActiveTrueOrderByCurrentValueDesc(USER_ID))
                .thenReturn(List.of());

            PortfolioSummaryResponse result = portfolioService.getPortfolioSummary(USER_ID);

            assertThat(result.getTotalInvested()).isEqualByComparingTo("0.00");
            assertThat(result.getHoldingCount()).isZero();
            assertThat(result.getHoldings()).isEmpty();
        }
    }

    // ═══════════════════════════════════════════
    // SYNC
    // ═══════════════════════════════════════════
    @Nested
    @DisplayName("syncPricesForUser()")
    class SyncTests {

        @Test
        @DisplayName("syncs MF and stock prices — returns correct counts")
        void sync_MfAndStocks_CorrectCounts() {
            when(holdingRepository.findByUserIdAndActiveTrueOrderByCurrentValueDesc(USER_ID))
                .thenReturn(List.of(equityMfHolding, stockHolding));
            when(priceSyncService.syncMutualFundNav("119598"))
                .thenReturn(Optional.of(new BigDecimal("124.50")));
            when(priceSyncService.syncStockPrice("INFY"))
                .thenReturn(Optional.of(new BigDecimal("1480.00")));
            when(holdingRepository.sumCurrentValue(USER_ID))
                .thenReturn(new BigDecimal("780000.00"));
            when(priceHistoryRepository.existsByHoldingIdAndRecordedAt(any(), any()))
                .thenReturn(false);
            when(priceHistoryRepository.save(any())).thenReturn(null);
            doNothing().when(outboxService).publish(any(), any());

            SyncResultResponse result = portfolioService.syncPricesForUser(USER_ID);

            assertThat(result.getSyncedCount()).isEqualTo(2);
            assertThat(result.getFailedCount()).isEqualTo(0);
            assertThat(result.getFailedSymbols()).isEmpty();
            assertThat(result.getSyncedAt()).isNotNull();
        }

        @Test
        @DisplayName("failed API call keeps old price — counted as failed")
        void sync_ApiFails_CountsAsFailed() {
            when(holdingRepository.findByUserIdAndActiveTrueOrderByCurrentValueDesc(USER_ID))
                .thenReturn(List.of(equityMfHolding));
            when(priceSyncService.syncMutualFundNav("119598"))
                .thenReturn(Optional.empty());  // API failure

            SyncResultResponse result = portfolioService.syncPricesForUser(USER_ID);

            assertThat(result.getSyncedCount()).isEqualTo(0);
            assertThat(result.getFailedCount()).isEqualTo(1);
            assertThat(result.getFailedSymbols()).contains("119598 (HDFC Nifty 50 Index)");
            // No Kafka event published when nothing synced
            verify(outboxService, never()).publish(any(), any());
        }

        @Test
        @DisplayName("NPS/PPF holdings skipped (not updatable)")
        void sync_NpsPpf_Skipped() {
            Holding npsHolding = Holding.builder()
                .id(3L).userId(USER_ID).type(HoldingType.NPS_PPF)
                .name("NPS Tier 1").updatable(false).active(true)
                .invested(new BigDecimal("200000")).currentValue(new BigDecimal("200000"))
                .build();

            when(holdingRepository.findByUserIdAndActiveTrueOrderByCurrentValueDesc(USER_ID))
                .thenReturn(List.of(npsHolding));

            SyncResultResponse result = portfolioService.syncPricesForUser(USER_ID);

            assertThat(result.getTotalHoldings()).isEqualTo(0); // non-updatable filtered out
            verify(priceSyncService, never()).syncMutualFundNav(any());
            verify(priceSyncService, never()).syncStockPrice(any());
        }
    }

    // ═══════════════════════════════════════════
    // DELETE
    // ═══════════════════════════════════════════
    @Test
    @DisplayName("deleteHolding soft-deactivates")
    void delete_SoftDeactivates() {
        when(holdingRepository.deactivate(1L, USER_ID)).thenReturn(1);
        assertThatCode(() -> portfolioService.deleteHolding(1L, USER_ID))
            .doesNotThrowAnyException();
        verify(holdingRepository).deactivate(1L, USER_ID);
    }

    @Test
    @DisplayName("deleteHolding throws NOT_FOUND when holding doesn't exist")
    void delete_NotFound_Throws() {
        when(holdingRepository.deactivate(99L, USER_ID)).thenReturn(0);
        assertThatThrownBy(() -> portfolioService.deleteHolding(99L, USER_ID))
            .isInstanceOf(PortfolioException.class);
    }

    // ═══════════════════════════════════════════
    // HOLDING BUSINESS METHODS
    // ═══════════════════════════════════════════
    @Nested
    @DisplayName("Holding entity business methods")
    class HoldingBusinessMethodTests {

        @Test
        @DisplayName("getReturnAmount = currentValue - invested")
        void returnAmount_Correct() {
            assertThat(equityMfHolding.getReturnAmount())
                .isEqualByComparingTo("132000.00");
        }

        @Test
        @DisplayName("getReturnPercent = (return/invested) × 100")
        void returnPercent_Correct() {
            // 132000 / 480000 × 100 = 27.50%
            assertThat(equityMfHolding.getReturnPercent())
                .isEqualByComparingTo("27.50");
        }

        @Test
        @DisplayName("refreshCurrentValue updates currentValue from units × price")
        void refreshCurrentValue_RecalculatesCorrectly() {
            equityMfHolding.setCurrentPrice(new BigDecimal("124.50"));
            equityMfHolding.refreshCurrentValue();
            // 4980 × 124.50 = 619,810.00 (rounded to 2dp)
            assertThat(equityMfHolding.getCurrentValue())
                .isEqualByComparingTo("619911.00");
        }

        @Test
        @DisplayName("isProfit returns true when currentValue > invested")
        void isProfit_True() {
            assertThat(equityMfHolding.isProfit()).isTrue();
        }

        @Test
        @DisplayName("isProfit returns false when currentValue < invested")
        void isProfit_False() {
            equityMfHolding.setCurrentValue(new BigDecimal("400000.00")); // loss
            assertThat(equityMfHolding.isProfit()).isFalse();
        }
    }
}
