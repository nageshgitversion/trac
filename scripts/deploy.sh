#!/bin/bash
# ═══════════════════════════════════════════════════════════════
# INVESTRAC — Kubernetes Deployment Script
# Usage:
#   ./scripts/deploy.sh dev     → Deploy to dev namespace
#   ./scripts/deploy.sh prod    → Deploy to production
#   ./scripts/deploy.sh status  → Show pod status
#   ./scripts/deploy.sh logs auth-service → Tail logs
# ═══════════════════════════════════════════════════════════════
set -e

NAMESPACE_DEV="investrac-dev"
NAMESPACE_PROD="investrac"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
K8S_DIR="$SCRIPT_DIR/../k8s"

case "$1" in
  dev)
    echo "Deploying to DEV namespace..."
    kubectl apply -k "$K8S_DIR/overlays/dev"
    kubectl rollout status deployment/auth-service        -n $NAMESPACE_DEV
    kubectl rollout status deployment/api-gateway         -n $NAMESPACE_DEV
    echo "DEV deployment complete"
    ;;

  prod)
    echo "Deploying to PRODUCTION namespace..."
    echo "WARNING: This will update production services."
    read -p "Are you sure? (yes/no): " confirm
    if [ "$confirm" != "yes" ]; then
      echo "Aborted."
      exit 1
    fi
    kubectl apply -k "$K8S_DIR/overlays/prod"
    # Wait for critical services
    kubectl rollout status deployment/auth-service        -n $NAMESPACE_PROD --timeout=5m
    kubectl rollout status deployment/api-gateway         -n $NAMESPACE_PROD --timeout=5m
    kubectl rollout status deployment/wallet-service      -n $NAMESPACE_PROD --timeout=5m
    kubectl rollout status deployment/transaction-service -n $NAMESPACE_PROD --timeout=5m
    echo "PRODUCTION deployment complete"
    ;;

  status)
    NAMESPACE=${2:-$NAMESPACE_PROD}
    echo "=== Pods in $NAMESPACE ==="
    kubectl get pods -n $NAMESPACE
    echo ""
    echo "=== Services ==="
    kubectl get services -n $NAMESPACE
    echo ""
    echo "=== HPAs ==="
    kubectl get hpa -n $NAMESPACE
    ;;

  logs)
    SERVICE=${2:-api-gateway}
    NAMESPACE=${3:-$NAMESPACE_PROD}
    echo "Tailing logs for $SERVICE in $NAMESPACE..."
    kubectl logs -f deployment/$SERVICE -n $NAMESPACE --tail=100
    ;;

  rollback)
    SERVICE=${2:-api-gateway}
    NAMESPACE=${3:-$NAMESPACE_PROD}
    echo "Rolling back $SERVICE..."
    kubectl rollout undo deployment/$SERVICE -n $NAMESPACE
    kubectl rollout status deployment/$SERVICE -n $NAMESPACE
    ;;

  *)
    echo "Usage: $0 {dev|prod|status|logs [service]|rollback [service]}"
    exit 1
    ;;
esac
