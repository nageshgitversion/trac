# 🚀 INVESTRAC - Environment Configuration Guide

## Quick Start

### 1. **Copy Environment Template**
```bash
cp .env.example .env
```

### 2. **Load Environment Variables**
```bash
# Load environment in current shell session
source ./scripts/load-env.sh

# Or if you want to validate it first:
./scripts/load-env.sh
```

### 3. **Start Infrastructure (Docker)**
```bash
docker-compose up -d
```

### 4. **Run Services**
```bash
# Automated startup with tmux
./scripts/startup-all.sh

# Or manually start individual services
java -jar infrastructure/eureka-server/target/eureka-server-1.0.0-SNAPSHOT.jar &
java -jar infrastructure/config-server/target/config-server-1.0.0-SNAPSHOT.jar &
java -jar infrastructure/api-gateway/target/api-gateway-1.0.0-SNAPSHOT.jar &
```

---

## 📋 Environment Variables Reference

### Database Configuration
```bash
# MySQL connection
DB_HOST=localhost              # Database host
DB_PORT=3306                   # Database port
DB_NAME=investrac              # Database name
DB_USER=investrac              # Database username
DB_PASSWORD=investrac123       # Database password
DB_DRIVER=com.mysql.cj.jdbc.Driver
DB_POOL_SIZE=20                # Connection pool size
DB_MAX_IDLE_TIME=900000        # Max idle time in ms
```

### Service Discovery (Eureka)
```bash
EUREKA_HOST=localhost
EUREKA_PORT=8761
EUREKA_USER=eureka-admin
EUREKA_PASSWORD=eureka-secret-2024
EUREKA_INSTANCE_PREFER_IP_ADDRESS=false
```

### Configuration Server
```bash
CONFIG_SERVER_PORT=8888
CONFIG_SERVER_USER=config-admin
CONFIG_SERVER_PASSWORD=config-secret-2024
CONFIG_REPO_URI=https://github.com/your-org/investrac-config
CONFIG_REPO_BRANCH=main
CONFIG_REPO_CLONE_ON_START=false
ENCRYPT_KEY=investrac-local-encrypt-key
```

### Message Queue (Kafka)
```bash
KAFKA_BROKERS=localhost:9092
KAFKA_GROUP_ID=investrac-services
KAFKA_CONSUMER_MAX_POLL_RECORDS=100
KAFKA_CONSUMER_SESSION_TIMEOUT_MS=30000
KAFKA_PRODUCER_RETRIES=3
```

### Caching (Redis)
```bash
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=redis-local-secret    # Empty if no auth
REDIS_DB=0
REDIS_TIMEOUT_MS=2000
REDIS_CACHE_TTL_MINUTES=30
```

### Service Ports
```bash
EUREKA_SERVER_PORT=8761
CONFIG_SERVER_PORT=8888
API_GATEWAY_PORT=8080
ADMIN_SERVER_PORT=9090
AUTH_SERVICE_PORT=8081
USER_SERVICE_PORT=8082
WALLET_SERVICE_PORT=8083
TRANSACTION_SERVICE_PORT=8084
ACCOUNT_SERVICE_PORT=8085
PORTFOLIO_SERVICE_PORT=8086
AI_SERVICE_PORT=8087
NOTIFICATION_SERVICE_PORT=8088
```

### Authentication & Security
```bash
JWT_ALGORITHM=RS256
JWT_EXPIRATION_MS=3600000              # 1 hour
JWT_REFRESH_EXPIRATION_MS=604800000    # 7 days
JWT_PRIVATE_KEY_PATH=keys/private.pem
JWT_PUBLIC_KEY_PATH=keys/public.pem
BCRYPT_STRENGTH=10                     # Password hashing strength
```

### AI Service (Claude API)
```bash
CLAUDE_API_KEY=sk-ant-xxxxx            # From Anthropic dashboard
CLAUDE_MODEL=claude-3-sonnet-20240229  # Anthropic model
CLAUDE_MAX_TOKENS=2048
CLAUDE_TEMPERATURE=0.7
CLAUDE_API_TIMEOUT_MS=30000
```

### Portfolio Service (Price APIs)
```bash
MF_API_BASE_URL=https://api.mfapi.in/mf
MF_API_KEY=                            # Optional
YAHOO_FINANCE_API_KEY=                 # Optional
PORTFOLIO_SYNC_SCHEDULE=0 0 20 * * MON-FRI  # Cron format
```

### Email Service (SMTP)
```bash
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USER=your-email@gmail.com
SMTP_PASSWORD=your-app-password        # Gmail: use app-specific password
SMTP_FROM_ADDRESS=noreply@investrac.com
SMTP_AUTH_ENABLED=true
SMTP_STARTTLS_ENABLED=true
```

### Firebase Cloud Messaging (Push Notifications)
```bash
FCM_PROJECT_ID=your-firebase-project
FCM_PRIVATE_KEY_ID=xxxxx
FCM_PRIVATE_KEY=-----BEGIN RSA PRIVATE KEY-----\n...\n-----END RSA PRIVATE KEY-----
FCM_CLIENT_EMAIL=firebase-adminsdk-xxxxx@your-project.iam.gserviceaccount.com
```

### Monitoring & Tracing
```bash
ZIPKIN_ENABLED=true
ZIPKIN_BASE_URL=http://localhost:9411
ZIPKIN_SAMPLER_PROBABILITY=0.1         # 10% sampling

PROMETHEUS_ENABLED=true
PROMETHEUS_PORT=9090
```

### Logging
```bash
LOG_LEVEL=INFO
LOG_LEVEL_INVESTRAC=INFO
LOG_LEVEL_SPRING_FRAMEWORK=WARN
LOG_LEVEL_SPRING_CLOUD=DEBUG
LOG_FILE_PATH=logs/investrac.log
LOG_FILE_MAX_SIZE=10MB
LOG_FILE_MAX_HISTORY=30                # Days to keep
```

### CORS & Security
```bash
CORS_ALLOWED_ORIGINS=http://localhost:4200,http://localhost:3000
CORS_ALLOWED_METHODS=GET,POST,PUT,DELETE,OPTIONS
CORS_ALLOWED_HEADERS=*
CORS_MAX_AGE=3600

RATE_LIMIT_ENABLED=true
RATE_LIMIT_REQUESTS_PER_MINUTE=100
RATE_LIMIT_BURST_SIZE=20
```

---

## 🔧 Common Tasks

### Regenerate JWT Keys
```bash
cd scripts
./generate-jwt-keys.sh
```

This creates `keys/private.pem` and `keys/public.pem`. Update your `.env`:
```bash
JWT_PRIVATE_KEY_PATH=keys/private.pem
JWT_PUBLIC_KEY_PATH=keys/public.pem
```

### Run Docker Infrastructure
```bash
# Start all services (MySQL, Kafka, Redis, Eureka, Zipkin, etc.)
docker-compose up -d

# View logs
docker-compose logs -f

# Stop all services
docker-compose down

# Remove volumes (clean slate)
docker-compose down -v
```

### Use Local MySQL Instead of Docker MySQL
If you already have MySQL installed locally and want to skip the Docker MySQL container:

```bash
# Start all services except Docker MySQL — connects to your local MySQL
docker-compose -f docker-compose.yml -f docker-compose.local-mysql.yml up -d
```

**Prerequisites:**
1. MySQL 8.0+ running locally on port 3306
2. A user that allows connections from Docker containers:
   ```sql
   CREATE USER IF NOT EXISTS 'root'@'%' IDENTIFIED BY 'root';
   GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' WITH GRANT OPTION;
   FLUSH PRIVILEGES;
   ```
3. On Linux, ensure MySQL binds to `0.0.0.0`:
   ```bash
   # In /etc/mysql/mysql.conf.d/mysqld.cnf
   bind-address = 0.0.0.0
   sudo systemctl restart mysql
   ```

You can also override credentials via `.env`:
```bash
DB_USER=your_user
DB_PASSWORD=your_password
```

If running services **locally** (not in Docker), they already default to
`localhost:3306` — just set your `.env` and start the JARs directly.

### Build All Modules
```bash
mvn clean install -DskipTests
```

### Start Individual Service
```bash
# Load environment
source ./scripts/load-env.sh

# Start a service with environment variables
java \
  -Dspring.datasource.url=jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME} \
  -Dspring.datasource.username=${DB_USER} \
  -Dspring.datasource.password=${DB_PASSWORD} \
  -Dspring.kafka.bootstrap-servers=${KAFKA_BROKERS} \
  -Dspring.redis.host=${REDIS_HOST} \
  -Dspring.redis.port=${REDIS_PORT} \
  -jar services/auth-service/target/auth-service-1.0.0-SNAPSHOT.jar
```

### View Active Services
```bash
# Check which services are running
ps aux | grep java

# Check port usage
lsof -i :8080  # API Gateway
lsof -i :8081  # Auth Service
lsof -i :8761  # Eureka
```

### Stop All Services
```bash
# Kill all Java processes
killall java

# Or specifically:
kill $(lsof -t -i :8080)  # Kill on port 8080
```

---

## 🐛 Troubleshooting

### Port Already in Use
```bash
# Find what's using the port
lsof -i :8080

# Kill the process
kill -9 <PID>
```

### Database Connection Failed
```bash
# Check if MySQL is running
mysql -h localhost -u investrac -p -e "SELECT 1;"

# Or with Docker
docker exec investrac-mysql mysql -u investrac -p -e "SELECT 1;"
```

### Local MySQL Not Reachable from Docker
```bash
# 1. Verify MySQL is running locally
mysql -u root -p -e "SELECT 1;"

# 2. Check MySQL is listening on all interfaces (not just 127.0.0.1)
sudo netstat -tlnp | grep 3306
# Should show 0.0.0.0:3306, NOT 127.0.0.1:3306

# 3. If bound to 127.0.0.1, edit /etc/mysql/mysql.conf.d/mysqld.cnf:
#    bind-address = 0.0.0.0
#    Then: sudo systemctl restart mysql

# 4. Verify user has remote access permissions
mysql -u root -p -e "SELECT host, user FROM mysql.user WHERE user='root';"
# Should include '%' host

# 5. Test from inside a Docker container
docker run --rm --add-host=host.docker.internal:host-gateway mysql:8.0 \
  mysql -h host.docker.internal -u root -proot -e "SELECT 1;"
```

### Kafka Connection Issues
```bash
# Check Kafka is running
docker exec investrac-kafka kafka-broker-api-versions.sh --bootstrap-server localhost:9092

# View Kafka topics
docker exec investrac-kafka kafka-topics.sh --list --bootstrap-server localhost:9092
```

### Redis Connection Issues
```bash
# Test Redis
redis-cli ping

# Or with Docker
docker exec investrac-redis redis-cli ping
```

### Service Won't Start
1. Check logs: `tail -100 logs/investrac.log`
2. Validate environment: `./scripts/load-env.sh`
3. Check port availability: `lsof -i`
4. Review service's application.yml for configuration issues

### API Gateway Returns 503 Service Unavailable
- Ensure Eureka is running on port 8761
- Check if backend services are registered
- View Eureka dashboard: `http://localhost:8761/`

---

## 📝 Production Deployment

For production, create a `.env.prod`:

```bash
# Production database
DB_HOST=prod-mysql.example.com
DB_USER=prod_user
DB_PASSWORD=<STRONG_PASSWORD>

# Production Eureka
EUREKA_HOST=prod-eureka.example.com
EUREKA_PASSWORD=<STRONG_PASSWORD>

# Production Config Server
CONFIG_REPO_URI=https://github.com/your-org/investrac-config
CONFIG_REPO_USERNAME=<GITHUB_TOKEN>
CONFIG_REPO_PASSWORD=<GITHUB_TOKEN>

# Production Encryption Key (use: openssl rand -base64 32)
ENCRYPT_KEY=<PRODUCTION_KEY>

# Production JWT Keys
JWT_PRIVATE_KEY_PATH=/etc/investrac/keys/private.pem
JWT_PUBLIC_KEY_PATH=/etc/investrac/keys/public.pem

# External APIs (Production)
CLAUDE_API_KEY=sk-ant-<REAL_KEY>

# Email Service (Production)
SMTP_USER=production-email@example.com
SMTP_PASSWORD=<PRODUCTION_PASSWORD>

# Security settings
RATE_LIMIT_REQUESTS_PER_MINUTE=1000
CORS_ALLOWED_ORIGINS=https://investrac.example.com

# Logging
LOG_LEVEL=WARN
LOG_FILE_PATH=/var/log/investrac/investrac.log
```

Load production environment:
```bash
./scripts/load-env.sh .env.prod
```

---

## 🔐 Security Best Practices

1. **Never commit `.env` to Git** - Add to `.gitignore`
2. **Use strong passwords** - Especially for production
3. **Rotate API keys regularly** - Claude, Firebase, etc.
4. **Use environment-specific secrets** - Different values for dev/staging/prod
5. **Enable HTTPS** - In production API Gateway
6. **Set strong JWT keys** - Run `scripts/generate-jwt-keys.sh`
7. **Restrict CORS origins** - Only allow your frontend domains
8. **Use secrets manager** - AWS Secrets Manager, HashiCorp Vault, etc.

---

## ✅ Verification Checklist

After starting services, verify:

- [ ] Eureka dashboard: `http://localhost:8761/`
- [ ] Config Server: `http://localhost:8888/config-server/actuator/health`
- [ ] API Gateway: `http://localhost:8080/swagger-ui.html`
- [ ] Prometheus: `http://localhost:9090/`
- [ ] Zipkin: `http://localhost:9411/`
- [ ] Admin Dashboard: `http://localhost:9090/`

All services should show "UP" in Eureka dashboard.

---

## 📚 Additional Resources

- [Spring Boot Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)
- [12 Factor App](https://12factor.net/config)
- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Kubernetes Deployment Guide](k8s/README.md)
