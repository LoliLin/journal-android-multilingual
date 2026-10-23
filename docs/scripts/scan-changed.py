#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Scan only the substance records this branch actually changed.

Fetches the baseline tree once (git archive) instead of calling git show per
file, then reports self-referential boilerplate and jargon in the fields
whose value differs from that baseline.

Usage: python scan-changed.py [base-ref]
"""
import json
import pathlib
import re
import subprocess
import sys
import tarfile
import tempfile

sys.stdout.reconfigure(encoding="utf-8", errors="replace")
BASE = pathlib.Path("app/src/main/assets/substances")
REF = sys.argv[1] if len(sys.argv) > 1 else "origin/main"

sys.path.insert(0, str(pathlib.Path("tools").resolve()))
from scan_jargon import boiler_in, JARGON  # noqa: E402


def walk(v, path, out):
    if isinstance(v, str):
        out.append((path, v))
    elif isinstance(v, dict):
        for k, x in v.items():
            walk(x, f"{path}.{k}", out)
    elif isinstance(v, list):
        for i, x in enumerate(v):
            walk(x, f"{path}[{i}]", out)


changed = subprocess.run(
    ["git", "diff", "--name-only", f"{REF}..HEAD", "--", "app/src/main/assets/substances"],
    capture_output=True, text=True, encoding="utf-8").stdout.splitlines()

# baseline: extract the tree once
tmp = pathlib.Path(tempfile.mkdtemp())
arch = subprocess.run(["git", "archive", REF, "app/src/main/assets/substances"],
                      capture_output=True)
with tarfile.open(fileobj=__import__("io").BytesIO(arch.stdout)) as tf:
    tf.extractall(tmp)

rows = []
for rel in changed:
    p = pathlib.Path(rel)
    if not p.exists() or p.suffix != ".json":
        continue
    try:
        after = json.loads(p.read_bytes().decode("utf-8-sig"))
    except Exception:
        continue
    if not isinstance(after, dict):
        continue
    bp = tmp / rel
    if bp.exists():
        try:
            before = json.loads(bp.read_bytes().decode("utf-8-sig"))
        except Exception:
            before = {}
    else:
        before = {}
    if not isinstance(before, dict):
        before = {}

    a_s, b_s = [], []
    walk(after, "$", a_s)
    walk(before, "$", b_s)
    b_map = dict(b_s)
    for path, s in a_s:
        if path in b_map and b_map[path] == s:
            continue
        low = s.lower()
        hits_b = boiler_in(s)
        hits_j = sorted({t for t in JARGON if re.search(rf"\b{re.escape(t)}\b", low)})
        if hits_b or hits_j:
            rows.append((rel, path, hits_b, hits_j, s, path in b_map))

import shutil
shutil.rmtree(tmp, ignore_errors=True)

print(f"files changed on this branch: {len(changed)}")
print(f"changed fields carrying boilerplate or jargon: {len(rows)}")
print(f"  self-referential boilerplate: {sum(1 for r in rows if r[2])}")
print(f"  jargon: {sum(1 for r in rows if r[3])}")
print(f"  of which newly added by this branch: {sum(1 for r in rows if not r[5])}")
print()
for rel, path, hb, hj, s, existed in rows:
    tags = []
    if hb:
        tags.append("BOILER")
    if hj:
        tags.append("JARGON")
    print(f"{rel} :: {path}  [{' '.join(tags)}]  {'(pre-existing)' if existed else '(NEW)'}")
    if hb:
        print(f"    boiler: {hb}")
    if hj:
        print(f"    jargon: {hj}")
    print(f"    {s[:400]}")
    print()
