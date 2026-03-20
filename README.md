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

### 2. Start Infrastructure (includes Docker MySQL)
```bash
cp .env.example .env
# Edit .env with your values (defaults work out of the box with Docker MySQL)
docker-compose up -d
```
> **MySQL runs inside Docker** — no local MySQL installation needed.
> Data is persisted in the `mysql-data` Docker volume.

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

## Database Setup

### Default: Docker MySQL (Recommended)
The default `docker-compose up -d` starts a MySQL 8.0 container alongside
all services. No local MySQL installation is required.

- **Container:** `investrac-mysql` (image: `mysql:8.0`)
- **Credentials:** `root` / `root` (override via `DB_USER` / `DB_PASSWORD` in `.env`)
- **Data:** Persisted in the `mysql-data` Docker volume (survives restarts)
- **Databases:** Created automatically per service via `createDatabaseIfNotExist=true`

```bash
# Start everything (MySQL + Kafka + Redis + services)
docker-compose up -d

# Verify MySQL is healthy
docker exec investrac-mysql mysqladmin status -u root -proot

# Connect to MySQL shell
docker exec -it investrac-mysql mysql -u root -proot
```

### Optional: Local MySQL Instead of Docker MySQL
If you prefer to use your own local MySQL installation:

#### Option A: Services in Docker, MySQL on Host
Use the provided override file — disables the Docker MySQL container:
```bash
docker-compose -f docker-compose.yml -f docker-compose.local-mysql.yml up -d
```
The override points all services to `host.docker.internal` so they connect
to your host MySQL directly.

**Prepare your local MySQL:**
```sql
-- Allow connections from Docker containers
CREATE USER IF NOT EXISTS 'root'@'%' IDENTIFIED BY 'root';
GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' WITH GRANT OPTION;
FLUSH PRIVILEGES;
```

> Databases (`investrac_auth`, `investrac_user`, etc.) are created
> automatically via `createDatabaseIfNotExist=true` in the JDBC URL.

#### Option B: Run Services Locally (No Docker for Services)
The `application.yml` files already default to `localhost`:
```bash
source ./scripts/load-env.sh   # loads .env (DB_HOST=localhost)
mvn clean install -DskipTests
java -jar services/auth-service/target/auth-service-1.0.0-SNAPSHOT.jar
```

#### Option C: Kubernetes (Dev Overlay)
The dev overlay sets `DB_HOST=localhost` via ConfigMap, so services
connect to a local MySQL without needing separate MySQL K8s manifests:
```bash
kubectl apply -k k8s/overlays/dev
```
To point at a different host, patch `DB_HOST` in the dev overlay's
`configMapGenerator`.

#### Linux Note
On Linux, `host.docker.internal` is mapped via `extra_hosts` in the override file.
If you have issues, ensure your MySQL `bind-address` is set to `0.0.0.0`
in `/etc/mysql/mysql.conf.d/mysqld.cnf` and restart MySQL:
```bash
sudo systemctl restart mysql
```
