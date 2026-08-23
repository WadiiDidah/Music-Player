from dataclasses import dataclass


@dataclass(frozen=True)
class MusicCommand:
    action: str
    track: str


def parse_command(text: str) -> MusicCommand:
    normalized = text.strip().lower()
    actions = {
        "play": ("joue", "play", "commence", "lance"),
        "stop": ("stop", "arrête", "arrete"),
        "delete": ("supprime", "supprimer", "delete"),
    }

    for action, keywords in actions.items():
        for keyword in keywords:
            if normalized.startswith(keyword):
                track = normalized[len(keyword):].strip()
                return MusicCommand(action, track)

    return MusicCommand("unknown", "")
