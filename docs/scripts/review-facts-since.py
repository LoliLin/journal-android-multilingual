#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Review only what the last commits changed.

Usage: python docs/scripts/review-facts-since.py <ref>
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

FIELDS = ("metabolism", "generalRisks", "longtermRisks", "saferUse",
          "dosageRemark", "effectsSummary", "addictionPotential", "summary")
ARRAYS = ("toxicities", "saferUse", "longtermRisks", "generalRisks")

PATS = {
    "number": re.compile(r"\d+(?:[.,]\d+)?%?"),
    "enzyme": re.compile(r"\b(?:CYP\d\w*|UGT\d\w*|MAO-?[AB])\b"),
    "unit": re.compile(r"\b(?:mg|ml|mL|L/kg|l/kg|ng/mL|ng/ml|mcg|kg|hr|hours?|"
                       r"days?|weeks?|months?)\b"),
    "drug": re.compile(r"\b\d+[A-Za-z]*[-/]?\d*[A-Za-z]+\b"),
}

ref = sys.argv[1]
tmp = pathlib.Path(tempfile.mkdtemp())
arch = subprocess.run(["git", "archive", ref, "app/src/main/assets/substances"],
                      capture_output=True)
with tarfile.open(fileobj=io.BytesIO(arch.stdout)) as tf:
    tf.extractall(tmp)

changed = subprocess.run(
    ["git", "diff", "--name-only", ref, "HEAD", "--", "app/src/main/assets/substances"],
    capture_output=True, text=True, encoding="utf-8").stdout.splitlines()

n = 0
for rel in changed:
    p = pathlib.Path(rel)
    if not p.exists() or p.suffix != ".json":
        continue
    try:
        now = json.loads(p.read_bytes().decode("utf-8-sig"))
    except Exception:
        continue
    bp = tmp / rel
    if not bp.exists():
        continue
    try:
        was = json.loads(bp.read_bytes().decode("utf-8-sig"))
    except Exception:
        continue
    if not isinstance(now, dict) or not isinstance(was, dict):
        continue
    name = rel.split("/")[-1]
    for k in FIELDS:
        o, nw = was.get(k), now.get(k)
        if not isinstance(o, str) or not isinstance(nw, str) or o == nw:
            continue
        for kind, pat in PATS.items():
            a = {x.rstrip("%") for x in pat.findall(o)} if kind == "number" else set(pat.findall(o))
            b = {x.rstrip("%") for x in pat.findall(nw)} if kind == "number" else set(pat.findall(nw))
            miss = a - b
            if miss:
                n += 1
                print(f"  {name} :: {k}  [{kind}] lost {sorted(miss)}")
    for k in ARRAYS:
        a, b = was.get(k), now.get(k)
        if not (isinstance(a, list) and isinstance(b, list)):
            continue
        for i, (x, y) in enumerate(zip(a, b)):
            if not isinstance(x, str) or not isinstance(y, str) or x == y:
                continue
            for kind, pat in PATS.items():
                s1 = {z.rstrip("%") for z in pat.findall(x)} if kind == "number" else set(pat.findall(x))
                s2 = {z.rstrip("%") for z in pat.findall(y)} if kind == "number" else set(pat.findall(y))
                miss = s1 - s2
                if miss:
                    n += 1
                    print(f"  {name} :: {k}[{i}]  [{kind}] lost {sorted(miss)}")

print(f"\nfields where a fact went missing since {ref}: {n}")
