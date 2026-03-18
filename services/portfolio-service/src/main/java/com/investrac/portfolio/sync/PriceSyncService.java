package com.investrac.portfolio.sync;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Optional;

/**
 * External price fetcher — non-blocking WebClient calls.
 *
 * Sources:
 *   Mutual Funds → mfapi.in (free, reliable, ~5 min delay after NAV update)
 *   Stocks       → Yahoo Finance v8 API (~15 min delay, free, no key needed)
 *
 * Design principles:
 *   - All calls are non-blocking (Reactive)
 *   - Returns Optional.empty() on ANY failure — never throws
 *   - Retries once with 2s delay before giving up
 *   - Logs warning on failure so ops team is aware
 *   - Previous price kept if fetch fails (data never corrupted)
 */
@Service
@Slf4j
public class PriceSyncService {

    private final WebClient mfApiClient;
    private final WebClient yahooClient;

    public PriceSyncService(
            @Value("${price-sync.mfapi-base-url:https://api.mfapi.in}") String mfApiBase,
            @Value("${price-sync.yahoo-base-url:https://query1.finance.yahoo.com}") String yahooBase,
            WebClient.Builder webClientBuilder) {

        this.mfApiClient = webClientBuilder
            .baseUrl(mfApiBase)
            .codecs(c -> c.defaultCodecs().maxInMemorySize(512 * 1024))
            .build();

        this.yahooClient = webClientBuilder
            .baseUrl(yahooBase)
            .defaultHeader("User-Agent", "Mozilla/5.0 (compatible; INVESTRAC/1.0)")
            .codecs(c -> c.defaultCodecs().maxInMemorySize(512 * 1024))
            .build();
    }

    // ════════════════════════════════════════
    // MUTUAL FUND NAV — mfapi.in
    // ════════════════════════════════════════

    /**
     * Fetch latest NAV for a Mutual Fund.
     *
     * @param schemeCode AMFI scheme code (e.g. "119598" for HDFC Nifty 50 Direct Growth)
     * @return Optional NAV as BigDecimal, or empty on any failure
     *
     * API Response format:
     * {
     *   "meta": { "scheme_name": "HDFC Nifty 50 Direct Growth" },
     *   "data": [
     *     { "date": "16-03-2026", "nav": "124.5678" },
     *     ...
     *   ],
     *   "status": "SUCCESS"
     * }
     */
    public Optional<BigDecimal> syncMutualFundNav(String schemeCode) {
        if (schemeCode == null || schemeCode.isBlank()) {
            log.warn("MF sync skipped — empty scheme code");
            return Optional.empty();
        }

        log.debug("Fetching MF NAV for schemeCode={}", schemeCode);

        try {
            MfApiResponse response = mfApiClient
                .get()
                .uri("/mf/{schemeCode}", schemeCode)
                .retrieve()
                .bodyToMono(MfApiResponse.class)
                .timeout(Duration.ofSeconds(10))
                .retryWhen(Retry.fixedDelay(1, Duration.ofSeconds(2))
                    .filter(ex -> !(ex instanceof WebClientResponseException.NotFound)))
                .onErrorResume(ex -> {
                    log.warn("MF NAV fetch failed for schemeCode={}: {}", schemeCode, ex.getMessage());
                    return Mono.empty();
                })
                .block();

            if (response == null
                    || !"SUCCESS".equals(response.status())
                    || response.data() == null
                    || response.data().isEmpty()) {
                log.warn("MF NAV response invalid for schemeCode={}", schemeCode);
                return Optional.empty();
            }

            // Latest NAV is first element
            String navStr = response.data().get(0).nav();
            if (navStr == null || navStr.isBlank()) {
                return Optional.empty();
            }

            BigDecimal nav = new BigDecimal(navStr);
            log.info("MF NAV synced: schemeCode={} nav={}", schemeCode, nav);
            return Optional.of(nav);

        } catch (Exception e) {
            log.warn("MF NAV sync error for schemeCode={}: {}", schemeCode, e.getMessage());
            return Optional.empty();
        }
    }

    // ════════════════════════════════════════
    // STOCK PRICE — Yahoo Finance
    // ════════════════════════════════════════

    /**
     * Fetch latest stock price from Yahoo Finance.
     *
     * @param symbol NSE symbol (e.g. "INFY" → calls chart for "INFY.NS")
     * @return Optional current price as BigDecimal, or empty on failure
     *
     * API: GET /v8/finance/chart/{symbol}.NS?interval=1d&range=1d
     * Response:
     * {
     *   "chart": {
     *     "result": [{
     *       "meta": { "regularMarketPrice": 1456.75 }
     *     }],
     *     "error": null
     *   }
     * }
     */
    public Optional<BigDecimal> syncStockPrice(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            log.warn("Stock sync skipped — empty symbol");
            return Optional.empty();
        }

        String yahooSymbol = symbol.toUpperCase() + ".NS";
        log.debug("Fetching stock price for symbol={} ({})", symbol, yahooSymbol);

        try {
            YahooChartResponse response = yahooClient
                .get()
                .uri("/v8/finance/chart/{symbol}?interval=1d&range=1d", yahooSymbol)
                .retrieve()
                .bodyToMono(YahooChartResponse.class)
                .timeout(Duration.ofSeconds(10))
                .retryWhen(Retry.fixedDelay(1, Duration.ofSeconds(2))
                    .filter(ex -> !(ex instanceof WebClientResponseException.NotFound)))
                .onErrorResume(ex -> {
                    log.warn("Stock price fetch failed for symbol={}: {}", symbol, ex.getMessage());
                    return Mono.empty();
                })
                .block();

            if (response == null
                    || response.chart() == null
                    || response.chart().result() == null
                    || response.chart().result().isEmpty()) {
                log.warn("Stock price response invalid for symbol={}", symbol);
                return Optional.empty();
            }

            Double price = response.chart().result().get(0).meta().regularMarketPrice();
            if (price == null || price <= 0) {
                log.warn("Invalid price returned for symbol={}: {}", symbol, price);
                return Optional.empty();
            }

            BigDecimal stockPrice = BigDecimal.valueOf(price)
                .setScale(4, java.math.RoundingMode.HALF_UP);
            log.info("Stock price synced: symbol={} price={}", symbol, stockPrice);
            return Optional.of(stockPrice);

        } catch (Exception e) {
            log.warn("Stock price sync error for symbol={}: {}", symbol, e.getMessage());
            return Optional.empty();
        }
    }

    // ════════════════════════════════════════
    // Response record types (for Jackson deserialization)
    // ════════════════════════════════════════

    // mfapi.in response
    public record MfApiResponse(
        String status,
        MfMeta meta,
        java.util.List<NavData> data
    ) {}

    public record MfMeta(String scheme_name, String scheme_code) {}

    public record NavData(String date, String nav) {}

    // Yahoo Finance response
    public record YahooChartResponse(YahooChart chart) {}

    public record YahooChart(
        java.util.List<YahooResult> result,
        Object error
    ) {}

    public record YahooResult(YahooMeta meta) {}

    public record YahooMeta(
        String symbol,
        Double regularMarketPrice,
        Double previousClose,
        String exchangeName
    ) {}
}
