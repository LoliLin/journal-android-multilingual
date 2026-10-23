#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Generate the research-chemical / designer-drug gap list for literature work.

These have no SmPC, so the only route is peer-reviewed literature. Names that
look like a plain INN or a designer analogue are worth searching.
"""
import json
import pathlib
import re

BASE = pathlib.Path("app/src/main/assets/substances/root")
OUT = pathlib.Path("drugs-rc.txt")
PLACEHOLDER = ("has not yet been verified", "\u5c1a\u672a\u6838\u5b9e")
# plain name, optionally with a salt/form suffix
NAME = re.compile(r"^[A-Za-z][A-Za-z0-9\-]{2,40}$")
SKIP = {"Alcohol", "Nicotine", "Cannabis", "Cocaine", "Heroin", "Opium",
        "Caffeine", "Aspirin", "Ibuprofen", "Paracetamol", "Psilocybin", "LSD"}

rows = []
for p in sorted(BASE.glob("*.json")):
    try:
        d = json.loads(p.read_bytes().decode("utf-8-sig"))
    except Exception:
        continue
    if not isinstance(d, dict):
        continue
    m = d.get("metabolism") or ""
    if m and not any(x in m for x in PLACEHOLDER):
        continue
    cats = set(d.get("categories") or [])
    if not (cats & {"research-chemical", "tentative"}):
        continue
    name = p.stem
    if not NAME.match(name) or name in SKIP:
        continue
    rows.append(name)

OUT.write_text("\n".join(rows) + "\n", encoding="utf-8")
print(f"rc candidates: {len(rows)}")
