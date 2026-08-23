from pathlib import Path
from typing import List, Optional


class MusicLibrary:

    SUPPORTED_GENRES = {"RAP", "RNB", "ROCK"}

    def __init__(self, root: Path):
        self.root = root
        self.root.mkdir(parents=True, exist_ok=True)

    def genre_directory(self, genre: str) -> Path:
        normalized = genre.strip().upper()

        if normalized not in self.SUPPORTED_GENRES:
            raise ValueError(
                f"Genre non supporté : {genre}"
            )

        directory = self.root / normalized
        directory.mkdir(parents=True, exist_ok=True)

        return directory

    def list_tracks(self, genre: str) -> List[str]:
        directory = self.genre_directory(genre)

        return sorted(
            path.name
            for path in directory.glob("*.mp3")
        )

    def read_track(
        self,
        genre: str,
        title: str,
        artist: str
    ) -> bytes:

        path = self._find_track(
            genre,
            title,
            artist
        )

        if path is None:
            raise FileNotFoundError(
                f"Musique introuvable : {artist}-{title}.mp3"
            )

        return path.read_bytes()

    def delete_track(
        self,
        genre: str,
        title: str,
        artist: str
    ) -> bool:

        path = self._find_track(
            genre,
            title,
            artist
        )

        if path is None:
            return False

        path.unlink()

        return True

    def rename_track(
        self,
        genre: str,
        old_title: str,
        new_title: str,
        artist: str
    ) -> bool:

        path = self._find_track(
            genre,
            old_title,
            artist
        )

        if path is None:
            return False

        new_path = path.with_name(
            f"{artist}-{new_title}.mp3"
        )

        path.rename(new_path)

        return True

    def _find_track(
        self,
        genre: str,
        title: str,
        artist: str
    ) -> Optional[Path]:

        directory = self.genre_directory(genre)

        expected_name = (
            f"{artist}-{title}.mp3"
        ).lower()

        for path in directory.glob("*.mp3"):

            if path.name.lower() == expected_name:
                return path

        return None