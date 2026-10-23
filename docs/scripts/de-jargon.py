#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Replace the remaining clinical vocabulary in the branch's fields.

Only the terms that describe how the body handles the drug are replaced.
The names of the metabolic reactions (demethylation, glucuronide,
hydroxylation) and the enzyme names stay: those are what a reader looks up
to understand a pathway, and there is no everyday word for them.

The replacements are phrases, not single words, so the sentence stays
grammatical.

Usage: python de-jargon2.py [--dry]
"""
import json
import pathlib
import re
import subprocess
import sys
import tarfile
import tempfile
import io

sys.stdout.reconfigure(encoding="utf-8", errors="replace")
BASE = pathlib.Path("app/src/main/assets/substances")
DRY = "--dry" in sys.argv
FIELDS = ("metabolism", "generalRisks", "longtermRisks", "saferUse",
          "dosageRemark", "effectsSummary", "addictionPotential", "summary")
ARRAYS = ("toxicities", "saferUse", "longtermRisks", "generalRisks")

# Phrases first: the longer match wins.
PHRASES = [
    (r"\bthe apparent volume of distribution is\b",
     "it spreads through the body at"),
    (r"\bthe volume of distribution is\b",
     "it spreads through the body at"),
    (r"\bvolume of distribution of\b", "the spread through the body of"),
    (r"\bapparent volume of distribution\b",
     "how much it spreads through the body"),
    (r"\bvolume of distribution\b", "how much it spreads through the body"),
    (r"\bmean absolute bioavailability\b", "how much of a dose reaches the blood"),
    (r"\babsolute bioavailability\b", "how much of the dose reaches the blood"),
    (r"\bsystemic bioavailability\b", "how much of the dose reaches the blood"),
    (r"\boral bioavailability\b", "how much of an oral dose reaches the blood"),
    (r"\bpresystemic metabolism\b", "the liver breaking some down on the way"),
    (r"\bpre-systemic elimination\b", "the liver breaking some down on the way"),
    (r"\bfirst-pass metabolism\b", "the liver breaking some down on the way"),
    (r"\bfirst pass metabolism\b", "the liver breaking some down on the way"),
    (r"\bfirst-pass effect\b", "the liver breaking some down on the way"),
    (r"\bplasma elimination half-life\b", "half-life"),
    (r"\bterminal phase half-life\b", "half-life"),
    (r"\bterminal elimination half-life\b", "half-life"),
    (r"\belimination half-life\b", "half-life"),
    (r"\bterminal half-life\b", "half-life"),
    (r"\bhalf-lives\b", "times in the body"),
    (r"\bhalf-life\b", "time in the body"),
    (r"\bpharmacokinetic properties\b", "how the body handles the drug"),
    (r"\bpharmacokinetics\b", "how the body handles the drug"),
    (r"\bpharmacokinetic\b", "how the body handles the drug"),
    (r"\bbiotransformation\b", "breakdown"),
    (r"\bplasma clearance of\b", "the body clears it at"),
    (r"\bplasma clearance is about\b", "the body clears it at about"),
    (r"\btotal plasma clearance is\b", "the body clears it at"),
    (r"\brelative low clearance of\b", "the body clears it slowly, at"),
    (r"\bclearance of\b", "the body clearing it at"),
    (r"\bclearance is\b", "the body clears it at"),
    (r"\bclearance\b", "how fast the body clears it"),
    (r"\bin vitro studies\b", "lab studies"),
    (r"\bin vitro work\b", "lab work"),
    (r"\bin vitro\b", "in the lab"),
    (r"\bin vivo\b", "in the body"),
    (r"\bpost-mortem\b", "after death"),
    (r"\bpostmortem\b", "after death"),
    (r"\bautopsy material\b", "material from post-mortem cases"),
    (r"\bforensic toxicology\b", "toxicology in deaths and drug seizures"),
    (r"\bpredominantly\b", "mostly"),
    (r"\bpredominant\b", "main"),
    (r"\bsubsequently\b", "then"),
    (r"\bconsequently\b", "so"),
    (r"\badditionally\b", "also"),
    (r"\bapproximately\b", "about"),
    (r"\butilising\b", "using"),
    (r"\badministered\b", "given"),
    (r"\bafter oral administration\b", "when taken by mouth"),
    (r"\boral administration\b", "taking it by mouth"),
]


def rewrite(text):
    out = text
    for pat, rep in PHRASES:
        out = re.sub(pat, rep, out, flags=re.I)
    # Collapse runs of spaces and tabs, but keep the line breaks: some of
    # these fields deliberately use them to separate paragraphs.
    out = re.sub(r"[ \t]{2,}", " ", out)
    out = re.sub(r"[ \t]+([,.;:])", r"\1", out)
    out = re.sub(r"\.{2,}", ".", out)
    out = re.sub(r"\.[ \t]*,[ \t]*", ". ", out)
    return out


def main():
    # Everything the branch touched, compared with the merge base -- not just
    # what is uncommitted right now.
    base = subprocess.run(["git", "merge-base", "origin/main", "HEAD"],
                          capture_output=True, text=True, encoding="utf-8").stdout.strip()
    changed = subprocess.run(
        ["git", "diff", "--name-only", base, "HEAD", "--", "app/src/main/assets/substances"],
        capture_output=True, text=True, encoding="utf-8").stdout.splitlines()

    total = 0
    for rel in changed:
        p = pathlib.Path(rel)
        if not p.exists() or p.suffix != ".json":
            continue
        raw = p.read_bytes()
        had_bom = raw.startswith(b"\xef\xbb\xbf")
        text = raw.decode("utf-8-sig")
        try:
            d = json.loads(text)
        except Exception:
            continue
        if not isinstance(d, dict):
            continue
        out = text
        touched = []
        for k in FIELDS:
            v = d.get(k)
            if not isinstance(v, str) or not v.strip():
                continue
            new = rewrite(v)
            if new == v:
                continue
            old_item = json.dumps(v, ensure_ascii=False)
            if old_item not in out:
                continue
            out = out.replace(old_item, json.dumps(new, ensure_ascii=False), 1)
            touched.append(k)
        for k in ARRAYS:
            v = d.get(k)
            if not isinstance(v, list):
                continue
            for item in v:
                if not isinstance(item, str) or not item.strip():
                    continue
                new = rewrite(item)
                if new == item:
                    continue
                old_item = json.dumps(item, ensure_ascii=False)
                if old_item not in out:
                    continue
                out = out.replace(old_item, json.dumps(new, ensure_ascii=False), 1)
                touched.append(f"{k}[]")
        if not touched:
            continue
        try:
            json.loads(out)
        except Exception as exc:
            print(f"SKIP {rel}: invalid JSON ({exc})")
            continue
        if not DRY:
            blob = out.encode("utf-8")
            if had_bom:
                blob = b"\xef\xbb\xbf" + blob
            p.write_bytes(blob)
        total += 1
        print(f"{p.name}  {touched}")
    print(f"\n{'would change' if DRY else 'changed'} {total} files")


if __name__ == "__main__":
    main()
