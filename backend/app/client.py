import sys

import Ice

from app.ice_runtime import Example, ROOT
from app.player import MusicPlayer


SERVERS = {
    "RAP": "RapServer:default -p 10000",
    "RNB": "RnbServer:default -p 10001",
    "ROCK": "RockServer:default -p 10002",
}


def proxy_for(communicator, genre: str):
    endpoint = SERVERS.get(genre.upper())

    if endpoint is None:
        raise ValueError(
            "Genre attendu : RAP, RNB ou ROCK."
        )

    proxy = Example.MyInterfacePrx.checkedCast(
        communicator.stringToProxy(endpoint)
    )

    if proxy is None:
        raise RuntimeError(
            "Impossible de joindre le serveur musical."
        )

    return proxy


def download(proxy, genre: str) -> None:
    title = input("Titre : ").strip()
    artist = input("Artiste : ").strip()

    if not title or not artist:
        print("Le titre et l'artiste sont obligatoires.")
        return

    data = proxy.addMusique(
        genre,
        title,
        artist
    )

    output = (
        ROOT
        / "downloads"
        / f"{artist}-{title}.mp3"
    )

    output.parent.mkdir(
        parents=True,
        exist_ok=True
    )

    output.write_bytes(data)

    print(
        f"Téléchargé : {output.name}"
    )


def list_tracks(proxy, genre: str) -> None:
    tracks = proxy.getMusiques(genre)

    if not tracks:
        print("Aucune musique disponible.")
        return

    print()

    for index, track in enumerate(
        tracks,
        start=1
    ):
        print(f"{index}. {track}")


def rename(proxy) -> None:
    old_title = input(
        "Titre actuel : "
    ).strip()

    new_title = input(
        "Nouveau titre : "
    ).strip()

    artist = input(
        "Artiste : "
    ).strip()

    if not old_title or not new_title or not artist:
        print("Tous les champs sont obligatoires.")
        return

    success = proxy.updateMusique(
        old_title,
        new_title,
        artist
    )

    if success:
        print("Musique renommée.")
    else:
        print("Musique introuvable.")


def delete(proxy) -> None:
    title = input(
        "Titre : "
    ).strip()

    artist = input(
        "Artiste : "
    ).strip()

    if not title or not artist:
        print("Le titre et l'artiste sont obligatoires.")
        return

    success = proxy.supprimerMusique(
        title,
        artist
    )

    if success:
        print("Musique supprimée.")
    else:
        print("Musique introuvable.")


def play_download(
    player: MusicPlayer
) -> None:

    filename = input(
        "Nom du fichier dans downloads : "
    ).strip()

    if not filename:
        print("Veuillez saisir un nom de fichier.")
        return

    path = (
        ROOT
        / "downloads"
        / filename
    )

    if not path.exists():
        print(
            f"Le fichier '{filename}' n'est pas présent "
            "dans le dossier downloads."
        )
        return

    player.play(path)

    print(
        f"Lecture de {filename}. "
        "Appuyez sur Entrée pour arrêter."
    )

    input()

    player.stop()


def handle_error(exc: Exception) -> None:

    if isinstance(exc, FileNotFoundError):
        print(
            "Erreur : fichier audio introuvable."
        )

    elif isinstance(
        exc,
        Ice.ConnectionRefusedException
    ):
        print(
            "Erreur : le serveur musical est indisponible."
        )

    elif isinstance(
        exc,
        Ice.ConnectionLostException
    ):
        print(
            "Erreur : connexion au serveur perdue."
        )

    elif isinstance(
        exc,
        Ice.TimeoutException
    ):
        print(
            "Erreur : le serveur met trop de temps à répondre."
        )

    elif isinstance(
        exc,
        Ice.MemoryLimitException
    ):
        print(
            "Erreur : le fichier audio est trop volumineux."
        )

    elif isinstance(
        exc,
        Ice.UnknownException
    ):
        print(
            "Erreur : le serveur n'a pas pu traiter la demande."
        )

    elif isinstance(
        exc,
        ValueError
    ):
        print(
            f"Erreur : {exc}"
        )

    else:
        print(
            "Erreur : une erreur inattendue est survenue."
        )


def main() -> None:
    player = MusicPlayer()

    properties = Ice.createProperties(
        sys.argv
    )

    properties.setProperty(
        "Ice.MessageSizeMax",
        "10240"
    )

    init_data = Ice.InitializationData()
    init_data.properties = properties

    try:

        with Ice.initialize(
            sys.argv,
            init_data
        ) as communicator:

            genre = input(
                "Genre (RAP/RNB/ROCK) : "
            ).strip().upper()

            proxy = proxy_for(
                communicator,
                genre
            )

            actions = {
                "1": lambda: list_tracks(
                    proxy,
                    genre
                ),
                "2": lambda: download(
                    proxy,
                    genre
                ),
                "3": lambda: rename(
                    proxy
                ),
                "4": lambda: delete(
                    proxy
                ),
                "5": lambda: play_download(
                    player
                ),
            }

            while True:

                print()
                print("1. Lister les musiques")
                print("2. Télécharger une musique")
                print("3. Renommer une musique")
                print("4. Supprimer une musique")
                print("5. Lire un téléchargement")
                print("6. Quitter")

                choice = input(
                    "Choix : "
                ).strip()

                if choice == "6":
                    player.stop()
                    print("Au revoir.")
                    break

                action = actions.get(choice)

                if action is None:
                    print("Choix invalide.")
                    continue

                try:
                    action()

                except Exception as exc:
                    handle_error(exc)

    except Exception as exc:
        handle_error(exc)


if __name__ == "__main__":
    main()