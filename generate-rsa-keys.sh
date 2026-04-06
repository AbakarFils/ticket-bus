#!/bin/bash
# Generate RSA key pair for ticket signing (shared between ticketing-service and validation-service)
# Usage: ./generate-rsa-keys.sh
# The output can be pasted into docker-compose.yml environment variables

echo "Generating RSA-2048 key pair..."

# Generate private key in PKCS#8 DER format, then base64
PRIVATE_KEY=$(openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -outform DER 2>/dev/null | base64 | tr -d '\n')

# Extract public key in DER format, then base64
PUBLIC_KEY=$(echo "$PRIVATE_KEY" | base64 -d | openssl rsa -pubout -outform DER 2>/dev/null | base64 | tr -d '\n')

echo ""
echo "=== RSA Keys Generated ==="
echo ""
echo "Add these to your docker-compose.yml environment variables:"
echo ""
echo "For ticketing-service:"
echo "  RSA_PRIVATE_KEY: ${PRIVATE_KEY}"
echo "  RSA_PUBLIC_KEY: ${PUBLIC_KEY}"
echo ""
echo "For validation-service:"
echo "  RSA_PUBLIC_KEY: ${PUBLIC_KEY}"
echo ""
echo "=== Or add to .env file ==="
echo ""
echo "RSA_PRIVATE_KEY=${PRIVATE_KEY}"
echo "RSA_PUBLIC_KEY=${PUBLIC_KEY}"

