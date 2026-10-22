#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Scan the EuropePMC harvest and report which drugs have a sentence that
names a concrete metabolite (not just "is metabolised").

Usage: python scan-evidence.py [dir]
"""
import pathlib
import re
import sys

sys.stdout.reconfigure(encoding="utf-8", errors="replace")
D = pathlib.Path(sys.argv[1] if len(sys.argv) > 1 else "epmc-rc")

# a metabolite name looks like a chemical entity, not a generic phrase
NAMED = re.compile(
    r"(\b\d+-(amino|hydroxy|hydroxyl|nitro|oxo|dealkyl)\b"
    r"|\bnor-?[a-z]{3,}\b"
    r"|\b[a-z]+(?:yl)?(?:glucuronide|sulphate|sulfate)\b"
    r"|\bN-?oxide\b"
    r"|\bdesmethyl|\bdealkyl|\bmonomethyl|\bdihydro"
    r"|\bglucuronidation\b|\bsulphation\b|\bsulfation\b)",
    re.I,
)
GENERIC = re.compile(r"^(is|are|was|were|has|have)\b", re.I)

rows = []
for f in sorted(D.glob("*.txt")):
    drug = f.stem.replace("_", "-")
    text = f.read_text(encoding="utf-8", errors="replace")
    hits = 0
    best = ""
    for line in text.split("\n"):
        line = line.strip()
        if not line.startswith("- "):
            continue
        if NAMED.search(line) and drug.lower()[:6] in line.lower():
            hits += 1
            if len(line) > len(best):
                best = line
    if hits:
        rows.append((hits, drug, best))

rows.sort(reverse=True)
print(f"drugs with a named-metabolite sentence: {len(rows)} / {len(list(D.glob('*.txt')))}")
for hits, drug, best in rows:
    print(f"\n[{hits}] {drug}")
    print(f"    {best[:420]}")
