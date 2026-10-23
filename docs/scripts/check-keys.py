#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Check every changed record against HEAD for lost top-level keys.

Uses one git archive of the baseline instead of a git show per file, which
is fast enough to run over the whole tree.

Usage: python check-keys.py
"""
import io
import json
import pathlib
import subprocess
import sys
import tarfile
import tempfile

sys.stdout.reconfigure(encoding="utf-8", errors="replace")
BASE = pathlib.Path("app/src/main/assets/substances")
REF = sys.argv[1] if len(sys.argv) > 1 else "HEAD"

changed = subprocess.run(
    ["git", "diff", "--name-only", REF, "--", "app/src/main/assets/substances"],
    capture_output=True, text=True, encoding="utf-8").stdout.splitlines()

tmp = pathlib.Path(tempfile.mkdtemp())
arch = subprocess.run(["git", "archive", REF, "app/src/main/assets/substances"],
                      capture_output=True)
with tarfile.open(fileobj=io.BytesIO(arch.stdout)) as tf:
    tf.extractall(tmp)

lost_files = []
empty_vals = []
for rel in changed:
    p = pathlib.Path(rel)
    if not p.exists():
        lost_files.append((rel, "FILE GONE"))
        continue
    try:
        d = json.loads(p.read_bytes().decode("utf-8-sig"))
    except Exception as exc:
        lost_files.append((rel, f"PARSE: {exc}"))
        continue
    bp = tmp / rel
    if not bp.exists():
        continue
    try:
        before = json.loads(bp.read_bytes().decode("utf-8-sig"))
    except Exception:
        continue
    if not isinstance(before, dict) or not isinstance(d, dict):
        continue
    miss = set(before) - set(d)
    if miss:
        lost_files.append((rel, f"keys lost: {sorted(miss)}"))
    # a value that became empty where it used to hold something
    for k in set(before) & set(d):
        b, a = before[k], d[k]
        if isinstance(b, str) and b.strip() and (a is None or a == ""):
            empty_vals.append((rel, k, "emptied"))
        if isinstance(b, list) and b and (not isinstance(a, list) or len(a) < len(b)):
            empty_vals.append((rel, k, f"list {len(b)} -> {len(a) if isinstance(a, list) else 'gone'}"))

print(f"changed files: {len(changed)}")
print(f"files with lost keys or parse errors: {len(lost_files)}")
for rel, why in lost_files:
    print(f"  {rel}: {why}")
print(f"fields emptied or shortened: {len(empty_vals)}")
for rel, k, why in empty_vals:
    print(f"  {rel} :: {k}: {why}")
