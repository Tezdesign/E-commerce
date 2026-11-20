# Catalogue : modèle de données

Fiche de conception : [fiche-conception.md](../catalogue/fiche-conception.md) (section 5).

Le Produit est au centre : il appartient à une Catégorie et possède un Stock, des images et des réservations de stock.
`EVENEMENT_SORTANT` est une table technique, hors des 5 entités métier.

```mermaid
erDiagram
    CATEGORIE ||--o{ PRODUIT : contient
    PRODUIT ||--|| STOCK : possede
    PRODUIT ||--o{ PRODUIT_IMAGE : illustre
    PRODUIT ||--o{ RESERVATION_STOCK : concerne

    CATEGORIE {
        bigint id PK
        varchar nom UK
        text description
    }
    PRODUIT {
        bigint id PK
        varchar nom
        text description
        decimal prix "10,3 et superieur a 0"
        varchar artisanId "sub Keycloak, sans cle etrangere"
        varchar statut "ACTIF ou MASQUE"
        bigint categorie_id FK
        datetime createdAt
        datetime updatedAt
    }
    PRODUIT_IMAGE {
        bigint id PK
        bigint produit_id FK
        varchar url
        int position
    }
    STOCK {
        bigint id PK
        bigint produit_id FK "unique"
        int quantiteDisponible "superieur ou egal a 0"
        int seuilAlerte
        bigint version "verrou optimiste"
    }
    RESERVATION_STOCK {
        bigint id PK
        varchar commandeId "vient de Commandes"
        bigint produit_id FK
        int quantite
        varchar statut "ACTIVE ou LIBEREE"
        datetime createdAt
    }
    EVENEMENT_SORTANT {
        bigint id PK
        varchar type
        json payload
        datetime createdAt
        datetime publieAt "vide = pas encore publie"
    }
```

## Contraintes à retenir

- `RESERVATION_STOCK` : unique sur (`commandeId`, `produit_id`). C'est ce qui rend la réservation et la libération **idempotentes**.
- `STOCK.quantiteDisponible` ne peut jamais être négative (contrainte CHECK).
- `PRODUIT.artisanId` n'a **pas** de clé étrangère : l'artisan appartient à Keycloak.
- `EVENEMENT_SORTANT` est écrite dans la **même transaction** que la modification du produit ou du stock.
