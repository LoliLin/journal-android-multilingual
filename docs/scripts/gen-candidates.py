#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Generate the emc candidate list straight from the repo gaps.

Only names that look like INN drug names are worth trying: a single
lowercase word, letters/digits/hyphen/parenthesis, no slang.
Research-chemical-only records are skipped -- no SmPC will exist for them.
"""
import json
import pathlib
import re

BASE = pathlib.Path("app/src/main/assets/substances/root")
OUT = pathlib.Path("drugs-next.txt")

PHARMA = {"antipsychotic", "antidepressant", "benzodiazepine", "barbiturate",
          "opioid", "anticonvulsant", "anxiolytic", "ssri", "snri", "maoi",
          "stimulant", "depressant", "antihistamine", "nootropic", "antidiabetic",
          "antihypertensive", "deliriant", "eugeroic", "habit-forming", "common",
          "antiepileptic", "sedative", "hypnotic", "anticholinergic", "dissociative",
          "narcotic", "psychostimulant", "anorexigen"}
# A plain INN-style name: one word, letters/digits/hyphen, optional salt suffix.
INN = re.compile(r"^[A-Za-z][A-Za-z0-9\-]{2,30}$")
PLACEHOLDER = ("has not yet been verified", "\u5c1a\u672a\u6838\u5b9e")

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
        continue  # already sourced
    cats = set(d.get("categories") or [])
    if not (cats & PHARMA):
        continue
    if "research-chemical" in cats or "tentative" in cats:
        continue
    name = p.stem
    if not INN.match(name):
        continue
    rows.append((name, ",".join(sorted(cats))))

OUT.write_text("\n".join(r[0] for r in rows) + "\n", encoding="utf-8")
print(f"candidates: {len(rows)}")
for n, c in rows[:200]:
    print(f"  {n:24} {c}")
