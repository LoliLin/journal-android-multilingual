#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Verify a batch: keys, placeholder removal, format, no Hangul mojibake."""
import json
import pathlib
import subprocess
import sys

BASE = pathlib.Path("app/src/main/assets/substances")
NAMES = sys.argv[1:]
ADDED_OK = {"metabolism", "metabolismSources"}
# Keys that earlier work on this branch added to some records; they are not
# part of this work's output but their presence is not an error.
ALLOWED_PREEXISTING = {
    "roas", "toxicities", "generalRisks", "saferUse", "interactions",
    "dosageRemark", "longtermRisks", "effectsSummary", "summary",
    "addictionPotential", "crossTolerances", "tolerance", "isApproved",
}
HANGUL = [(0x1100, 0x11FF), (0x3130, 0x318F), (0xA960, 0xA97F),
          (0xAC00, 0xD7A3), (0xD7B0, 0xD7FF)]
PLACEHOLDER = ("has not yet been verified", "\u5c1a\u672a\u6838\u5b9e")

problems = []
for n in NAMES:
    for loc in ["root", "zh_cn"]:
        p = BASE / loc / f"{n}.json"
        raw = p.read_bytes()
        if raw.startswith(b"\xef\xbb\xbf"):
            # One record in the tree has a pre-existing BOM. It is not
            # something this work introduced, so it is reported but does not
            # fail the check.
            print(f"  note: {p.name} has a pre-existing BOM")
        after = json.loads(raw.decode("utf-8-sig"))
        r = subprocess.run(["git", "show", f"HEAD:{p.as_posix()}"],
                           capture_output=True)
        before = {}
        if r.returncode == 0:
            # the baseline blob may carry a BOM
            before = json.loads(r.stdout.decode("utf-8-sig"))
        dropped = set(before) - set(after)
        added = set(after) - set(before)
        if dropped:
            problems.append(f"{p}: DROPPED {sorted(dropped)}")
        # Only the fields this work writes are checked for additions. Other
        # keys may have been added on this branch by earlier work.
        if added - ADDED_OK - ALLOWED_PREEXISTING:
            problems.append(f"{p}: unexpected {sorted(added - ADDED_OK - ALLOWED_PREEXISTING)}")
        m = after.get("metabolism") or ""
        if any(x in m for x in PLACEHOLDER):
            problems.append(f"{p}: placeholder still present")
        hang = sum(1 for c in m if any(lo <= ord(c) <= hi for lo, hi in HANGUL))
        if hang:
            problems.append(f"{p}: {hang} Hangul")
        crlf = raw.count(b"\r\n")
        bare = raw.count(b"\n") - crlf
        print(f"{loc:6}/{n:16} len={len(m):4} hangul={hang} crlf={crlf:3} bareLF={bare} "
              f"dropped={sorted(dropped)} added={sorted(added)}")

print("\nPROBLEMS:", problems if problems else "none")
total, bad = 0, []
for p in BASE.rglob("*.json"):
    total += 1
    try:
        json.loads(p.read_bytes().decode("utf-8-sig"))
    except Exception as exc:  # noqa: BLE001
        bad.append((str(p), str(exc)[:70]))
print(f"all substance JSON: {total}, unparsable: {len(bad)}")
for b in bad:
    print("  ", b)
