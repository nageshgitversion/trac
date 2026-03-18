# INVESTRAC — Master Architecture Context
# Paste this at the start of EVERY Claude session

## Stack
- Java 17, Spring Boot 3.2.3, Maven Multi-Module
- MySQL 8.0 (one DB per service), Redis 7, Apache Kafka 3.6
- Spring Cloud 2023.0.0: Gateway, Eureka, Config, Resilience4j
- Zipkin (tracing), Spring Boot Admin 3.2.1, ELK (logging)
- JWT RS256 asymmetric keys, BCrypt strength 12
- Flyway for DB migrations, MapStruct 1.5.5 for mapping
- Lombok 1.18.30, Docker + Docker Compose, Kubernetes

## Group ID / Package Convention
- groupId: com.investrac
- Package: com.investrac.{servicename}.{layer}
- Example: com.investrac.wallet.service.WalletService

## Service Ports
- eureka-server:      8761
- config-server:      8888
- api-gateway:        8080
- admin-server:       9090
- auth-service:       8081
- user-service:       8082
- wallet-service:     8083
- transaction-service:8084
- account-service:    8085
- portfolio-service:  8086
- ai-service:         8087
- notification-service:8088

## Databases (one per service — never shared)
- investrac_auth, investrac_user, investrac_wallet
- investrac_transaction, investrac_account
- investrac_portfolio, investrac_ai, investrac_notification

## API Standard Response
{
  "success": true/false,
  "message": "...",
  "data": {...},
  "errorCode": "WLTH-XXXX",
  "timestamp": "2026-03-16T09:41:00Z",
  "traceId": "abc123"
}

## Error Codes
WLTH-1001=Invalid credentials, WLTH-1002=Token expired
WLTH-1003=Token invalid, WLTH-1004=Account locked
WLTH-1005=Email exists, WLTH-2001=Wallet not found
WLTH-2002=Wallet exists, WLTH-2003=Insufficient balance
WLTH-3001=Transaction not found, WLTH-3002=Transaction failed
WLTH-4001=Account not found, WLTH-5001=Service unavailable

## Kafka Topics
investrac.transaction.created  → wallet-service, notification-service
investrac.wallet.debited       → transaction-service
investrac.wallet.credited      → notification-service
investrac.transaction.completed→ notification-service
investrac.transaction.failed   → notification-service
investrac.account.emi-due      → notification-service
investrac.portfolio.synced     → notification-service, ai-service
investrac.wallet.low-balance   → notification-service
investrac.user.registered      → notification-service

## SAGA Pattern: Choreography-based
1. transaction-service saves tx (PENDING) + outbox event
2. Outbox scheduler publishes → investrac.transaction.created
3. wallet-service consumes → checks idempotency (sagaId) → deducts balance
4. wallet-service publishes → investrac.wallet.debited {success/failure}
5. transaction-service consumes → marks tx COMPLETED or FAILED
6. notification-service consumes → sends push notification

## Security Rules
- RS256 JWT: Only auth-service has private key, all others verify with public key
- BCrypt strength 12 for all passwords
- PAN/Aadhaar encrypted AES-256 before storing
- Never log: passwords, tokens, PAN, full account numbers
- Every DTO validated with Jakarta Bean Validation
- Outbox Pattern for ALL Kafka events (never lost)
- Idempotency guard (SagaProcessedEvent table) in every consumer

## Session Progress — Update after each session
✅ Session 1: Parent POM + project skeleton (281 dirs)
✅ Session 1: common-dto (ApiResponse, PagedResponse, ErrorCodes)
✅ Session 1: common-events (all 8 Kafka event records)
✅ Session 1: eureka-server (full)
✅ Session 1: config-server (full)
✅ Session 1: api-gateway (JWT filter, circuit breaker, rate limiting)
✅ Session 1: admin-server (full)
✅ Session 1: auth-service (entities, repos, DTOs, service, controller, tests)
✅ Session 1: wallet-service (entities, repos, DTOs, service, SAGA consumer, outbox)
✅ Session 1: Flyway migrations (auth + wallet + transaction + account + portfolio)
✅ Session 1: docker-compose.yml (full stack)
✅ Session 1: .env + generate-jwt-keys.sh + .gitignore
✅ Session 1: GitHub Actions CI pipeline
✅ Session 1: Dockerfiles (all services)

✅ Session 2: transaction-service (full)
❌ Session 3 (NEXT): account-service (MaturityCalculator, EMI scheduler)
✅ Session 4: portfolio-service (Holding, PriceHistory, PriceSyncService, scheduler, controller, tests)
✅ Session 5: ai-service (ClaudeApiClient, PromptBuilder, AiService, NightlyInsightScheduler, controller, tests)
✅ Session 6: notification-service (full — all 6 Kafka consumers, FCM, email, 16 tests)
✅ Session 7: user-service (full — profile, KYC AES-256, preferences, 16 tests)
✅ Session 8: common-security (JwtPublicKeyProvider, JwtTokenVerifier, JwtClaims, GatewayHeaderAuthFilter, BaseSecurityConfig, RateLimitingService, 12 tests)
✅ Session 9: Kubernetes manifests (all services, HPA, network policy, ingress, dev/prod overlays, deploy.sh)
✅ Session 10: Angular 17 PWA (auth, home, transactions, wallet, portfolio, settings, 36 TS files, 22 tests)

## Next Session Prompt (copy-paste)
Paste this full INVESTRAC_CONTEXT.md.
Then say:

"Generate the COMPLETE transaction-service with:
- Transaction entity (id, userId, walletId, accountId, type, category,
  name, amount, envelopeKey, txDate, note, source, status, sagaId)
- TransactionRepository with queries for monthly summary, category totals,
  date range search
- CreateTransactionRequest DTO with full Jakarta validation
- TransactionResponse DTO
- TransactionService:
  * createTransaction() — saves tx PENDING, publishes to outbox
  * getTransactions() — paged, filterable by type/category/date
  * updateTransaction() — reverse old wallet effect, apply new
  * deleteTransaction() — soft delete + compensate wallet
  * getMonthSummary() — income/expense/investment totals
- WalletDebitedEventConsumer — SAGA Step 3
  * Listens to investrac.wallet.debited
  * Marks tx COMPLETED or FAILED
  * Publishes investrac.transaction.completed or .failed
- TransactionEventProducer — Outbox pattern
- TransactionController — all endpoints with Swagger
- GlobalExceptionHandler
- application.yml for transaction-service
- V1 migration (already created — skip)
- TransactionServiceTest (unit tests)"

## Session 4 Prompt (portfolio-service)
Paste this full INVESTRAC_CONTEXT.md. Then say:

"Generate the COMPLETE portfolio-service with:
- Holding entity (id, userId, type, name, symbol, units, buyPrice,
  currentPrice, invested, currentValue, xirr, sipAmount, isUpdatable,
  lastSynced, note, isActive)
- PriceHistory entity (holdingId, price, recordedAt)
- HoldingRepository with portfolio summary queries (totalInvested,
  totalCurrentValue, by type breakdown, holdings needing price sync)
- PriceSyncService:
  * syncMutualFundNav(schemeCode) — calls https://api.mfapi.in/mf/{code}
  * syncStockPrice(symbol) — calls Yahoo Finance /v8/finance/chart/{symbol}.NS
  * syncAll(userId) — syncs all updatable holdings for a user
  * Uses WebClient (reactive), timeout 5s, fallback on error
- PortfolioService:
  * createHolding() — saves holding, calculates currentValue from units×nav
  * getPortfolioSummary() — totalInvested, totalValue, totalReturn%,
    XIRR (weighted average), assetAllocation breakdown
  * updateHolding() — partial update
  * deleteHolding() — soft delete
  * getHoldingById()
- DailySyncScheduler:
  * @Scheduled cron '0 0 20 * * MON-FRI' (8 PM IST after market close)
  * Finds all users with updatable holdings
  * Calls priceSyncService.syncAll() for each
  * Publishes PortfolioSyncedEvent via Outbox after sync
- PortfolioController — all endpoints with Swagger
- HoldingRequest/Response DTOs with validation
- PortfolioSummaryResponse DTO
- GlobalExceptionHandler
- SecurityConfig + KafkaConfig
- application.yml (port 8086)
- Flyway V1 migration (already created — skip, but add V2 for price_history)
- PortfolioServiceTest (unit tests)
- MaturityCalculatorTest equivalent: PriceSyncServiceTest (mock HTTP responses)"
