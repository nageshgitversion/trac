# Deployment Platform Guide — INVESTRAC

This guide compares cloud platforms for deploying INVESTRAC and provides
step-by-step instructions for the recommended options.

## Application Requirements

Before choosing a platform, consider what INVESTRAC needs:

| Requirement | Detail |
|-------------|--------|
| **Containers** | 12 Java services + 1 Angular frontend |
| **Kubernetes** | Kustomize manifests already in `k8s/` |
| **MySQL 8** | Relational database for all services |
| **Kafka** | Event streaming (SAGA choreography) |
| **Redis** | Distributed caching and sessions |
| **Zipkin** | Distributed tracing |
| **Memory** | ~6–8 GB total for all services |
| **CI/CD** | GitHub Actions pipeline in `.github/workflows/ci.yml` |

---

## Platform Comparison

| Platform | Managed K8s | Managed MySQL | Managed Kafka | Managed Redis | Free Tier | Starting Cost (est.) |
|----------|:-----------:|:-------------:|:-------------:|:-------------:|:---------:|---------------------:|
| **AWS (EKS)** | ✅ EKS | ✅ RDS | ✅ MSK | ✅ ElastiCache | Limited | ~$150–300/mo |
| **Google Cloud (GKE)** | ✅ GKE | ✅ Cloud SQL | ✅ Managed Kafka | ✅ Memorystore | $300 credit | ~$120–250/mo |
| **Azure (AKS)** | ✅ AKS | ✅ Azure MySQL | ✅ Event Hubs | ✅ Azure Cache | $200 credit | ~$130–270/mo |
| **DigitalOcean (DOKS)** | ✅ DOKS | ✅ Managed DB | ❌ Self-host | ✅ Managed Redis | $200 credit | ~$80–160/mo |
| **Railway** | ❌ | ✅ MySQL | ❌ Self-host | ✅ Redis | $5 credit | ~$30–60/mo |
| **Render** | ❌ | ✅ PostgreSQL¹ | ❌ Self-host | ✅ Redis | Free tier | ~$50–100/mo |
| **Fly.io** | ❌ | ✅ Postgres¹ | ❌ Self-host | ✅ Upstash Redis | Free tier | ~$40–80/mo |
| **Hetzner** | ❌ Self-managed | ❌ Self-host | ❌ Self-host | ❌ Self-host | — | ~$30–60/mo |

¹ PostgreSQL, not MySQL — would require migration.

---

## Recommended Platforms

### 1. AWS (EKS) — Best for Production

**Why:** Full managed service coverage, enterprise-grade reliability, and
the strongest Kafka offering (Amazon MSK).

**Architecture:**
```
Route 53 (DNS)
    └── ALB (HTTPS termination)
         └── EKS Cluster
              ├── api-gateway  ←  Service mesh
              ├── auth-service
              ├── wallet-service
              ├── transaction-service
              └── ...other services
Amazon RDS (MySQL 8)
Amazon MSK (Kafka)
Amazon ElastiCache (Redis)
```

**Setup Steps:**

```bash
# 1. Create EKS cluster
eksctl create cluster \
  --name investrac \
  --region us-east-1 \
  --nodegroup-name workers \
  --node-type t3.medium \
  --nodes 3 \
  --managed

# 2. Create RDS MySQL instance
aws rds create-db-instance \
  --db-instance-identifier investrac-db \
  --engine mysql \
  --engine-version 8.0 \
  --db-instance-class db.t3.medium \
  --allocated-storage 20 \
  --master-username admin \
  --master-user-password <password>

# 3. Create ElastiCache Redis
aws elasticache create-cache-cluster \
  --cache-cluster-id investrac-redis \
  --engine redis \
  --cache-node-type cache.t3.micro \
  --num-cache-nodes 1

# 4. Update ConfigMap with managed service endpoints
kubectl create configmap investrac-config \
  --from-literal=DB_HOST=investrac-db.xxxxx.us-east-1.rds.amazonaws.com \
  --from-literal=KAFKA_SERVERS=<msk-bootstrap-servers> \
  --from-literal=REDIS_HOST=investrac-redis.xxxxx.cache.amazonaws.com \
  -n investrac

# 5. Deploy application
kubectl apply -k k8s/overlays/prod
```

**Estimated Cost:** $150–300/month (3 t3.medium nodes + RDS + ElastiCache + MSK)

---

### 2. Google Cloud (GKE) — Best Balance of Cost and Features

**Why:** GKE Autopilot reduces operational overhead, Cloud SQL is
cost-effective, and you get $300 in free credits to start.

**Architecture:**
```
Cloud DNS
    └── Cloud Load Balancer (HTTPS)
         └── GKE Autopilot
              ├── api-gateway
              ├── auth-service
              └── ...services
Cloud SQL (MySQL 8)
Memorystore (Redis)
```

**Setup Steps:**

```bash
# 1. Create GKE Autopilot cluster
gcloud container clusters create-auto investrac \
  --region us-central1

# 2. Create Cloud SQL MySQL instance
gcloud sql instances create investrac-db \
  --database-version=MYSQL_8_0 \
  --tier=db-f1-micro \
  --region=us-central1

# 3. Create Memorystore Redis
gcloud redis instances create investrac-redis \
  --size=1 \
  --region=us-central1

# 4. Deploy Kafka on GKE (Strimzi operator)
kubectl apply -f https://strimzi.io/install/latest?namespace=investrac

# 5. Deploy application
kubectl apply -k k8s/overlays/prod
```

**Estimated Cost:** $120–250/month (Autopilot + Cloud SQL + Memorystore)

---

### 3. DigitalOcean (DOKS) — Best for Small Teams / Budget

**Why:** Simplest managed Kubernetes, lower cost, and straightforward
pricing. Good for teams that want Kubernetes without AWS complexity.

**Architecture:**
```
DO Load Balancer
    └── DOKS (Kubernetes)
         ├── api-gateway
         ├── Kafka (self-hosted on DOKS)
         └── ...services
Managed MySQL
Managed Redis
```

**Setup Steps:**

```bash
# 1. Create Kubernetes cluster via doctl
doctl kubernetes cluster create investrac \
  --region nyc1 \
  --size s-2vcpu-4gb \
  --count 3

# 2. Create managed MySQL
doctl databases create investrac-db \
  --engine mysql \
  --version 8 \
  --size db-s-1vcpu-1gb \
  --region nyc1

# 3. Create managed Redis
doctl databases create investrac-redis \
  --engine redis \
  --size db-s-1vcpu-1gb \
  --region nyc1

# 4. Deploy Kafka on DOKS (Strimzi or Bitnami Helm chart)
helm install kafka oci://registry-1.docker.io/bitnamicharts/kafka \
  -n investrac

# 5. Deploy application
kubectl apply -k k8s/overlays/prod
```

**Estimated Cost:** $80–160/month (3 nodes + Managed DB + Managed Redis)

---

### 4. Railway — Best for Quick Deployment / Prototyping

**Why:** Zero infrastructure management, deploy directly from GitHub, and
built-in MySQL and Redis. Ideal for demos and early-stage development.

> **Note:** Railway does not provide Kubernetes. Each service runs as a
> separate container. For INVESTRAC's 12+ services this can get expensive
> at scale, but it is the fastest way to get a running deployment.

**Setup Steps:**

1. Connect your GitHub repository to [railway.app](https://railway.app)
2. Create a new project and add services:
   - One Railway service per microservice (point each to its Dockerfile)
   - One MySQL plugin
   - One Redis plugin
   - One Kafka service (use Bitnami Kafka image)
3. Set environment variables in Railway dashboard (copy from `.env.example`)
4. Railway auto-deploys on every push to `main`

**Estimated Cost:** $30–60/month (usage-based, good for low traffic)

---

## Decision Matrix

Choose based on your priorities:

| Priority | Recommended Platform |
|----------|---------------------|
| **Production fintech workload** | AWS EKS |
| **Cost + managed services balance** | Google Cloud GKE |
| **Simple K8s + tight budget** | DigitalOcean DOKS |
| **Fastest time-to-deploy** | Railway |
| **Maximum control + lowest cost** | Hetzner (self-managed) |
| **Microsoft ecosystem** | Azure AKS |

---

## CI/CD Integration

The existing GitHub Actions pipeline (`.github/workflows/ci.yml`) can push
Docker images to any registry. Update the `docker-build` job to push to
your chosen platform's container registry:

```yaml
# AWS ECR
- name: Push to ECR
  run: |
    aws ecr get-login-password | docker login --username AWS --password-stdin $ECR_REGISTRY
    docker push $ECR_REGISTRY/investrac/$SERVICE:$TAG

# Google Artifact Registry
- name: Push to GAR
  run: |
    gcloud auth configure-docker us-central1-docker.pkg.dev
    docker push us-central1-docker.pkg.dev/$PROJECT/investrac/$SERVICE:$TAG

# DigitalOcean Container Registry
- name: Push to DOCR
  run: |
    doctl registry login
    docker push registry.digitalocean.com/investrac/$SERVICE:$TAG
```

---

## Pre-Deployment Checklist

Before deploying to any platform, ensure the following:

- [ ] JWT keys generated (`./scripts/generate-jwt-keys.sh`)
- [ ] `.env` configured with production values
- [ ] Docker images built and pushed to a container registry
- [ ] Kubernetes secrets created for DB passwords, JWT keys, API keys
- [ ] MySQL databases will be auto-created (`createDatabaseIfNotExist=true`)
- [ ] Kafka topics are created on first use by the services
- [ ] DNS and TLS certificates configured for your domain
- [ ] Health check endpoints verified (`/actuator/health` on each service)
- [ ] Resource limits reviewed in `k8s/base/services/*.yaml`

---

## Further Reading

- [README.md](README.md) — Quick start and local development
- [ENV_SETUP.md](ENV_SETUP.md) — Full environment variable reference
- [k8s/](k8s/) — Kubernetes manifests (Kustomize)
- [scripts/deploy.sh](scripts/deploy.sh) — Kubernetes deployment helper script
