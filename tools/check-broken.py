#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Find the sentences that the phrase substitutions broke.

Replacing a noun with a clause does not always fit the sentence it lands
in. "the volume of distribution is 3.3-6.8 l/kg" became "the how much it
spreads through the body is 3.3-6.8 l/kg", and "clearance of a 50 mg
oral dose" became "the body clears it at a 50 mg oral dose".

This looks for the shapes that come out of such a substitution, so they
can be rewritten by hand.

Usage: python tools/check-broken.py
"""
import json
import pathlib
import re
import sys

sys.stdout.reconfigure(encoding="utf-8", errors="replace")
BASE = pathlib.Path("app/src/main/assets/substances")

# A determiner or adverb directly in front of a clause.
BROKEN = [
    (r"\b(?:significant|considerable|marked|extensive|some|minor|"
     r"presystemic|pre-systemic|systemic|first-pass|first pass|"
     r"substantial)\s+the liver breaking some down on the way\b",
     "an adjective in front of the clause"),
    (r"\b(?:significant|considerable|marked|extensive|some|minor)\s+"
     r"how much (?:it|the drug) spreads through the body\b",
     "an adjective in front of the clause"),
    (r"\b(?:significant|considerable|marked|extensive)\s+"
     r"how fast the body clears it\b",
     "an adjective in front of the clause"),
    (r"\b(?:significant|considerable|marked|extensive)\s+"
     r"how much of the dose reaches the blood\b",
     "an adjective in front of the clause"),
    (r"\bthe how much it spreads through the body\b",
     "a determiner in front of the clause"),
    (r"\bthe how fast the body clears it\b",
     "a determiner in front of the clause"),
    (r"\bthe how much of the dose reaches the blood\b",
     "a determiner in front of the clause"),
    (r"\bthe time in the body of\b", "a noun phrase in the wrong place"),
    (r"\bthe body clears it at a\b", "a clause with a prepositional tail"),
    (r"\bcreatinine how fast the body clears it\b",
     "a clause inside a noun phrase"),
    (r"\bhow much of the dose reaches the blood of\b",
     "a clause with a prepositional tail"),
    (r"\bhalf-lives\b", "an awkward plural"),
    (r"\bthe half-life of\b", "an awkward phrase"),
]

FIELDS = ("metabolism", "generalRisks", "longtermRisks", "saferUse",
          "dosageRemark", "effectsSummary", "addictionPotential", "summary")
ARRAYS = ("toxicities", "saferUse", "longtermRisks", "generalRisks")

out = []
for p in sorted(BASE.rglob("*.json")):
    try:
        d = json.loads(p.read_bytes().decode("utf-8-sig"))
    except Exception:
        continue
    if not isinstance(d, dict):
        continue
    for k in FIELDS:
        v = d.get(k)
        if not isinstance(v, str):
            continue
        for pat, why in BROKEN:
            if re.search(pat, v, re.I):
                out.append(f"{p.relative_to(BASE)} :: {k}  -- {why}")
                out.append(f"  {v[:280]}")
                out.append("")
                break
    for k in ARRAYS:
        v = d.get(k)
        if not isinstance(v, list):
            continue
        for item in v:
            if not isinstance(item, str):
                continue
            for pat, why in BROKEN:
                if re.search(pat, item, re.I):
                    out.append(f"{p.relative_to(BASE)} :: {k}[]  -- {why}")
                    out.append(f"  {item[:280]}")
                    out.append("")
                    break

print(f"fields with a broken substitution: {len(out) // 3}")
for line in out:
    print(line)
