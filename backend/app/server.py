import sys

import Ice

from app.ice_runtime import Example, ROOT
from app.library import MusicLibrary


class MusicService(Example.MyInterface):

    def __init__(self, genre: str, library: MusicLibrary):
        self.genre = genre
        self.library = library

    def printMessage(self, message, current=None):
        print(message)

    def getMusiques(self, genre, current=None):
        return self.library.list_tracks(self.genre)

    def addMusique(self, genre, nom, auteur, current=None):
        return self.library.read_track(
            self.genre,
            nom,
            auteur
        )

    def supprimerMusique(self, nom, auteur, current=None):
        return self.library.delete_track(
            self.genre,
            nom,
            auteur
        )

    def updateMusique(
        self,
        oldNom,
        nvNom,
        auteur,
        current=None
    ):
        return self.library.rename_track(
            self.genre,
            oldNom,
            nvNom,
            auteur
        )

    def getFavoris(self, current=None):
        return self.library.list_tracks(self.genre)


def main() -> None:

    library = MusicLibrary(
        ROOT / "library"
    )

    properties = Ice.createProperties(
        sys.argv
    )

    properties.setProperty(
        "Ice.MessageSizeMax",
        "10240"
    )

    init_data = Ice.InitializationData()
    init_data.properties = properties

    with Ice.initialize(
        sys.argv,
        init_data
    ) as communicator:

        services = {
            "RAP": ("RapServer", 10000),
            "RNB": ("RnbServer", 10001),
            "ROCK": ("RockServer", 10002),
        }

        adapters = []

        for genre, (identity, port) in services.items():

            adapter = (
                communicator
                .createObjectAdapterWithEndpoints(
                    identity,
                    f"default -p {port}"
                )
            )

            service = MusicService(
                genre,
                library
            )

            adapter.add(
                service,
                communicator.stringToIdentity(
                    identity
                )
            )

            adapter.activate()
            adapters.append(adapter)

            print(
                f"{genre} disponible sur le port {port}"
            )

        print(
            "Serveur musical démarré. "
            "Ctrl+C pour arrêter."
        )

        communicator.waitForShutdown()


if __name__ == "__main__":
    main()