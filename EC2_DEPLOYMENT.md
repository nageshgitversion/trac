# Deploying INVESTRAC on AWS EC2 with Docker Compose

This guide walks you through deploying the full INVESTRAC stack on an AWS EC2
instance using Docker Compose. **All databases run as Docker containers** — no
local MySQL installation is needed.

---

## Prerequisites

| Requirement | Details |
|-------------|---------|
| AWS Account | With permission to create EC2 instances and Security Groups |
| EC2 instance | **t3.large** or larger (≥ 8 GB RAM recommended for all services) |
| AMI | Amazon Linux 2023 or Ubuntu 22.04 LTS |
| Key pair | For SSH access |
| Storage | ≥ 30 GB gp3 root volume |

---

## Step 1 — Launch an EC2 Instance

1. Open **AWS Console → EC2 → Launch Instance**.
2. Choose **Amazon Linux 2023** or **Ubuntu 22.04 LTS**.
3. Select instance type **t3.large** (2 vCPU / 8 GB RAM) or **t3.xlarge** for
   production loads.
4. Attach your key pair.
5. Configure storage: **30 GB gp3** minimum.
6. In **Network settings**, create or select a Security Group and add inbound
   rules (see Step 2).
7. Launch the instance and note its **Public IPv4 address**.

---

## Step 2 — Configure the Security Group

Open only the ports that external clients need. Internal services (MySQL, Redis,
Kafka, Zookeeper) are **not** exposed to the internet.

| Port | Protocol | Source | Purpose |
|------|----------|--------|---------|
| 22   | TCP | Your IP only | SSH access |
| 9000 | TCP | 0.0.0.0/0 | API Gateway (primary entry point) |
| 9090 | TCP | Your IP only | Admin Server |
| 8761 | TCP | Your IP only | Eureka dashboard (optional) |

> **Security tip:** Restrict Admin Server and Eureka to your IP instead of
> `0.0.0.0/0`.

---

## Step 3 — SSH into the Instance

```bash
ssh -i /path/to/your-key.pem ec2-user@<EC2_PUBLIC_IP>
# or for Ubuntu:
ssh -i /path/to/your-key.pem ubuntu@<EC2_PUBLIC_IP>
```

---

## Step 4 — Install Docker and Docker Compose

### Amazon Linux 2023

```bash
sudo dnf update -y
sudo dnf install -y docker
sudo systemctl enable --now docker
sudo usermod -aG docker ec2-user
newgrp docker

# Install Docker Compose plugin
sudo mkdir -p /usr/local/lib/docker/cli-plugins
sudo curl -SL https://github.com/docker/compose/releases/latest/download/docker-compose-linux-x86_64 \
  -o /usr/local/lib/docker/cli-plugins/docker-compose
sudo chmod +x /usr/local/lib/docker/cli-plugins/docker-compose
docker compose version
```

### Ubuntu 22.04 LTS

```bash
sudo apt-get update -y
sudo apt-get install -y ca-certificates curl gnupg lsb-release
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg \
  | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] \
  https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" \
  | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
sudo apt-get update -y
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
sudo systemctl enable --now docker
sudo usermod -aG docker ubuntu
newgrp docker
docker compose version
```

---

## Step 5 — Copy the Repository to EC2

**Option A — Git clone** (if the repo is on GitHub):
```bash
sudo dnf install -y git   # Amazon Linux
# or: sudo apt-get install -y git   # Ubuntu

git clone https://github.com/nageshgitversion/trac.git investrac
cd investrac
```

**Option B — rsync from your local machine** (if not on GitHub):
```bash
# Run this on your LOCAL machine
rsync -avz --exclude '.git' --exclude 'target' \
  /path/to/local/trac/ \
  ec2-user@<EC2_PUBLIC_IP>:~/investrac/
```

---

## Step 6 — Generate JWT Keys

```bash
cd ~/investrac
chmod +x scripts/generate-jwt-keys.sh
./scripts/generate-jwt-keys.sh
# The script prints JWT_PRIVATE_KEY and JWT_PUBLIC_KEY values — copy them for the next step.
```

---

## Step 7 — Configure Environment Variables

```bash
cp .env.ec2.example .env
nano .env    # or: vim .env
```

Fill in **all** placeholder values:

| Variable | How to obtain |
|----------|--------------|
| `EC2_HOST` | Your EC2 **Public IPv4** or public DNS hostname |
| `DB_PASSWORD` | Choose a strong password (≥ 16 characters) |
| `REDIS_PASSWORD` | Choose a strong password |
| `JWT_PRIVATE_KEY` | Output of `scripts/generate-jwt-keys.sh` |
| `JWT_PUBLIC_KEY` | Output of `scripts/generate-jwt-keys.sh` |
| `CLAUDE_API_KEY` | Anthropic console |
| `ENCRYPT_KEY` | Run: `openssl rand -base64 32` |
| `ENCRYPTION_KEY` | Run: `openssl rand -hex 32` |
| `SMTP_*` | Your email provider credentials |

> **MySQL runs entirely inside Docker** (`DB_HOST` is set to `mysql` inside
> `docker-compose.yml` — you do not need to install MySQL on the EC2 host).

---

## Step 8 — Build and Start the Stack

Build all service images (takes 5–15 minutes on first run):

```bash
docker compose -f docker-compose.yml -f docker-compose.ec2.yml build
```

Start the full stack:

```bash
docker compose -f docker-compose.yml -f docker-compose.ec2.yml up -d
```

---

## Step 9 — Verify the Deployment

### Check that all containers are running

```bash
docker compose -f docker-compose.yml -f docker-compose.ec2.yml ps
```

All services should show **Up** or **healthy** within ~5 minutes.

### Watch startup logs

```bash
# Watch all services
docker compose -f docker-compose.yml -f docker-compose.ec2.yml logs -f

# Watch a specific service
docker compose -f docker-compose.yml -f docker-compose.ec2.yml logs -f auth-service
```

### Health-check endpoints

Replace `<EC2_PUBLIC_IP>` with your instance's public IP:

| Service | URL |
|---------|-----|
| API Gateway | `http://<EC2_PUBLIC_IP>:9000/actuator/health` |
| Eureka | `http://<EC2_PUBLIC_IP>:8761` |
| Admin Server | `http://<EC2_PUBLIC_IP>:9090` |

---

## Step 10 — Start on Boot (Optional)

Create a systemd service so Docker Compose restarts automatically after a
reboot:

```bash
sudo tee /etc/systemd/system/investrac.service > /dev/null <<'EOF'
[Unit]
Description=INVESTRAC Docker Compose Stack
Requires=docker.service
After=docker.service network-online.target

[Service]
Type=oneshot
RemainAfterExit=yes
WorkingDirectory=/home/ec2-user/investrac
ExecStart=/usr/local/lib/docker/cli-plugins/docker-compose \
  -f docker-compose.yml -f docker-compose.ec2.yml up -d
ExecStop=/usr/local/lib/docker/cli-plugins/docker-compose \
  -f docker-compose.yml -f docker-compose.ec2.yml down
User=ec2-user

[Install]
WantedBy=multi-user.target
EOF

sudo systemctl daemon-reload
sudo systemctl enable investrac
```

> Adjust `WorkingDirectory` and `User` if you used Ubuntu (`ubuntu`) or a
> different home directory.

---

## Data Persistence

All persistent data lives in named Docker volumes on the EC2 host:

| Volume | Contents |
|--------|----------|
| `investrac_mysql-data` | MySQL databases |
| `investrac_redis-data` | Redis cache |
| `investrac_kafka-data` | Kafka message log |
| `investrac_zookeeper-data` | Zookeeper state |

Volumes survive `docker compose down` but are removed by `docker compose down -v`.
Back up MySQL regularly (see below).

### MySQL Backup

```bash
# Dump all databases
docker exec investrac-mysql \
  mysqldump -u root -p"${DB_PASSWORD}" --all-databases \
  > backup-$(date +%Y%m%d).sql

# Restore
docker exec -i investrac-mysql \
  mysql -u root -p"${DB_PASSWORD}" < backup-20240101.sql
```

---

## Updating the Stack

```bash
cd ~/investrac
git pull                          # fetch latest code
docker compose -f docker-compose.yml -f docker-compose.ec2.yml build
docker compose -f docker-compose.yml -f docker-compose.ec2.yml up -d
```

Only the changed service containers are recreated; others keep running.

---

## Troubleshooting

### Services fail to start (OOM / exit code 137)

The EC2 instance may not have enough RAM. Check:
```bash
free -h
docker stats --no-stream
```
Upgrade to **t3.xlarge** (16 GB) or reduce `memory` limits in
`docker-compose.yml`.

### MySQL container not healthy

```bash
docker logs investrac-mysql
docker exec investrac-mysql mysqladmin status -u root -p"${DB_PASSWORD}"
```

If MySQL logs show `InnoDB: Initializing buffer pool`, wait 1–2 minutes.

### Kafka not ready

Kafka is the slowest service to start. Wait up to 3 minutes, then:
```bash
docker logs investrac-kafka
```

### Cannot connect to API Gateway externally

1. Verify the EC2 Security Group allows port 9000 from your IP.
2. Confirm the container is healthy: `docker inspect investrac-gateway | grep Health`.
3. Check Eureka for registered services: `http://<EC2_PUBLIC_IP>:8761`.

---

## Architecture on EC2

```
Internet
   │
   ▼  port 9000
[ EC2 Security Group ]
   │
   ▼
[ API Gateway (Docker) ]
   │
   ├─── auth-service:8081
   ├─── user-service:8082
   ├─── wallet-service:8083
   ├─── transaction-service:8084
   ├─── account-service:8085
   ├─── portfolio-service:8086
   ├─── ai-service:8087
   └─── notification-service:8088
         │           │
         ▼           ▼
    [ MySQL 8.0 ] [ Redis 7 ]   ← Docker volumes on EC2 EBS
    [ Kafka ]   [ Zookeeper ]
    [ Zipkin ]  [ Kafka UI ]    ← internal only
```

All containers share the `investrac-network` bridge. Only the API Gateway port
(9000) needs to be open to the internet.
