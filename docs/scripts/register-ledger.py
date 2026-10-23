#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Register sources in docs/substance-metabolism-sources.json.

Usage: python register-ledger.py "Name|https://..." ["Name|https://..." ...]
        python register-ledger.py --lit "Name|https://..." [...]

"product-information" is the default evidence class; --lit marks a
peer-reviewed literature source. Minimal, format-preserving insertion into the
plain-ASCII-sorted "reviewed" array.
"""
import json
import pathlib
import sys

P = pathlib.Path("docs/substance-metabolism-sources.json")
INLINE_MAX_URL = 52


def entry_lines(name, url, evidence):
    if len(url) <= INLINE_MAX_URL:
        src = [f'      "sources": ["{url}"],']
    else:
        src = ['      "sources": [', f'        "{url}"', "      ],"]
    return ["    {", f'      "name": "{name}",', *src,
            f'      "evidence": "{evidence}"', "    },"]


def main():
    args = sys.argv[1:]
    evidence = "product-information"
    if args and args[0] == "--lit":
        evidence = "literature"
        args = args[1:]
    new = [tuple(a.split("|", 1)) for a in args]

    raw_bytes = P.read_bytes()
    nl = "\r\n" if b"\r\n" in raw_bytes else "\n"
    lines = raw_bytes.decode("utf-8").split(nl)

    start = next(i for i, l in enumerate(lines) if l == '  "reviewed": [')
    end = next(i for i in range(start + 1, len(lines)) if lines[i] == "  ],")
    existing = {json.loads(l.split(": ", 1)[1].rstrip(",")) for l in lines[start:end]
                if l.startswith('      "name":')}

    for name, url in new:
        if name in existing:
            print(f"  {name} already reviewed")
            continue
        insert_at = end
        for i in range(start + 1, end):
            if lines[i].startswith('      "name":'):
                other = json.loads(lines[i].split(": ", 1)[1].rstrip(","))
                if other > name:
                    j = i
                    while lines[j] != "    {":
                        j -= 1
                    insert_at = j
                    break
        block = entry_lines(name, url, evidence)
        lines = lines[:insert_at] + block + lines[insert_at:]
        end += len(block)
        print(f"  inserted {name} ({evidence}) at line {insert_at + 1}")

    out = nl.join(lines)
    data = json.loads(out)
    still = [n for n, _ in new if n in data.get("notYetVerified", [])]
    if still:
        lines2 = out.split(nl)
        hdr = next(i for i, l in enumerate(lines2) if l == '  "notYetVerified": [')
        keep, removed = [], []
        for i, l in enumerate(lines2):
            if i > hdr and l.strip().rstrip(",").strip('"') in still:
                removed.append(l.strip())
                continue
            keep.append(l)
        out = nl.join(keep)
        data = json.loads(out)
        print(f"  removed from notYetVerified: {removed}")

    names = [e["name"] for e in data["reviewed"]]
    if names != sorted(names):
        raise SystemExit("REFUSING: reviewed no longer plain-ASCII sorted")
    for n, _ in new:
        if n not in names or n in data.get("notYetVerified", []):
            raise SystemExit(f"REFUSING: {n} state inconsistent")

    P.write_bytes(out.encode("utf-8"))
    chk = json.loads(P.read_bytes().decode("utf-8"))
    print(f"\nreviewed now: {len(chk['reviewed'])}   notYetVerified: {len(chk['notYetVerified'])}")


if __name__ == "__main__":
    main()
