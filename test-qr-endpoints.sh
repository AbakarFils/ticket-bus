#!/bin/bash
echo "🧪 build jar"
export JAVA_HOME=/tmp/jdk-17.0.18+8/Contents/Home && /tmp/apache-maven-3.9.6/bin/mvn clean package -DskipTests 2>&1 | tail -60

echo "🧪 image charge jar"
docker compose up

echo "🧪 image build jar"
docker compose build

echo "🧪 Test des Endpoints QR et Impression - TicketBus"
echo "=================================================="

# Attendre que le service démarre
echo "⏳ Attente du démarrage du service..."
sleep 5

# Test 1: Health check
echo ""
echo "1️⃣ Test Health Check"
echo "GET /actuator/health"
curl -s http://localhost:8081/actuator/health | jq . 2>/dev/null || curl -s http://localhost:8081/actuator/health
echo ""

# Test 2: Stats tickets
echo ""
echo "2️⃣ Test Statistiques Tickets"
echo "GET /api/tickets/stats"
curl -s http://localhost:8081/api/tickets/stats | jq . 2>/dev/null || curl -s http://localhost:8081/api/tickets/stats
echo ""

# Test 3: Get recent tickets
echo ""
echo "3️⃣ Test Récupération Tickets Récents"
echo "GET /api/tickets/recent"
curl -s http://localhost:8081/api/tickets/recent | jq '.[0] | {id, productName, status}' 2>/dev/null || curl -s http://localhost:8081/api/tickets/recent | head -200
echo ""

# Test 4: Get public key
echo ""
echo "4️⃣ Test Clé Publique RSA"
echo "GET /api/tickets/public-key"
curl -s http://localhost:8081/api/tickets/public-key | jq . 2>/dev/null || curl -s http://localhost:8081/api/tickets/public-key
echo ""

# Test 5: Validation QR (avec un payload simple)
echo ""
echo "5️⃣ Test Validation QR Code"
echo "POST /api/tickets/validate-qr"
curl -s -X POST http://localhost:8081/api/tickets/validate-qr \
  -H "Content-Type: application/json" \
  -d '{"qrPayload": "{\"ticketId\":1,\"signature\":\"test-signature\"}"}' \
  | jq . 2>/dev/null || curl -s -X POST http://localhost:8081/api/tickets/validate-qr \
  -H "Content-Type: application/json" \
  -d '{"qrPayload": "{\"ticketId\":1,\"signature\":\"test-signature\"}"}'
echo ""

# Test 6: Impression PDF (test de l'endpoint, pas du contenu)
echo ""
echo "6️⃣ Test Endpoint Impression PDF"
echo "GET /api/tickets/1/print"
PDF_RESPONSE=$(curl -s -I http://localhost:8081/api/tickets/1/print | head -5)
echo "$PDF_RESPONSE"
echo ""

# Test 7: Image QR Code
echo ""
echo "7️⃣ Test Endpoint Image QR"
echo "GET /api/tickets/1/qr-image"
QR_RESPONSE=$(curl -s -I http://localhost:8081/api/tickets/1/qr-image | head -5)
echo "$QR_RESPONSE"
echo ""

# Test 8: Utilisation ticket
echo ""
echo "8️⃣ Test Utilisation Ticket"
echo "POST /api/tickets/1/use"
curl -s -X POST http://localhost:8081/api/tickets/1/use \
  -H "Content-Type: application/json" \
  | jq . 2>/dev/null || curl -s -X POST http://localhost:8081/api/tickets/1/use
echo ""

echo ""
echo "✅ Tests terminés !"
echo "Si tous les endpoints retournent des réponses (même avec des erreurs métier),"
echo "cela signifie que les nouvelles fonctionnalités sont correctement déployées."
