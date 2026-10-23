#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Print the emc sections for chosen drugs from a fetched output file.
Usage: python show-hits.py <outfile> Name1 Name2 ...
Writes UTF-8 to stdout via sys.stdout.reconfigure to dodge the GBK console.
"""
import pathlib
import sys

sys.stdout.reconfigure(encoding="utf-8", errors="replace")
txt = pathlib.Path(sys.argv[1]).read_text(encoding="utf-8")
want = set(sys.argv[2:])
cur = None
keep = False
for line in txt.split("\n"):
    if line.startswith("##### "):
        cur = line[6:].strip()
        keep = cur in want
    if keep:
        print(line)
