from __future__ import annotations
import json
from pathlib import Path

SOURCE = Path("app/src/main/assets/Substances.json")
TARGET_DIR = Path("app/src/main/assets/substances/en_US")
CATEGORIES_FILENAME = "_categories.json"

def main():
    if not SOURCE.exists():
        raise FileNotFoundError(f"Missing source file at {SOURCE}")

    data = json.loads(SOURCE.read_text())
    categories = data.get("categories", [])
    substances = data.get("substances", [])

    TARGET_DIR.mkdir(parents=True, exist_ok=True)

    (TARGET_DIR / CATEGORIES_FILENAME).write_text(
        json.dumps(categories, indent=2, ensure_ascii=False),
        encoding="utf-8",
    )

    for substance in substances:
        name = substance.get("name")
        if not name:
            continue
        target = TARGET_DIR / f"{name}.json"
        target.write_text(json.dumps(substance, indent=2, ensure_ascii=False), encoding="utf-8")

if __name__ == "__main__":
    main()
