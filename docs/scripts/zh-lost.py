#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""List the zh_cn fields whose rewrite dropped a number or a drug name.

For each one, print the record, the field, and both texts, so the Chinese can
be brought back in line with the English by hand.

Usage: python docs/scripts/zh-lost.py [out-file]
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
OUT = sys.argv[1] if len(sys.argv) > 1 else "zh-lost.txt"

PATS = {
    "number": re.compile(r"\d+(?:[.,]\d+)?%?"),
    "drug": re.compile(r"[A-Za-z]{0,4}\d+[A-Za-z]*(?:[-/][A-Za-z0-9]+)*"),
}


def tokens(text, kind):
    found = set(PATS[kind].findall(text))
    if kind == "number":
        found = {x.rstrip("%") for x in found}
    return found


tmp = pathlib.Path(tempfile.mkdtemp())
arch = subprocess.run(["git", "archive", "origin/main", "app/src/main/assets/substances"],
                      capture_output=True)
with tarfile.open(fileobj=io.BytesIO(arch.stdout)) as tf:
    tf.extractall(tmp)

changed = subprocess.run(
    ["git", "diff", "--name-only", "origin/main", "HEAD", "--", "app/src/main/assets/substances/zh_cn"],
    capture_output=True, text=True, encoding="utf-8").stdout.splitlines()

# also include uncommitted changes
changed2 = subprocess.run(
    ["git", "diff", "--name-only", "HEAD", "--", "app/src/main/assets/substances/zh_cn"],
    capture_output=True, text=True, encoding="utf-8").stdout.splitlines()
changed += [c for c in changed2 if c not in changed]

rows = []
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
        for kind in PATS:
            m = tokens(old, kind) - tokens(new, kind)
            if m:
                miss[kind] = sorted(m)
        if miss:
            rows.append((rel, field, miss, old, new))

    for k, v in was.items():
        nv = now.get(k)
        if isinstance(v, str):
            check(k, v, nv)
        elif isinstance(v, list) and isinstance(nv, list):
            for i, (a, b) in enumerate(zip(v, nv)):
                check(f"{k}[{i}]", a, b)

lines = []
for rel, field, miss, old, new in rows:
    lines.append(f"@@@ {rel} :: {field}   {miss}")
    lines.append(f"OLD| {old}")
    lines.append(f"NEW| {new}")
    lines.append("")
pathlib.Path(OUT).write_text("\n".join(lines), encoding="utf-8")
print(f"wrote {OUT}: {len(rows)} fields")
