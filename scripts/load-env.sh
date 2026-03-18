#!/bin/bash
# ════════════════════════════════════════════════════════════════════════════════
# INVESTRAC - Load Environment Variables from .env
# ════════════════════════════════════════════════════════════════════════════════
# Usage: source ./scripts/load-env.sh
#        or ./scripts/load-env.sh <env-file>
#        or source load-env.sh (from project root)

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Get the project root - prefer PWD if it contains .env
if [ -f ".env" ]; then
    PROJECT_ROOT="$(pwd)"
elif [ -f "$(dirname "$0")/../.env" ]; then
    PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
else
    PROJECT_ROOT="$(pwd)"
fi

# Determine which env file to use
ENV_FILE="${1:-.env}"

# If relative path, make it relative to project root
if [[ ! "$ENV_FILE" = /* ]]; then
    ENV_FILE="$PROJECT_ROOT/$ENV_FILE"
fi

# Check if .env file exists
if [ ! -f "$ENV_FILE" ]; then
    echo -e "${RED}❌ Error: .env file not found at $ENV_FILE${NC}"
    echo -e "${YELLOW}ℹ️  Please copy .env.example to .env and update values${NC}"
    exit 1
fi

# Function to load and validate env file
load_env_file() {
    local env_file=$1
    
    # Load all variables from .env file (but don't export yet)
    set -a
    source "$env_file"
    set +a
    
    echo -e "${GREEN}✓ Environment variables loaded from $env_file${NC}"
}

# Function to validate required variables
validate_required_vars() {
    local required_vars=(
        "DB_HOST"
        "DB_USER"
        "DB_PASSWORD"
        "EUREKA_HOST"
        "EUREKA_PORT"
        "CONFIG_SERVER_USER"
        "CONFIG_SERVER_PASSWORD"
        "SPRING_PROFILES_ACTIVE"
    )
    
    local missing_vars=()
    
    for var in "${required_vars[@]}"; do
        if [ -z "${!var}" ]; then
            missing_vars+=("$var")
        fi
    done
    
    if [ ${#missing_vars[@]} -gt 0 ]; then
        echo -e "${RED}❌ Missing required environment variables:${NC}"
        for var in "${missing_vars[@]}"; do
            echo -e "${RED}   - $var${NC}"
        done
        return 1
    fi
    
    return 0
}

# Function to display loaded variables (censored)
display_loaded_vars() {
    echo ""
    echo -e "${GREEN}═══════════════════════════════════════════════════════════════════${NC}"
    echo -e "${GREEN}Environment Configuration${NC}"
    echo -e "${GREEN}═══════════════════════════════════════════════════════════════════${NC}"
    
    echo -e "\n${YELLOW}Database:${NC}"
    echo "  Host: $DB_HOST:$DB_PORT"
    echo "  Database: $DB_NAME"
    echo "  User: $DB_USER"
    
    echo -e "\n${YELLOW}Service Discovery:${NC}"
    echo "  Eureka: $EUREKA_HOST:$EUREKA_PORT"
    echo "  User: $EUREKA_USER"
    
    echo -e "\n${YELLOW}Config Server:${NC}"
    echo "  Port: $CONFIG_SERVER_PORT"
    echo "  User: $CONFIG_SERVER_USER"
    echo "  Repository: $CONFIG_REPO_URI"
    
    echo -e "\n${YELLOW}Message Queue:${NC}"
    echo "  Kafka: $KAFKA_BROKERS"
    
    echo -e "\n${YELLOW}Cache:${NC}"
    echo "  Redis: $REDIS_HOST:$REDIS_PORT"
    
    echo -e "\n${YELLOW}Service Ports:${NC}"
    echo "  API Gateway: $API_GATEWAY_PORT"
    echo "  Auth Service: $AUTH_SERVICE_PORT"
    echo "  User Service: $USER_SERVICE_PORT"
    echo "  Portfolio Service: $PORTFOLIO_SERVICE_PORT"
    echo "  AI Service: $AI_SERVICE_PORT"
    
    echo -e "\n${YELLOW}Spring Profile:${NC}"
    echo "  Profile: $SPRING_PROFILES_ACTIVE"
    
    echo -e "\n${GREEN}═══════════════════════════════════════════════════════════════════${NC}"
}

# Main execution
echo -e "${YELLOW}Loading environment from: $ENV_FILE${NC}"

if load_env_file "$ENV_FILE"; then
    if validate_required_vars; then
        display_loaded_vars
        echo -e "\n${GREEN}✓ All environment variables loaded successfully!${NC}\n"
        return 0 2>/dev/null || exit 0
    else
        echo -e "\n${RED}❌ Environment validation failed${NC}"
        return 1 2>/dev/null || exit 1
    fi
else
    echo -e "${RED}❌ Failed to load environment file${NC}"
    return 1 2>/dev/null || exit 1
fi
