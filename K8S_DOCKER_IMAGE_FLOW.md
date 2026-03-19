# Kubernetes Docker Image Flow

This document explains how Kubernetes gets Docker images in the INVESTRAC platform — the complete journey from source code to a running pod.

---

## Architecture Overview

```
┌──────────┐    push     ┌───────────────┐   build & push   ┌────────────┐
│  GitHub   │───────────▶│ GitHub Actions │─────────────────▶│ Docker Hub │
│   Repo    │            │  CI Pipeline   │                  │ (Registry) │
└──────────┘             └───────────────┘                   └─────┬──────┘
                                                                   │
                                                             image pull
                                                                   │
┌───────────────────────────────────────────────────────────────────▼──────┐
│                        Kubernetes Cluster                                │
│  ┌─────────────┐    ┌──────────────┐    ┌───────────────────────────┐   │
│  │  Kustomize   │──▶│   Kubelet    │──▶│   Running Pods             │   │
│  │ (tag overlay)│   │ (image pull) │   │  investrac/auth-service    │   │
│  └─────────────┘    └──────────────┘   │  investrac/api-gateway     │   │
│                                         │  investrac/wallet-service  │   │
│                                         │  ...                       │   │
│                                         └───────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 1. Where Are Images Stored?

All Docker images are hosted on **Docker Hub** under the `investrac` namespace:

| Image Name | Service | Port |
|---|---|---|
| `investrac/eureka-server` | Service Discovery | 8761 |
| `investrac/api-gateway` | API Gateway | 8080 |
| `investrac/auth-service` | Authentication | 8081 |
| `investrac/user-service` | User Management | 8082 |
| `investrac/wallet-service` | Wallet Management | 8083 |
| `investrac/transaction-service` | Transactions | 8084 |
| `investrac/account-service` | Accounts | 8085 |
| `investrac/portfolio-service` | Portfolio | 8086 |
| `investrac/ai-service` | AI Analytics | 8087 |
| `investrac/notification-service` | Notifications | 8088 |

Each image is tagged with:
- **`latest`** — the most recent build from the `main` branch
- **`<commit-sha>`** — pinned to a specific Git commit for traceability

---

## 2. How Are Images Built? (CI/CD Pipeline)

Images are built and pushed by **GitHub Actions** (`.github/workflows/ci.yml`).

### Trigger

The pipeline runs on every push to `main` or `develop` and on pull requests to those branches. Docker images are **only built and pushed when code is merged to `main`**.

### Pipeline Stages

```
Code Push / PR
      │
      ▼
┌─────────────────────┐
│  1. Build & Test     │  All branches — compile, unit test, coverage
│     (Maven + JDK 17) │  Uses MySQL 8.0 + Redis 7.2 service containers
└──────────┬──────────┘
           ▼
┌─────────────────────┐
│  2. Security Scan    │  OWASP Dependency Check
└──────────┬──────────┘
           ▼
┌─────────────────────────────────────┐
│  3. Docker Build & Push             │  ◀── Only on main branch
│     (matrix: 11 services in parallel)│
└─────────────────────────────────────┘
```

### Docker Build Details (Stage 3)

For each of the 11 services in parallel:

1. **Install parent POM** — `mvn install -N -DskipTests`
2. **Build common modules** — `mvn install -pl common/common-dto,common/common-events,common/common-security`
3. **Package service JAR** — `mvn package -pl <service-path> -DskipTests`
4. **Build Docker image** using the service's multi-stage `Dockerfile`
5. **Push to Docker Hub** with tags `latest` and `<commit-sha>`

Authentication to Docker Hub uses repository secrets: `DOCKER_USERNAME` and `DOCKER_PASSWORD`.

### Multi-Stage Dockerfile

Every service uses the same Dockerfile pattern:

```dockerfile
# Stage 1: Build the JAR
FROM maven:3.9-amazoncorretto-17 AS builder
WORKDIR /build
COPY . .
RUN mvn -DskipTests clean package -q

# Stage 2: Runtime image
FROM amazoncorretto:17-alpine-jdk
WORKDIR /app
RUN apk add --no-cache curl && addgroup -S appgroup && adduser -D -S -G appgroup appuser
COPY --from=builder --chown=appuser:appgroup /build/<service-path>/target/*.jar app.jar
USER appuser
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -XX:+ExitOnOutOfMemoryError"
EXPOSE <port>
HEALTHCHECK --interval=20s --timeout=10s --start-period=120s --retries=5 \
  CMD curl -f http://localhost:<port>/actuator/health || exit 1
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

---

## 3. How Does Kubernetes Pull Images?

### Kustomize Image Tag Management

The project uses **Kustomize** to manage image tags across environments. Each K8s deployment manifest references a base image like `investrac/auth-service:latest`, and Kustomize overlays override the tag per environment.

**Base** (`k8s/base/kustomization.yaml`) — default tag `1.0.0`:
```yaml
images:
  - name: investrac/auth-service
    newTag: "1.0.0"
  - name: investrac/api-gateway
    newTag: "1.0.0"
  # ... all services
```

**Dev overlay** (`k8s/overlays/dev/kustomization.yaml`) — tag `dev`:
```yaml
images:
  - name: investrac/auth-service
    newTag: "dev"
  - name: investrac/wallet-service
    newTag: "dev"
  # ...
```

**Prod overlay** (`k8s/overlays/prod/kustomization.yaml`) — tag `latest`:
```yaml
images:
  - name: investrac/auth-service
    newTag: "latest"
  - name: investrac/api-gateway
    newTag: "latest"
  # ...
```

### Image Pull Policy

All deployments set `imagePullPolicy: Always`, which forces the kubelet to pull a fresh image from Docker Hub on every pod creation — even if a cached copy exists locally. This ensures the `latest` tag always resolves to the newest build.

```yaml
containers:
  - name: auth-service
    image: investrac/auth-service:latest
    imagePullPolicy: Always
```

### Deploying to Kubernetes

Apply the appropriate overlay with `kubectl`:

```bash
# Production
kubectl apply -k k8s/overlays/prod/

# Development
kubectl apply -k k8s/overlays/dev/
```

Kustomize merges the base manifests with the overlay patches and replaces image tags before applying to the cluster.

---

## 4. End-to-End Flow

Here is the complete flow from a code commit to a running Kubernetes pod:

```
 1. Developer pushes code to the main branch on GitHub
                        │
                        ▼
 2. GitHub Actions CI triggers automatically
    ├─ Build & test all modules (Maven + JDK 17)
    ├─ Run OWASP security scan
    └─ (only if all pass and branch is main) continue to Docker build
                        │
                        ▼
 3. Docker Build (runs in parallel for all 11 services)
    ├─ Install parent POM and common dependencies
    ├─ Package service JAR with Maven
    ├─ Build Docker image via multi-stage Dockerfile
    │    ├─ Stage 1: maven:3.9-amazoncorretto-17 compiles the JAR
    │    └─ Stage 2: amazoncorretto:17-alpine-jdk runs the JAR
    ├─ Log in to Docker Hub (secrets: DOCKER_USERNAME / DOCKER_PASSWORD)
    └─ Push image with two tags:
         ├─ investrac/<service>:latest
         └─ investrac/<service>:<commit-sha>
                        │
                        ▼
 4. Images are now on Docker Hub
    e.g. investrac/auth-service:latest
                        │
                        ▼
 5. Deploy to Kubernetes
    Run: kubectl apply -k k8s/overlays/prod/
    ├─ Kustomize merges base manifests + prod overlay
    └─ Image tags are replaced (e.g. :1.0.0 → :latest)
                        │
                        ▼
 6. Kubelet pulls images from Docker Hub
    ├─ imagePullPolicy: Always → always pulls fresh
    └─ Downloads investrac/<service>:latest from Docker Hub
                        │
                        ▼
 7. Pod starts
    ├─ Container created from pulled image
    ├─ ConfigMap mounted (EUREKA_HOST, DB_HOST, KAFKA_SERVERS, etc.)
    ├─ Secrets mounted (db-password, jwt-keys, etc.)
    ├─ Health checks begin (liveness + readiness probes)
    └─ Rolling update: maxSurge=1, maxUnavailable=0 (zero downtime)
                        │
                        ▼
 8. Service discovery
    ├─ Pod registers with Eureka (EUREKA_HOST=eureka-server)
    ├─ API Gateway discovers backend services
    └─ Traffic flows: Client → API Gateway → Service Pods
```

---

## 5. Environment-Specific Configuration

Pods receive configuration via **ConfigMaps** and **Secrets**, not baked into the Docker image:

| Source | Key Examples | Purpose |
|---|---|---|
| `investrac-config` ConfigMap | `DB_HOST`, `EUREKA_HOST`, `KAFKA_SERVERS`, `REDIS_HOST` | Service connectivity |
| `investrac-secrets` Secret | `db-password`, `jwt-public-key`, `redis-password`, `eureka-password` | Sensitive credentials |

The dev overlay overrides `DB_HOST=localhost` for local database development.

---

## 6. Local Development (docker-compose)

For local development, `docker-compose.yml` builds images directly from Dockerfiles instead of pulling from Docker Hub:

```yaml
auth-service:
  build:
    context: .
    dockerfile: services/auth-service/Dockerfile
```

This means locally images are built on-the-fly from source code, while in Kubernetes they are pulled from Docker Hub after being pushed by CI.

---

## Key Files Reference

| File | Purpose |
|---|---|
| `.github/workflows/ci.yml` | CI/CD pipeline — builds, tests, and pushes Docker images |
| `k8s/base/kustomization.yaml` | Base Kustomize config with default image tags |
| `k8s/overlays/dev/kustomization.yaml` | Dev overlay — `dev` tags, 1 replica, local DB |
| `k8s/overlays/prod/kustomization.yaml` | Prod overlay — `latest` tags, higher replicas |
| `k8s/base/services/*.yaml` | K8s Deployment + Service + HPA per service |
| `k8s/base/config/configmap.yaml` | Non-secret environment configuration |
| `k8s/base/config/secrets.yaml` | Secret credentials (values must be replaced) |
| `services/*/Dockerfile` | Multi-stage Dockerfiles for each business service |
| `infrastructure/*/Dockerfile` | Multi-stage Dockerfiles for infrastructure services |
| `docker-compose.yml` | Local development with on-the-fly image builds |
