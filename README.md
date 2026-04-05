# 🚌 TicketBus — Système de billettique bus professionnel

Système complet de billettique pour réseaux de transport urbain, avec QR codes signés asymétriquement, validation offline, anti-fraude, et back-office.

## Architecture

```
App Client  ──►  API Gateway (8080)
                  ├── Ticketing Service  (8081)
                  ├── Wallet Service     (8082)
                  ├── Validation Service (8083)
                  ├── Pricing Service    (8084)
                  └── Payment Service   (8085)

App Contrôleur ──►  Validation API + cache offline

Back-office Angular ──► API Gateway
```

## Modules

| Module | Port | Rôle |
|--------|------|------|
| `api-gateway` | 8080 | Point d'entrée unique, routage, CORS |
| `ticketing-service` | 8081 | Génération tickets QR signés RSA-2048 |
| `wallet-service` | 8082 | Portefeuille rechargeable, débit |
| `validation-service` | 8083 | Scan QR, anti-rejeu Redis, ValidationEvent |
| `pricing-service` | 8084 | Produits, tarifs, Best Fare Engine |
| `payment-service` | 8085 | Passerelle paiement (stub) |
| `common` | — | Entités JPA partagées, DTOs, enums |
| `back-office` | 4200 | Back-office Angular Material |

## Démarrage rapide

### Prérequis
- Java 21+
- Maven 3.9+
- Docker & Docker Compose
- Node.js 18+ (pour le back-office)

### Infrastructure (PostgreSQL, Redis, Keycloak)

```bash
docker compose up postgres redis keycloak -d
```

### Backend (tous les services)

```bash
mvn clean package -DskipTests
# Puis dans des terminaux séparés :
java -jar api-gateway/target/*.jar
java -jar ticketing-service/target/*.jar
java -jar validation-service/target/*.jar
java -jar wallet-service/target/*.jar
java -jar pricing-service/target/*.jar
java -jar payment-service/target/*.jar
```

### Tout avec Docker Compose

```bash
mvn clean package -DskipTests
docker compose up --build
```

### Back-office Angular

```bash
cd back-office
npm install
npm start
# Accès : http://localhost:4200
```

## API Principales

### Acheter un ticket
```http
POST http://localhost:8080/api/tickets/purchase
Content-Type: application/json

{ "userId": 1, "productId": 1 }
```

### Scanner / Valider un ticket
```http
POST http://localhost:8080/api/validate
Content-Type: application/json

{
  "qrPayload": "1|1|<nonce>|<validFrom>|<validUntil>|1",
  "signature": "<base64-RSA-signature>",
  "terminalId": "BUS-42",
  "location": "Arrêt Central",
  "offline": false
}
```

### Recharger un wallet
```http
POST http://localhost:8080/api/wallets/1/topup
Content-Type: application/json

{ "amount": 10000 }
```

### Produits disponibles
```http
GET http://localhost:8080/api/pricing/products
```

### Recommandation tarifaire
```http
GET http://localhost:8080/api/pricing/recommend?userId=1&tripsPerMonth=25
```

## Sécurité & Anti-fraude

### Signature RSA asymétrique
- Chaque ticket est signé avec la clé privée du serveur (RSA-2048, SHA256withRSA).
- Le terminal de contrôle embarque uniquement la clé publique.
- Le payload signé est : `ticketId|userId|nonce|validFrom|validUntil|maxUsage`.

### Anti-rejeu via Redis
- Chaque nonce de ticket est stocké dans Redis avec TTL 48h après validation.
- Un verrou Redis `lock:ticket:{id}` (10s) prévient les scans concurrents.

### Détection collision temporelle
- Si le même ticket est validé à des emplacements différents dans une fenêtre de 5 min → alerte log.

### Configuration des clés RSA (production)
```yaml
# ticketing-service/src/main/resources/application.yml
ticketing:
  rsa:
    private-key: <base64-PKCS8-private-key>
    public-key: <base64-X509-public-key>
```
En dev, une paire de clés est générée automatiquement et la clé publique est loguée au démarrage.

## Produits par défaut (initialisés automatiquement)

| Produit | Type | Prix | Usages | Durée |
|---------|------|------|--------|-------|
| Ticket unitaire | UNIT | 500 XAF | 1 | — |
| Pack 10 trajets | PACK | 4 500 XAF | 10 | — |
| Pass journée | PASS | 1 500 XAF | illimité | 1 jour |
| Pass semaine | PASS | 8 000 XAF | illimité | 7 jours |
| Pass mois | PASS | 25 000 XAF | illimité | 30 jours |

## Entités clés

| Entité | Champs principaux |
|--------|-------------------|
| `Ticket` | id, userId, productId, nonce, validFrom, validUntil, signature, usageCount, maxUsage, status |
| `ValidationEvent` | id, ticketId, terminalId, location, timestamp, offline, result |
| `Wallet` | id, userId, balance, currency |
| `Product` | id, name, type, price, maxUsage, durationDays |
| `User` | id, email, passwordHash, role |

## Roadmap

### Phase 1 — MVP ✅
- [x] QR signé RSA-2048
- [x] Validation avec anti-rejeu Redis
- [x] Wallet rechargeable
- [x] Produits tarifaires (UNIT, PACK, PASS)
- [x] API Gateway
- [x] Back-office Angular

### Phase 2 — Intelligence tarifaire
- [ ] Best Fare Engine (recommandation automatique)
- [ ] Budget planner (plafonnement journalier/mensuel)
- [ ] Carnets et abonnements avancés

### Phase 3 — Industrialisation
- [ ] App Android contrôleur avec mode offline complet
- [ ] Rotation visuelle du QR code (renouvellement 30s)
- [ ] Alertes fraude temps réel
- [ ] Observabilité (ELK, Prometheus, OpenTelemetry)
- [ ] Multi-opérateurs / multi-zones

## Stack technique

- **Backend** : Java 21, Spring Boot 3.2, Spring Security, Spring Cloud Gateway
- **Base de données** : PostgreSQL 16
- **Cache / Anti-rejeu** : Redis 7
- **Identité** : Keycloak 23
- **Crypto** : RSA-2048 / SHA256withRSA (JJWT)
- **Front-office** : Angular 17 + Angular Material
- **Infra** : Docker Compose, prêt Kubernetes
