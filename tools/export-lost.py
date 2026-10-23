#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Export the fields where the rewrite dropped something, for hand repair.

Usage: python tools/export-lost.py <ref> [out-file]
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
out_file = sys.argv[2] if len(sys.argv) > 2 else "lost-fields.txt"

tmp = pathlib.Path(tempfile.mkdtemp())
arch = subprocess.run(["git", "archive", ref, "app/src/main/assets/substances"],
                      capture_output=True)
with tarfile.open(fileobj=io.BytesIO(arch.stdout)) as tf:
    tf.extractall(tmp)

changed = subprocess.run(
    ["git", "diff", "--name-only", ref, "HEAD", "--", "app/src/main/assets/substances"],
    capture_output=True, text=True, encoding="utf-8").stdout.splitlines()

out = []
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

    def check(field, old, new):
        if not isinstance(old, str) or not isinstance(new, str) or old == new:
            return
        miss = {}
        for kind, pat in PATS.items():
            a = {x.rstrip("%") for x in pat.findall(old)} if kind == "number" else set(pat.findall(old))
            b = {x.rstrip("%") for x in pat.findall(new)} if kind == "number" else set(pat.findall(new))
            m = a - b
            if m:
                miss[kind] = sorted(m)
        if miss:
            out.append(f"### {rel} :: {field}   {miss}")
            out.append(f"OLD: {old}")
            out.append(f"NEW: {new}")
            out.append("")

    for k in FIELDS:
        check(k, was.get(k), now.get(k))
    for k in ARRAYS:
        a, b = was.get(k), now.get(k)
        if isinstance(a, list) and isinstance(b, list):
            for i, (x, y) in enumerate(zip(a, b)):
                check(f"{k}[{i}]", x, y)

pathlib.Path(out_file).write_text("\n".join(out), encoding="utf-8")
print(f"wrote {out_file}: {len(out) // 4} fields")
