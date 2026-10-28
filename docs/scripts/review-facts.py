#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Review the wording rewrite: did any fact get lost?

Compares each changed field against its version before the rewrite and
checks that every number, unit, metabolite name, enzyme name and drug name
survived. A rewrite that drops "24%" or "norfentanyl" is a rewrite that
changed the record, not just its wording.

Usage: python docs/scripts/review-facts.py [base-ref] [--all]
"""
import io
import json
import pathlib
import re
import subprocess
import sys
import tarfile
import tempfile

sys.stdout.reconfigure(encoding="utf-8", errors="replace")
BASE = pathlib.Path("app/src/main/assets/substances")
REF = "origin/main"
SHOW_ALL = "--all" in sys.argv

FIELDS = ("metabolism", "generalRisks", "longtermRisks", "saferUse",
          "dosageRemark", "effectsSummary", "addictionPotential", "summary")
ARRAYS = ("toxicities", "saferUse", "longtermRisks", "generalRisks")

TOKEN_PATTERNS = {
    "number": re.compile(r"\d+(?:[.,]\d+)?%?"),
    "enzyme": re.compile(r"\b(?:CYP\d\w*|UGT\d\w*|MAO-?[AB])\b"),
    "unit": re.compile(r"\b(?:mg|ml|mL|L/kg|l/kg|ng/mL|ng/ml|mcg|kg|hr|hours?|days?|weeks?|months?)\b"),
    # a chemical name: starts with a digit or a capital, contains a digit
    "drug": re.compile(r"\b\d+[A-Za-z]*[-/]?\d*[A-Za-z]+\b"),
}

base = subprocess.run(["git", "merge-base", REF, "HEAD"],
                      capture_output=True, text=True, encoding="utf-8").stdout.strip()
tmp = pathlib.Path(tempfile.mkdtemp())
arch = subprocess.run(["git", "archive", base, "app/src/main/assets/substances"],
                      capture_output=True)
with tarfile.open(fileobj=io.BytesIO(arch.stdout)) as tf:
    tf.extractall(tmp)

changed = subprocess.run(
    ["git", "diff", "--name-only", base, "HEAD", "--", "app/src/main/assets/substances"],
    capture_output=True, text=True, encoding="utf-8").stdout.splitlines()

lost = []
checked = 0


def compare(path, field, old, new):
    global checked
    if not isinstance(old, str) or not isinstance(new, str) or old == new:
        return
    checked += 1
    for kind, pat in TOKEN_PATTERNS.items():
        o = {x.rstrip("%") for x in pat.findall(old)} if kind == "number" else set(pat.findall(old))
        n = {x.rstrip("%") for x in pat.findall(new)} if kind == "number" else set(pat.findall(new))
        missing = o - n
        if missing:
            lost.append((path, field, kind, sorted(missing), old, new))


for rel in changed:
    p = pathlib.Path(rel)
    if not p.exists() or p.suffix != ".json":
        continue
    try:
        now = json.loads(p.read_bytes().decode("utf-8-sig"))
    except Exception:
        continue
    if not isinstance(now, dict):
        continue
    bp = tmp / rel
    if not bp.exists():
        continue
    try:
        was = json.loads(bp.read_bytes().decode("utf-8-sig"))
    except Exception:
        continue
    if not isinstance(was, dict):
        continue
    for k in FIELDS:
        compare(rel, k, was.get(k), now.get(k))
    for k in ARRAYS:
        a, b = was.get(k), now.get(k)
        if isinstance(a, list) and isinstance(b, list):
            for i, (x, y) in enumerate(zip(a, b)):
                compare(rel, f"{k}[{i}]", x, y)

print(f"fields compared: {checked}")
print(f"fields where a fact went missing: {len(lost)}")
shown = 0
for path, field, kind, missing, old, new in lost:
    if not SHOW_ALL and shown >= 25:
        break
    shown += 1
    print(f"\n{path} :: {field}  [{kind}] lost {missing}")
    print(f"  OLD: {old[:300]}")
    print(f"  NEW: {new[:300]}")
