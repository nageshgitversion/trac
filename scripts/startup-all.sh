#!/bin/bash
# ════════════════════════════════════════════════════════════════════════════════
# INVESTRAC - Start All Services with Environment Variables
# ════════════════════════════════════════════════════════════════════════════════
# Usage: ./scripts/startup-all.sh
#        ./scripts/startup-all.sh <env-file>

set -e

# Color codes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Get project root
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
ENV_FILE="${1:-.env}"

# Load environment
if [[ ! "$ENV_FILE" = /* ]]; then
    ENV_FILE="$PROJECT_ROOT/$ENV_FILE"
fi

echo -e "${BLUE}════════════════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}INVESTRAC - Service Startup${NC}"
echo -e "${BLUE}════════════════════════════════════════════════════════════════════${NC}\n"

# Load environment variables
if [ ! -f "$ENV_FILE" ]; then
    echo -e "${RED}❌ Error: .env file not found at $ENV_FILE${NC}"
    exit 1
fi

echo -e "${YELLOW}Loading environment from: $ENV_FILE${NC}"
set -a
source "$ENV_FILE"
set +a

# Check prerequisites
echo -e "\n${YELLOW}Checking prerequisites...${NC}"

# Function to check if port is available
check_port() {
    local port=$1
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
        return 1  # Port is in use
    fi
    return 0  # Port is available
}

# Function to check if service is running
check_service() {
    local port=$1
    local name=$2
    
    if check_port $port; then
        echo -e "${GREEN}✓ Port $port available for $name${NC}"
        return 0
    else
        echo -e "${RED}✗ Port $port already in use (${name})${NC}"
        return 1
    fi
}

# Check all ports
PORTS_OK=true
check_service $EUREKA_SERVER_PORT "Eureka Server" || PORTS_OK=false
check_service $CONFIG_SERVER_PORT "Config Server" || PORTS_OK=false
check_service $API_GATEWAY_PORT "API Gateway" || PORTS_OK=false
check_service $AUTH_SERVICE_PORT "Auth Service" || PORTS_OK=false

if [ "$PORTS_OK" = false ]; then
    echo -e "\n${RED}❌ Some ports are already in use. Please free them or modify .env${NC}"
    exit 1
fi

# Check if database is running
echo -e "\n${YELLOW}Checking database connection...${NC}"
if command -v mysqladmin &> /dev/null; then
    if mysqladmin ping -h "$DB_HOST" -u "$DB_USER" -p"$DB_PASSWORD" >/dev/null 2>&1; then
        echo -e "${GREEN}✓ Database is running${NC}"
    else
        echo -e "${YELLOW}⚠ Cannot connect to database. Make sure MySQL is running:${NC}"
        echo "  Host: $DB_HOST"
        echo "  Port: $DB_PORT"
        echo "  User: $DB_USER"
    fi
fi

# Check if Kafka is running
echo -e "\n${YELLOW}Checking Kafka connection...${NC}"
if (echo > /dev/tcp/$(echo $KAFKA_BROKERS | cut -d: -f1)/$(echo $KAFKA_BROKERS | cut -d: -f2)) 2>/dev/null; then
    echo -e "${GREEN}✓ Kafka is running${NC}"
else
    echo -e "${YELLOW}⚠ Cannot connect to Kafka. Make sure it's running on $KAFKA_BROKERS${NC}"
fi

# Check if Redis is running
echo -e "\n${YELLOW}Checking Redis connection...${NC}"
if (echo > /dev/tcp/$REDIS_HOST/$REDIS_PORT) 2>/dev/null; then
    echo -e "${GREEN}✓ Redis is running${NC}"
else
    echo -e "${YELLOW}⚠ Cannot connect to Redis. Make sure it's running on $REDIS_HOST:$REDIS_PORT${NC}"
fi

# Display startup plan
echo -e "\n${GREEN}═══════════════════════════════════════════════════════════════════${NC}"
echo -e "${GREEN}Startup Plan${NC}"
echo -e "${GREEN}═══════════════════════════════════════════════════════════════════${NC}\n"

echo -e "${BLUE}Infrastructure Services (start in order):${NC}"
echo "  1. Eureka Server (http://localhost:$EUREKA_SERVER_PORT)"
echo "  2. Config Server (http://localhost:$CONFIG_SERVER_PORT)"
echo "  3. API Gateway (http://localhost:$API_GATEWAY_PORT)"
echo "  4. Admin Server (http://localhost:$ADMIN_SERVER_PORT)"

echo -e "\n${BLUE}Core Services:${NC}"
echo "  5. Auth Service (http://localhost:$AUTH_SERVICE_PORT)"
echo "  6. User Service (http://localhost:$USER_SERVICE_PORT)"
echo "  7. Wallet Service (http://localhost:$WALLET_SERVICE_PORT)"

echo -e "\n${BLUE}Business Services:${NC}"
echo "  8. Transaction Service (http://localhost:$TRANSACTION_SERVICE_PORT)"
echo "  9. Account Service (http://localhost:$ACCOUNT_SERVICE_PORT)"
echo "  10. Portfolio Service (http://localhost:$PORTFOLIO_SERVICE_PORT)"
echo "  11. AI Service (http://localhost:$AI_SERVICE_PORT)"
echo "  12. Notification Service (http://localhost:$NOTIFICATION_SERVICE_PORT)"

echo -e "\n${YELLOW}Available commands:${NC}"
echo "  source <(scripts/load-env.sh)         - Load environment only"
echo "  ./scripts/startup-eureka.sh           - Start Eureka"
echo "  ./scripts/startup-config.sh           - Start Config Server"
echo "  ./scripts/startup-api-gateway.sh      - Start API Gateway"
echo "  ./scripts/startup-all-services.sh     - Start all services (requires tmux)"

echo -e "\n${YELLOW}Docker alternative:${NC}"
echo "  docker-compose up -d                   - Start all infrastructure"
echo "  docker-compose logs -f                 - View logs"

echo -e "\n${GREEN}═══════════════════════════════════════════════════════════════════${NC}"
echo -e "${GREEN}✓ Environment ready for service startup${NC}"
echo -e "${GREEN}═══════════════════════════════════════════════════════════════════${NC}\n"

# Offer to start services
read -p "Would you like to start Eureka Server now? (y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    # Start Eureka Server
    EUREKA_JAR="$PROJECT_ROOT/infrastructure/eureka-server/target/eureka-server-1.0.0-SNAPSHOT.jar"
    if [ -f "$EUREKA_JAR" ]; then
        echo -e "${GREEN}Starting Eureka Server...${NC}"
        java -jar "$EUREKA_JAR" &
        sleep 5
        echo -e "${GREEN}✓ Eureka Server started${NC}"
    else
        echo -e "${RED}❌ Eureka Server JAR not found. Run 'mvn clean install' first${NC}"
    fi
fi
