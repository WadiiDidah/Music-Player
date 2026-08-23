from pathlib import Path

import vlc


class MusicPlayer:
    def __init__(self):
        self.instance = vlc.Instance()
        self.player = self.instance.media_player_new()

    def play(self, path: Path) -> None:
        if not path.exists():
            raise FileNotFoundError(path)
        self.player.set_media(self.instance.media_new(str(path)))
        self.player.play()

    def pause(self) -> None:
        self.player.pause()

    def stop(self) -> None:
        self.player.stop()
