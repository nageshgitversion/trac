package com.investrac.portfolio.sync;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * PriceSyncService tests using MockWebServer (OkHttp).
 *
 * MockWebServer lets us test the full WebClient flow without hitting real APIs.
 * It starts a local HTTP server that returns predefined responses.
 *
 * Dependency needed in pom.xml (already included via spring-boot-starter-webflux test):
 *   com.squareup.okhttp3:mockwebserver
 */
@DisplayName("PriceSyncService Unit Tests")
class PriceSyncServiceTest {

    private static MockWebServer mockServer;
    private PriceSyncService priceSyncService;

    @BeforeAll
    static void startServer() throws IOException {
        mockServer = new MockWebServer();
        mockServer.start();
    }

    @BeforeEach
    void setUp() {
        String baseUrl = mockServer.url("/").toString();
        // Point both MF and Yahoo to same mock server for simplicity
        priceSyncService = new PriceSyncService(
            baseUrl, baseUrl, WebClient.builder()
        );
    }

    @AfterAll
    static void stopServer() throws IOException {
        mockServer.shutdown();
    }

    // ═══════════════════════════════════════════
    // MUTUAL FUND NAV
    // ═══════════════════════════════════════════

    @Test
    @DisplayName("syncMutualFundNav: returns correct NAV from valid response")
    void mf_ValidResponse_ReturnsNav() {
        String mockBody = """
            {
              "status": "SUCCESS",
              "meta": {
                "scheme_name": "HDFC Nifty 50 Index Direct Growth",
                "scheme_code": "119598"
              },
              "data": [
                {"date": "16-03-2026", "nav": "124.5678"},
                {"date": "15-03-2026", "nav": "123.1234"}
              ]
            }
            """;

        mockServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setBody(mockBody));

        Optional<BigDecimal> result = priceSyncService.syncMutualFundNav("119598");

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualByComparingTo("124.5678");
    }

    @Test
    @DisplayName("syncMutualFundNav: returns empty on 404 (invalid scheme code)")
    void mf_NotFound_ReturnsEmpty() {
        mockServer.enqueue(new MockResponse().setResponseCode(404));

        Optional<BigDecimal> result = priceSyncService.syncMutualFundNav("INVALID");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("syncMutualFundNav: returns empty when status is not SUCCESS")
    void mf_FailedStatus_ReturnsEmpty() {
        mockServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setBody("""
                {"status": "FAILURE", "data": [], "meta": {}}
                """));

        Optional<BigDecimal> result = priceSyncService.syncMutualFundNav("119598");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("syncMutualFundNav: returns empty when data array is empty")
    void mf_EmptyData_ReturnsEmpty() {
        mockServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setBody("""
                {"status": "SUCCESS", "data": [], "meta": {}}
                """));

        Optional<BigDecimal> result = priceSyncService.syncMutualFundNav("119598");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("syncMutualFundNav: returns empty for null/blank scheme code")
    void mf_NullSymbol_ReturnsEmpty() {
        assertThat(priceSyncService.syncMutualFundNav(null)).isEmpty();
        assertThat(priceSyncService.syncMutualFundNav("")).isEmpty();
        assertThat(priceSyncService.syncMutualFundNav("  ")).isEmpty();
    }

    @Test
    @DisplayName("syncMutualFundNav: returns empty on server error (500)")
    void mf_ServerError_ReturnsEmpty() {
        mockServer.enqueue(new MockResponse().setResponseCode(500));
        mockServer.enqueue(new MockResponse().setResponseCode(500)); // retry

        Optional<BigDecimal> result = priceSyncService.syncMutualFundNav("119598");

        assertThat(result).isEmpty();
    }

    // ═══════════════════════════════════════════
    // STOCK PRICES
    // ═══════════════════════════════════════════

    @Test
    @DisplayName("syncStockPrice: returns correct price from valid Yahoo response")
    void stock_ValidResponse_ReturnsPrice() {
        String mockBody = """
            {
              "chart": {
                "result": [{
                  "meta": {
                    "symbol": "INFY.NS",
                    "regularMarketPrice": 1456.75,
                    "previousClose": 1440.20,
                    "exchangeName": "NSE"
                  }
                }],
                "error": null
              }
            }
            """;

        mockServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setBody(mockBody));

        Optional<BigDecimal> result = priceSyncService.syncStockPrice("INFY");

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualByComparingTo("1456.7500");
    }

    @Test
    @DisplayName("syncStockPrice: appends .NS to symbol for Yahoo API call")
    void stock_AppendsDotNs() throws Exception {
        mockServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setBody("""
                {"chart":{"result":[{"meta":{"regularMarketPrice":1456.75}}],"error":null}}
                """));

        priceSyncService.syncStockPrice("TCS");

        // Verify the request path contained .NS
        var recordedRequest = mockServer.takeRequest();
        assertThat(recordedRequest.getPath()).contains("TCS.NS");
    }

    @Test
    @DisplayName("syncStockPrice: returns empty when result array is empty")
    void stock_EmptyResult_ReturnsEmpty() {
        mockServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setBody("""
                {"chart":{"result":[],"error":null}}
                """));

        Optional<BigDecimal> result = priceSyncService.syncStockPrice("INFY");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("syncStockPrice: returns empty for null/blank symbol")
    void stock_NullSymbol_ReturnsEmpty() {
        assertThat(priceSyncService.syncStockPrice(null)).isEmpty();
        assertThat(priceSyncService.syncStockPrice("")).isEmpty();
    }

    @Test
    @DisplayName("syncStockPrice: returns empty when regularMarketPrice is 0 or negative")
    void stock_ZeroPrice_ReturnsEmpty() {
        mockServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setBody("""
                {"chart":{"result":[{"meta":{"regularMarketPrice":0.0}}],"error":null}}
                """));

        Optional<BigDecimal> result = priceSyncService.syncStockPrice("INFY");

        assertThat(result).isEmpty();
    }
}
