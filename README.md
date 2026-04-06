# TicketBus — Système de Validation de Tickets de Bus

Système complet de validation de tickets de bus avec **mode offline** et mécanismes **anti-fraude** : backend Spring Boot, dashboard Angular 19 et application Android pour contrôleurs de terrain.

## Architecture

```
ticket-bus/
├── backend/    # API REST Spring Boot 3.2.5 (Java 17) — port 8080
├── frontend/   # Dashboard Angular 19.2.20 (SCSS, standalone)
└── mobile/     # Application Android Kotlin (SDK 34, Room, WorkManager)
```

---

## Backend (Spring Boot 3.2.5)

### Lancer
```bash
cd backend
mvn spring-boot:run
# API : http://localhost:8080
# H2 Console : http://localhost:8080/h2-console
# Admin : admin / admin123 (créé automatiquement)
```

### API Endpoints

| Méthode | Endpoint | Rôle requis | Description |
|---------|----------|-------------|-------------|
| POST | /api/auth/login | Public | Authentification JWT |
| POST | /api/auth/register | Public | Inscription |
| POST | /api/tickets | ADMIN | Créer un ticket |
| GET | /api/tickets | Authentifié | Liste paginée |
| GET | /api/tickets/{id}/qrcode | Authentifié | Image QR (PNG 300×300) |
| PUT | /api/tickets/{id}/cancel | Authentifié | Annuler un ticket |
| POST | /api/validation/validate | CONTROLLER | Valider un QR (7 étapes) |
| GET | /api/validation/events | Authentifié | Historique |
| POST | /api/sync/upload | CONTROLLER | Upload validations offline |
| GET | /api/sync/blacklist | Authentifié | Deltas blacklist |
| GET | /api/sync/public-key | **Public** | Clé publique RSA courante |
| POST | /api/keys/rotate | ADMIN | Rotation de clés |

### Pipeline de validation (7 étapes)
1. **Signature** — vérification SHA256withRSA de la payload JSON
2. **Statut** — ticket doit être ACTIVE
3. **Fenêtre d'activation** — `activationWindowStart/End` si configurée
4. **Blacklist** — vérification en base
5. **Rejeu nonce** — nonce déjà vu dans les dernières 24h → SUSPECT
6. **Compteur d'usage** — `usageCount < maxUsageCount`
7. **Collision temporelle** — même ticket, lieu différent, <5 min → SUSPECT

### Format QR Code (signé RSA-2048/SHA256withRSA)
```json
{
  "ticketNumber": "TKT-XXXXXXXX",
  "nonce": "uuid-unique",
  "timestamp": "2024-01-15T10:30:00",
  "passenger": "Jean Dupont",
  "route": "Ligne 42",
  "departure": "Gare Centrale",
  "arrival": "Aéroport",
  "validFrom": "2024-01-15T08:00:00",
  "validUntil": "2024-01-15T23:59:00",
  "signature": "base64-SHA256withRSA"
}
```

---

## Frontend (Angular 19.2.20)

> ⚠️ Angular 19.2.20 est requis — les versions 17/18 ont des vulnérabilités XSS/XSRF non corrigées (≤18.2.14).

### Lancer
```bash
cd frontend
npm install
npx ng serve
# Accès : http://localhost:4200
```

### Fonctionnalités
- **Dashboard** — statistiques, validations récentes, statut online/offline
- **Tickets** — création (formulaire réactif), liste paginée, détail avec QR
- **QR rotation** — rafraîchissement automatique toutes les 30s (anti-capture)
- **Validation** — paste/saisie JSON QR, détection VALID/INVALID/SUSPECT
- **Mode offline** — file d'attente localStorage, sync auto à la reconnexion
- **Sync** — état synchronisation, cache blacklist et clé publique

---

## Mobile Android (Kotlin, SDK 34)

### Compiler
```bash
cd mobile
./gradlew assembleDebug   # nécessite Android SDK
```

### Pipeline de validation offline
1. Parse du JSON QR → extraction payload
2. Vérification **signature RSA** (clé publique en cache `SharedPreferences`)
3. Vérification **expiration** (`validUntil`)
4. Vérification **blacklist locale** (Room DB)
5. Détection **rejeu nonce** (nonce déjà vu en cache Room)
6. Enregistrement `PendingValidation` → sync auto (WorkManager, 15 min)

---

## Mécanismes Anti-Fraude

| Menace | Protection |
|--------|-----------|
| Réutilisation du même QR | Nonce UUID unique + compteur `usageCount` |
| Partage de capture d'écran | Fenêtre d'activation courte + rotation QR 30s |
| Double scan multi-bus | Détection collision temporelle (<5 min, lieu ≠) → SUSPECT |
| Faux tickets (forgés) | Signature RSA-2048 + rotation de clés via `/api/keys/rotate` |

---

## Sécurité
- JWT stateless (jjwt 0.12.x) — rôles ADMIN / CONTROLLER / PASSENGER
- BCrypt pour les mots de passe
- Angular 19.2.20 (corrige CVE XSS/XSRF présents en 17/18)
- Clé RSA auto-générée au démarrage (`DataInitializer`)
