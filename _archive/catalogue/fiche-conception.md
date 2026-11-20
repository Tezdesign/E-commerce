# Microservice Catalogue : fiche de conception

Mini-projet AWD, validation 1. Critères visés : **I1** (conception) et **I2** (interactions).
Plateforme e-commerce pour artisans tunisiens. Service : `catalogue-service`, port 8081.

Les schémas sont dans `Documentation/Diagrams/` :
[modèle de données](../Diagrams/catalogue-modele-donnees.md) et
[interactions](../Diagrams/catalogue-interactions.md).

> Les points marqués **« à confirmer en équipe »** concernent d'autres services : ce sont des propositions, pas des décisions prises.

---

## 1. Rôle du service

Le Catalogue publie et tient à jour **l'offre des artisans** : produits, catégories, prix et stock.

- **Contexte borné :** le Catalogue a son propre vocabulaire (fiche produit, catégorie, prix, disponibilité, réservation de stock).
- **Un seul motif de changement :** l'offre de l'artisan. Si on change la façon de payer ou de livrer, le Catalogue ne change pas.
- **Sa propre base :** une base MySQL que personne d'autre ne lit.

## 2. Ce qu'il gère et ce qu'il ne gère pas

| Le Catalogue gère | Il ne gère pas | Service responsable |
|---|---|---|
| Produits des artisans (nom, description, prix, images) | Paniers et commandes | Commandes |
| Catégories | Paiements | Paiement |
| Stock et réservations de stock | Livraison | Livraison |
| Statut d'un produit (ACTIF ou MASQUÉ) | Messages e-mail et SMS | Notifications |
| | Notes et commentaires des clients | Avis |
| | Recherche avancée, recommandations | Service commun |
| | Comptes et identité des artisans | Keycloak (le Catalogue garde seulement `artisanId`) |

## 3. Pourquoi un service à part, et pas un service par table

- **Produit, Catégorie et Stock changent ensemble** et parlent le même langage. Quand un produit est vendu, son stock baisse : séparer ces trois tables en trois services rendrait chaque vente dépendante de plusieurs appels réseau et casserait la cohérence.
- **Un service à part** parce que le Catalogue est très lu (navigation, recherche) et doit pouvoir monter en charge sans toucher aux commandes ni aux paiements.
- **Le découpage suit une capacité métier** (publier l'offre), pas une table ni un écran.

## 4. Cas d'utilisation couverts

| Code | Acteur | Cas d'utilisation |
|---|---|---|
| C2 | Client | Parcourir et rechercher des produits (avec le service commun) |
| C3 | Client | Consulter la fiche d'un produit |
| A1 | Artisan | Publier un produit |
| A2 | Artisan | Modifier ou retirer un produit |
| A3 | Artisan | Gérer son stock |
| D2 | Administrateur | Gérer les catégories de produits |

## 5. Modèle de données

**5 entités métier.** Schéma : [catalogue-modele-donnees.md](../Diagrams/catalogue-modele-donnees.md).

| Entité | Attributs (types) | Contraintes | Relations |
|---|---|---|---|
| **Produit** | id (bigint), nom (varchar 150), description (texte), prix (decimal 10,3), artisanId (varchar), statut (ACTIF ou MASQUÉ), createdAt, updatedAt | `nom` non vide ; `prix` **> 0** ; `artisanId` obligatoire, sans clé étrangère | appartient à **une** Catégorie ; a **un** Stock ; a plusieurs Images et Réservations |
| **ProduitImage** | id, produit_id, url (varchar), position (int) | une image appartient à un seul produit | plusieurs images par Produit |
| **Catégorie** | id, nom (varchar), description | `nom` **unique** ; liste plate, sans hiérarchie | contient plusieurs Produits |
| **Stock** | id, produit_id, quantiteDisponible (int), seuilAlerte (int), version (bigint) | un seul Stock par Produit ; `quantiteDisponible` **≥ 0** (CHECK) | un Stock par Produit |
| **ReservationStock** | id, commandeId (varchar), produit_id, quantite (int), statut (ACTIVE ou LIBÉRÉE), createdAt | `quantite` > 0 ; **unique** sur (commandeId, produit_id) | plusieurs Réservations par Produit |

- `artisanId` est l'identifiant de l'utilisateur dans Keycloak : le Catalogue ne possède pas les données de l'artisan.
- `commandeId` est l'identifiant donné par Commandes : le Catalogue ne connaît pas le contenu de la commande.
- `version` sert à éviter que deux modifications simultanées du stock s'écrasent.
- **Pourquoi ReservationStock :** elle permet de **libérer le stock une seule fois** quand une commande est annulée, même si le message arrive deux fois.

**Table technique (hors des 5 entités métier) : `EvenementSortant`.** Elle garde les événements à publier dans la même transaction que la modification, pour qu'ils partent quand même si le broker de messages est arrêté.

## 6. Choix de la base : MySQL

1. **Données relationnelles :** produit, catégorie et stock sont liés entre eux.
2. **Contraintes d'intégrité :** clés étrangères, unicité du nom de catégorie, quantité jamais négative.
3. **Transactions ACID pour le stock :** deux clients qui achètent le dernier exemplaire en même temps ne doivent pas tous les deux réussir (éviter la **survente**).

**Une base par service :** aucun autre service n'accède à la base du Catalogue. Les autres reçoivent seulement des identifiants (`produitId`) par API ou par événements.

**H2 en mémoire** sert uniquement à **faire démarrer le projet** à la validation 1, sans installer MySQL. Ses données disparaissent au redémarrage : ce n'est pas le choix final.

## 7. API REST

Documentée avec Swagger (springdoc-openapi). Préfixes `/produits` et `/categories`, sous-ressource `/produits/{id}/stock`.

### 7.1 Dans le périmètre de la validation 1 : 8 endpoints

| # | Méthode | URI | Rôle | Accès | Codes de statut |
|---|---|---|---|---|---|
| 1 | POST | `/produits` | Créer un produit (avec son stock initial) | ARTISAN | 201, 400, 401, 403 |
| 2 | GET | `/produits` | Lister (pagination, filtres) | public | 200, 400 |
| 3 | GET | `/produits/{id}` | Détail d'un produit avec son stock | public | 200, 404 |
| 4 | PUT | `/produits/{id}` | Modifier un produit (y compris son statut) | ARTISAN propriétaire | 200, 400, 401, 403, 404 |
| 5 | DELETE | `/produits/{id}` | Retirer un produit : il passe en MASQUÉ (suppression logique) | ARTISAN propriétaire, ADMIN | 204, 401, 403, 404 |
| 6 | PATCH | `/produits/{id}/stock` | Ajuster, réserver ou libérer du stock | selon l'opération (voir 7.4) | 200, 400, 401, 403, 404, 409 |
| 7 | GET | `/categories` | Lister les catégories | public | 200 |
| 8 | POST | `/categories` | Créer une catégorie | ADMIN | 201, 400, 401, 403, 409 |

### 7.2 Reportés après la validation

| # | Méthode | URI | Rôle | Accès | Codes de statut |
|---|---|---|---|---|---|
| 9 | PUT | `/categories/{id}` | Modifier une catégorie | ADMIN | 200, 400, 401, 403, 404, 409 |
| 10 | DELETE | `/categories/{id}` | Supprimer une catégorie | ADMIN | 204, 401, 403, 404, 409 (elle contient encore des produits) |

Ces deux endpoints complètent le cas d'utilisation D2 (gérer les catégories).

### 7.3 Détails

- **`GET /produits`** : paramètres `page` et `size` (défaut 0 et 20), `categorieId`, `statut` (défaut ACTIF) et `artisanId` (pour que l'artisan retrouve **ses** produits, cas A1 à A3). Un visiteur ne voit que les produits **ACTIFS**. La réponse est une page : `content`, `totalElements`, `totalPages`.
- **Codes utilisés :** 201 création, 200 lecture ou modification, 204 suppression, 400 donnée invalide, 401 sans jeton, 403 sans droit, 404 introuvable, 409 conflit (stock insuffisant, nom de catégorie déjà pris).
- **Corps d'erreur uniforme** pour 400, 404 et 409 : `timestamp`, `status`, `message`, `details`.

### 7.4 `PATCH /produits/{id}/stock` : trois opérations

Corps de la requête : `{ "operation": "AJUSTER | RESERVER | LIBERER", "quantite": 3, "commandeId": "CMD-102" }`. Réponse 200 : `produitId` et `quantiteDisponible`.

| Opération | Qui l'appelle | Effet | `commandeId` |
|---|---|---|---|
| `AJUSTER` | ARTISAN propriétaire | Ajoute `quantite` au stock (peut être négatif). Le résultat doit rester ≥ 0, sinon 409 | non |
| `RESERVER` | service Commandes | Retire `quantite` du stock et crée une réservation ACTIVE. 409 si le stock est insuffisant | obligatoire |
| `LIBERER` | service Commandes | Remet la quantité si la réservation est ACTIVE, puis la passe en LIBÉRÉE | obligatoire |

**Anti-survente.** La vérification et la réservation sont **une seule opération atomique** : une seule instruction SQL qui retire la quantité **seulement si** le stock est suffisant. Si rien n'est modifié, la réponse est 409.

```sql
UPDATE stock
   SET quantite_disponible = quantite_disponible - :q, version = version + 1
 WHERE produit_id = :id AND quantite_disponible >= :q;
```

**Idempotence.** Rejouer la même requête ne change rien : une réservation déjà faite avec le même `commandeId` ne retire rien de plus, et une réservation déjà libérée n'est pas libérée une seconde fois. Cela protège contre les nouveaux essais automatiques et contre un message reçu en double.

## 8. Rôles et accès

| Rôle | Droits |
|---|---|
| Public, CLIENT | Lire les produits ACTIFS et les catégories |
| ARTISAN propriétaire | Créer ses produits ; modifier, masquer et gérer le stock de **ses** produits seulement (sinon 403) |
| ADMIN | Gérer les catégories ; retirer n'importe quel produit |
| Service Commandes | `RESERVER` et `LIBERER` le stock *(à confirmer en équipe : compte de service Keycloak)* |

`artisanId` vient du **jeton Keycloak** (`sub`), **jamais du corps de la requête** : sinon un artisan pourrait créer ou modifier un produit au nom d'un autre.

## 9. Interactions (I2)

Tableau, cas de panne et schéma : [catalogue-interactions.md](../Diagrams/catalogue-interactions.md).

## 10. Check-list

- [x] Responsabilités et exclusions claires
- [x] 5 entités métier maximum, relations et contraintes précisées
- [x] MySQL justifié, H2 présenté comme solution de démarrage
- [x] 8 endpoints dans le périmètre (2 reportés), codes de statut précisés
- [x] Aucun autre service n'accède à la base du Catalogue

## 11. Points à confirmer en équipe ou avec l'enseignant

| Sujet | Avec qui | Proposition |
|---|---|---|
| Annulation des commandes impayées | Commandes | Commandes annule après un délai et publie `CommandeAnnulée` : c'est ce qui libère le stock bloqué |
| Compte de service pour `RESERVER` et `LIBERER` | Keycloak (groupe) | Un rôle dédié au service Commandes |
| Événement `StockBas` (I-9) | Notifications | L'ajouter au tableau des interactions de l'équipe ; sinon retirer le seuil d'alerte |
| Choix du broker | Groupe | RabbitMQ ou Kafka, selon le cours de la séance 7 ; le contenu des événements ne dépend pas de ce choix |
| Nombre d'endpoints | Enseignant | 8 dans le périmètre, 2 reportés |
