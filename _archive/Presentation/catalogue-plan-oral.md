# Catalogue : plan de l'oral (environ 1 min 40)

Quatre diapositives, 25 secondes chacune. Le texte à dire fait environ 60 mots : il se lit à voix naturelle en 25 secondes.
Pour les schémas, exporter les blocs Mermaid de `Documentation/Diagrams/` en image (par exemple sur mermaid.live) et les coller dans les diapositives.

---

## Diapositive 1 : rôle du Catalogue

**Sur la diapositive**

| Il gère | Il ne gère pas |
|---|---|
| Produits des artisans | Commandes (Commandes) |
| Catégories | Paiements (Paiement) |
| Prix | Livraison (Livraison) |
| Stock | Messages (Notifications) |

Une ligne en bas : *un service à part, car produit, catégorie et stock changent ensemble.*

**À dire (25 s)**

« Je m'occupe du Catalogue. Il gère les produits des artisans, leurs catégories, leurs prix et leur stock. Il ne gère ni les commandes, ni les paiements, ni la livraison : ce sont d'autres services. Je l'ai séparé parce que produit, catégorie et stock changent ensemble, et parce qu'il est très lu et doit monter en charge seul. »

---

## Diapositive 2 : modèle de données

**Sur la diapositive** : le schéma de `catalogue-modele-donnees.md` (Produit au centre) et une ligne : *MySQL : relationnel, contraintes, transactions ACID pour le stock.*

**À dire (25 s)**

« Le produit est au centre : il appartient à une catégorie et a un stock. J'ai aussi des images et des réservations de stock. J'ai choisi MySQL : les données sont liées, il y a des contraintes, et le stock demande des transactions ACID pour éviter de vendre deux fois le dernier produit. H2 sert seulement à faire démarrer le projet. »

---

## Diapositive 3 : API REST

**Sur la diapositive** : le tableau des 8 endpoints de la fiche (méthode, URI, codes de statut).

| Méthode | URI | Codes |
|---|---|---|
| POST | `/produits` | 201, 400, 401, 403 |
| GET | `/produits` | 200, 400 |
| GET | `/produits/{id}` | 200, 404 |
| PUT | `/produits/{id}` | 200, 400, 401, 403, 404 |
| DELETE | `/produits/{id}` | 204, 401, 403, 404 |
| PATCH | `/produits/{id}/stock` | 200, 400, 401, 403, 404, 409 |
| GET | `/categories` | 200 |
| POST | `/categories` | 201, 400, 401, 403, 409 |

**À dire (25 s)**

« Huit endpoints dans le périmètre : créer, lister, lire, modifier et retirer un produit, lister et créer une catégorie, et une route de stock. La route de stock réserve en une seule opération : si le stock est insuffisant, elle répond 409. Rejouer la même requête ne change rien. »

---

## Diapositive 4 : interactions et pannes

**Sur la diapositive** : le schéma de séquence de `catalogue-interactions.md`, et la phrase : *si X est en panne, alors…* (voir ci-dessous).

| Si… est en panne | Alors… |
|---|---|
| Le Catalogue | Commandes répond 503 : pas de risque de survente |
| Le service commun | Le Catalogue continue ; l'événement attend dans la file |
| Le broker de messages | Les modifications restent enregistrées ; la publication est retentée plus tard |

**À dire (25 s)**

« Commandes m'appelle en direct pour réserver le stock, car elle a besoin de la réponse. Si je suis arrêté, Commandes répond 503 au client : pas de risque de survente. Le reste passe par des événements : les produits vers la recherche, l'annulation de commande vers moi. Si un service est arrêté, l'événement attend dans la file. »

---

## Chronométrage

| Diapositive | Durée |
|---|---|
| 1. Rôle | 25 s |
| 2. Modèle de données | 25 s |
| 3. API REST | 25 s |
| 4. Interactions et pannes | 25 s |
| **Total** | **1 min 40** |

À répéter une fois à voix haute avec un minuteur : le texte est calibré pour tenir, mais seule une répétition le confirme.
