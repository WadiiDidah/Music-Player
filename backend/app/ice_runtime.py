from pathlib import Path

import Ice

ROOT = Path(__file__).resolve().parents[1]
SLICE_FILE = ROOT / "slice" / "MyInterface.ice"

Ice.loadSlice(str(SLICE_FILE))
import Example

__all__ = ["Example", "ROOT"]
