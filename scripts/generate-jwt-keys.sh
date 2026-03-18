#!/bin/bash
# ═══════════════════════════════════════════════════
# Generate RS256 JWT key pair for INVESTRAC
# Run once and store keys in AWS Secrets Manager
# ═══════════════════════════════════════════════════

set -e
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
KEYS_DIR="$SCRIPT_DIR/../keys"
mkdir -p "$KEYS_DIR"

echo "Generating RSA 2048-bit key pair..."

# Generate private key
openssl genrsa -out "$KEYS_DIR/private.pem" 2048

# Extract public key
openssl rsa -in "$KEYS_DIR/private.pem" -pubout -out "$KEYS_DIR/public.pem"

# Convert to base64 for use in environment variables
PRIVATE_KEY_B64=$(openssl pkcs8 -topk8 -nocrypt -in "$KEYS_DIR/private.pem" -outform DER | base64 | tr -d '\n')
PUBLIC_KEY_B64=$(openssl rsa -in "$KEYS_DIR/private.pem" -pubout -outform DER 2>/dev/null | base64 | tr -d '\n')

echo ""
echo "===== ADD THESE TO YOUR .env FILE ====="
echo ""
echo "JWT_PRIVATE_KEY=$PRIVATE_KEY_B64"
echo ""
echo "JWT_PUBLIC_KEY=$PUBLIC_KEY_B64"
echo ""
echo "===== KEYS ALSO SAVED TO: $KEYS_DIR ====="
echo "IMPORTANT: Add keys/ to .gitignore — NEVER commit private keys to Git!"
echo ""

# Clean up PEM files
rm -f "$KEYS_DIR/private.pem" "$KEYS_DIR/public.pem"
