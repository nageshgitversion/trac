# INVESTRAC — Intelligent Investment Tracker

Production-ready microservices fintech application.

## Stack
Spring Boot 3.2 · MySQL 8 · Kafka · Redis · Angular 17

## Quick Start

### 1. Generate JWT Keys
```bash
chmod +x scripts/generate-jwt-keys.sh
./scripts/generate-jwt-keys.sh
# Copy output to .env file
```

### 2. Start Infrastructure
```bash
cp .env.example .env
# Edit .env with your values
docker-compose up -d zookeeper kafka redis zipkin \
  mysql-auth mysql-wallet mysql-transaction mysql-portfolio mysql-account
```

### 3. Start Services (in order)
```bash
docker-compose up -d eureka-server
docker-compose up -d config-server
docker-compose up -d api-gateway admin-server
docker-compose up -d auth-service wallet-service
```

### 4. Access
| Service         | URL                          |
|----------------|------------------------------|
| API Gateway    | http://localhost:8080         |
| Eureka         | http://localhost:8761         |
| Zipkin         | http://localhost:9411         |
| Admin Server   | http://localhost:9090         |
| Kafka UI       | http://localhost:8090         |
| Auth Swagger   | http://localhost:8081/swagger-ui.html |
| Wallet Swagger | http://localhost:8083/swagger-ui.html |

## Build All
```bash
mvn clean install -DskipTests
```

## Run Tests
```bash
mvn test
```

## Service Architecture

```
Angular PWA → Nginx → API Gateway (8080)
                           ↓
             ┌─────────────┼─────────────┐
             ↓             ↓             ↓
         auth:8081    wallet:8083  transaction:8084
             ↓             ↓             ↓
         MySQL Auth   MySQL Wallet  MySQL Txn
                      Kafka ←→ SAGA choreography
```

## Security Notes
- Never commit `.env` to Git
- JWT private key only in auth-service
- All passwords BCrypt strength 12
- Run OWASP check: `mvn verify -P security-check`
