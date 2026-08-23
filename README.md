# Architecture Distribuée — Music Player

Projet de lecteur musical distribué combinant un backend Python basé sur ZeroC Ice et une application mobile Android.

L'objectif du projet est de mettre en pratique une architecture distribuée dans laquelle plusieurs services musicaux communiquent avec les clients à travers des appels distants.

Le système permet de gérer un catalogue musical réparti par genre et propose une interface mobile permettant à terme de contrôler le lecteur à l'aide de commandes vocales.

État actuel
→ Backend fonctionnel
→ Front Android fonctionnel
→ Connexion Android ↔ backend à finaliser

## Présentation

L'application repose sur deux composants principaux.

### Backend Python

Le backend utilise ZeroC Ice pour mettre en place une architecture client/serveur distribuée.

Trois services musicaux sont disponibles :

- RAP
- RNB
- ROCK

Chaque service expose les opérations permettant de consulter et gérer les morceaux associés à son genre.

Le client Python permet notamment de :

- Lister les morceaux disponibles
- Télécharger un morceau
- Renommer un morceau
- Supprimer un morceau
- Lire localement un morceau téléchargé

### Application Android

L'application Android constitue l'interface mobile du projet.

Elle propose une interface de lecteur musical avec :

- Affichage du morceau courant
- Contrôles de lecture
- Barre de progression
- Interface de commande vocale
- Enregistrement depuis le microphone
- Préparation de l'envoi des commandes vers le backend

## Architecture

```text
                 ┌──────────────────────┐
                 │ Application Android  │
                 │    Voice Player      │
                 └──────────┬───────────┘
                            │
                            │ HTTP / Audio
                            │
                 ┌──────────▼───────────┐
                 │    Backend Python    │
                 └──────────┬───────────┘
                            │
                       ZeroC Ice
                            │
             ┌──────────────┼──────────────┐
             │              │              │
             ▼              ▼              ▼
        RAP Service     RNB Service    ROCK Service
        Port 10000      Port 10001     Port 10002
             │              │              │
             └──────────────┼──────────────┘
                            ▼
                      Music Library