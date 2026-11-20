# Architecture cible

Critère E4. Détail des composants, des interactions (I-1 à I-9) et des points à confirmer :
[E3-E4-donnees-communication-architecture.md](../E3-E4-donnees-communication-architecture.md).
Version dessinée (pour la présentation) : document Docs « E3 et E4 — Données, communication et architecture », section 4.

```mermaid
flowchart LR
    FE["Front-end<br/>application Web"] --> GW["API Gateway<br/>point d'entrée unique"]
    GW <--> KC["Keycloak<br/>jetons et rôles"]
    GW --> MS
    EUR["Eureka<br/>registre de services"] -.->|enregistrement| MS
    CFG["Config Server<br/>configuration"] -.->|configuration| MS
    BR["Broker de messages<br/>RabbitMQ, à confirmer"]

    subgraph MS["Microservices Spring Boot"]
        AVIS["Avis<br/>MySQL"]
        CMD["Commandes<br/>MySQL"]
        CAT["Catalogue<br/>MySQL"]
        PAY["Paiement<br/>MySQL"]
        LIV["Livraison<br/>MySQL"]
        NOT["Notifications<br/>MySQL"]
        CMN["Service commun<br/>MongoDB ou PostgreSQL"]
    end

    AVIS -->|I-3 synchrone| CMD
    CMD -->|I-1 synchrone| CAT
    CMD -->|I-2 synchrone| PAY

    CMD <-->|I-4 I-5 I-6 I-8| BR
    CAT <-->|I-7 I-8 I-9| BR
    PAY -->|I-6| BR
    LIV <-->|I-4 I-5 I-6| BR
    BR -->|I-6 I-9| NOT
    BR -->|I-7| CMN
```

**Lecture.** Trait plein entre services : appel synchrone (OpenFeign). Flèche vers le broker : le service publie un événement. Flèche depuis le broker : il le consomme. Tout tourne dans Docker Compose, avec un conteneur par composant.
