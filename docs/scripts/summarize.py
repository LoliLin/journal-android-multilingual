#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Summarize an emc-fetch output: which drugs had hits, with product names.
Usage: python summarize.py <outfile>"""
import pathlib
import re
import sys

sys.stdout.reconfigure(encoding="utf-8", errors="replace")
txt = pathlib.Path(sys.argv[1]).read_text(encoding="utf-8")
cur = None
hits = {}
for line in txt.split("\n"):
    if line.startswith("##### "):
        cur = line[6:].strip()
        hits.setdefault(cur, [])
    m = re.match(r"  -- \[(\d+)\] (.+)", line)
    if m and cur:
        hits[cur].append((m.group(1), m.group(2)))

found = {k: v for k, v in hits.items() if v}
print(f"drugs scanned: {len(hits)}   with hits: {len(found)}")
for k, v in found.items():
    print(f"\n{k}  ({len(v)} products)")
    for pid, nm in v:
        print(f"   [{pid}] {nm[:90]}")
