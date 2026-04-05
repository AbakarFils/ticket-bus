# Ticket Bus — architecture pro orientée terrain

Ce dépôt décrit une architecture de billettique bus moderne, avec un **focus fort sur la validation sécurisée** et le **mode offline indispensable** côté contrôle.

## Vision produit

Un système de transport fiable ne se limite pas à « scanner un QR code ».
Il combine :

- billettique (produits, wallet, pass)
- moteur tarifaire (best fare / capping)
- gestion budgétaire client
- contrôle anti-fraude embarqué
- exploitation temps réel et supervision

## Mode offline indispensable (contrôle embarqué)

Pour un usage réel terrain, l’application de contrôle doit continuer à fonctionner en cas de coupure réseau.

### Ce que le terminal de contrôle embarque

- la **clé publique** de vérification
- un **cache local** des tickets récemment vus
- un mécanisme de **blacklist synchronisée**
- une **file d’attente** des validations à remonter

### Fonctionnement en offline

Le terminal peut :

- vérifier la **signature** du ticket
- vérifier l’**expiration**
- détecter un **rejeu local simple**

### Quand le réseau revient

- synchronisation des événements de validation
- consolidation centrale
- détection avancée des doublons/collisions

## Anti-fraude minimale requise

### 1) Rejeu (même QR utilisé plusieurs fois)

**Mesures**

- nonce
- horodatage
- compteur d’usage
- verrou métier côté backend

### 2) Capture d’écran partagée

**Mesures**

- ticket lié à un compte
- fenêtre d’activation courte
- rotation visuelle du code
- contrôle d’identité si nécessaire

### 3) Double scan sur plusieurs bus

**Mesures**

- centralisation des `ValidationEvent`
- règles de collision temporelle
- alerte si même ticket validé à des endroits incompatibles

### 4) Faux tickets

**Mesures**

- signature asymétrique
- rotation des clés
- journalisation complète

## Stack technique cohérente

### Backend

- Java / Spring Boot
- Spring Security
- PostgreSQL
- Redis (cache, anti-rejeu, sessions techniques)
- Kafka (découplage des événements de validation)
- Keycloak (identité agents/admin)
- API Gateway

### Mobile (contrôle)

- Android natif ou cross-platform
- scan QR via ZXing ou ML Kit
- stockage local sécurisé
- synchronisation offline/online

### Back-office

- Angular
- Material
- dashboards exploitation / fraude / revenus

### Infrastructure

- Docker
- Kubernetes (si montée en charge)
- observabilité : logs, métriques, traces

## Roadmap de delivery

### MVP solide

- achat ticket unitaire
- wallet
- QR signé
- scan et validation
- back-office simple
- reporting minimal
- gestion expiration
- anti double usage basique

### Version 2

- carnets
- pass semaine / mois
- budget planner
- recommandations tarifaires
- mode offline robuste
- alertes fraude
- dashboard financier

### Version 3

- capping intelligent
- best fare engine
- multi-opérateurs
- multi-zones
- interopérabilité
- analytique avancée

## Architecture logique simplifiée

```text
App Client
   -> API Gateway
      -> Identity Service
      -> Payment Service
      -> Wallet Service
      -> Ticketing Service
      -> Pricing Service
      -> Validation Service
      -> Notification Service

App Contrôleur
   -> Validation API
   -> Mode offline local cache

Back Office Angular
   -> Admin API
```

## Stratégie d’industrialisation recommandée

### Phase 1 — noyau robuste

- QR signé
- validation fiable
- wallet
- produits simples

### Phase 2 — intelligence budgétaire

- recommandation selon budget
- carnet / pass
- plafonnement (capping)

### Phase 3 — exploitation pro

- offline résilient
- détection fraude avancée
- observabilité
- supervision opérationnelle

## Proposition métier concrète

Produits :

- ticket unitaire
- pack 10 trajets
- pass jour
- pass semaine
- pass mois
- wallet rechargeable
- budget cap journalier / hebdo / mensuel

Le moteur tarifaire choisit automatiquement le **meilleur coût** pour le client selon :

- son budget déclaré
- sa fréquence réelle
- ses validations accumulées

---

Ce document sert de base pour cadrer une implémentation « terrain » progressive, de la première mise en service jusqu’à une exploitation à grande échelle.
