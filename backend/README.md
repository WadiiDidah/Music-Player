# Distributed-Music-Player

Projet Python distribué de gestion et de lecture musicale basé sur ZeroC Ice.

Le projet permet à un client de communiquer avec plusieurs services musicaux distants, organisés par genre, afin de consulter, télécharger, renommer et supprimer des morceaux.

## Fonctionnalités

- Architecture client / serveur avec ZeroC Ice
- Services séparés pour RAP, RNB et ROCK
- Consultation du catalogue musical distant
- Téléchargement d'un morceau depuis le serveur
- Renommage d'un morceau
- Suppression d'un morceau
- Lecture locale avec VLC
- Analyse simple de commandes en langage naturel
- Petite API Flask pour tester l'analyse des commandes
- Ancienne configuration IceStorm conservée dans `legacy/`

## Historique du projet

Ce projet a été réalisé initialement en 2023 dans le cadre de mon apprentissage
des architectures distribuées avec Python et ZeroC Ice.

Il a ensuite été repris et amélioré afin de moderniser sa structure et de le
rendre plus propre et plus simple à exécuter.

Les principales améliorations apportées lors de cette refonte comprennent :

- réorganisation de l'architecture du projet
- nettoyage et simplification du code Python
- amélioration de la communication client / serveur
- séparation des services musicaux par genre
- amélioration de la gestion des erreurs
- configuration de la taille des messages ZeroC Ice pour le transfert des fichiers audio
- amélioration du téléchargement et de la lecture locale des morceaux
- ajout d'une documentation complète pour l'installation et l'exécution
- conservation de l'ancienne expérimentation IceStorm dans `legacy/`

Cette version constitue donc une évolution du projet original de 2023.

## Architecture

```text
Client CLI
   |
   | ZeroC Ice
   v
Music Server
   |
   +--> RAP : port 10000
   +--> RNB : port 10001
   +--> ROCK : port 10002
   |
   v
MusicLibrary
   |
   v
library/
```

## Structure

```text
Distributed-Music-Player/
├── app/
│   ├── client.py
│   ├── server.py
│   ├── library.py
│   ├── player.py
│   ├── command_parser.py
│   ├── web_api.py
│   └── ice_runtime.py
├── slice/
│   └── MyInterface.ice
├── library/
│   ├── RAP/
│   ├── RNB/
│   └── ROCK/
├── downloads/
├── legacy/
│   └── icestorm/
├── requirements.txt
└── README.md
```

## Technologies

- Python 3
- ZeroC Ice 3.7
- Slice / RPC
- python-vlc
- Flask

## Installation

Créer un environnement virtuel :

```bash
python3 -m venv .venv
source .venv/bin/activate
```

Installer les dépendances :

```bash
pip install -r requirements.txt
```

VLC doit également être installé sur la machine pour utiliser la lecture audio.

Sur macOS :

```bash
brew install --cask vlc
```

## Lancer le serveur

Depuis la racine du projet :

```bash
python3 -m app.server
```

Le serveur expose :

```text
RAP  -> localhost:10000
RNB  -> localhost:10001
ROCK -> localhost:10002
```

## Lancer le client

Dans un second terminal :

```bash
source .venv/bin/activate
python3 -m app.client
```

Choisir ensuite un genre puis une action dans le menu.

## API de commandes

Lancer :

```bash
python3 -m app.web_api
```

Exemple :

```bash
curl -X POST http://localhost:5000/commands \
  -H "Content-Type: application/json" \
  -d '{"command":"lance Anas Monalisa"}'
```

Réponse :

```json
{
  "action": "play",
  "track": "anas monalisa"
}
```

## IceStorm

La version historique du projet comportait également une démonstration publish / subscribe avec IceStorm. Les fichiers Slice et les configurations d'origine utiles ont été conservés dans `legacy/icestorm` afin de montrer cette partie de l'architecture sans mélanger le code principal.

## Améliorations futures

- Interface graphique moderne pour le lecteur
- Gestion persistante des favoris
- Recherche par titre et artiste
- Authentification des utilisateurs
- Streaming audio par blocs plutôt que transfert complet du fichier
- Conteneurisation des services
- Remplacement du parser de commandes par un traitement du langage naturel plus avancé
