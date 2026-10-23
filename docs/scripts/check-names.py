#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Usage: python check-names.py Name1 Name2 ..."""
import json
import pathlib
import sys

BASE = pathlib.Path("app/src/main/assets/substances")
PLACEHOLDER = ("has not yet been verified", "\u5c1a\u672a\u6838\u5b9e")

for n in sys.argv[1:]:
    rp = BASE / "root" / f"{n}.json"
    if not rp.exists():
        print(f"{n:22} NO ROOT FILE")
        continue
    d = json.loads(rp.read_bytes().decode("utf-8-sig"))
    m = d.get("metabolism") or ""
    st = "PLACEHOLDER" if any(x in m for x in PLACEHOLDER) else ("FILLED" if m else "NO-FIELD")
    zh = "zh:OK" if (BASE / "zh_cn" / f"{n}.json").exists() else "zh:MISSING"
    print(f"{n:22} {st:12} {zh:11} {d.get('categories')}")
