#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Find self-referential disclaimers and jargon in the substance records.

Two passes:
  1. boilerplate  - sentences that talk about the evidence instead of the
                     substance ("Evidence is from ...", "The cited evidence
                     is for ...", "Studies conducted in ...", "not evidence
                     that ...").
  2. jargon       - technical vocabulary that a reader of the app would not
                     use, with the field it appears in.

Usage: python scan-jargon.py [substances-dir]
"""
import json
import pathlib
import re
import sys

sys.stdout.reconfigure(encoding="utf-8", errors="replace")
BASE = pathlib.Path(sys.argv[1] if len(sys.argv) > 1 else "app/src/main/assets/substances")

# --- 1. self-referential boilerplate -------------------------------------
# These are statements ABOUT the record or the source, not about the
# substance. "There is no boxed warning" is not one of them -- that is a
# real fact about the drug.
BOILER = [
    r"^Evidence is from\b",
    r"^The cited evidence is\b",
    r"^Evidence for the cited\b",
    r"^The evidence (described|reported|stated) (here|is)\b",
    r"^Studies (were )?conducted in\b",
    r"^The (label|SmPC|summary of product characteristics|monograph) (states|reports|says|gives) that\b",
    r"^The (label|SmPC) (does not|has no) (establish|provide|give|specify|contain)\b",
    r"^According to the (label|SmPC)\b",
    r"^The cited (label|monograph|SmPC|source|review|study|article)\b",
    r"^This label\b",
    r"^The cited\b",
    r"^No detailed (pharmacokinetic )?information is available\b",
    r"^not evidence that\b",
    r"^This is a documentation gap\b",
    r"^do not infer\b",
    r"^In the absence of\b",
    r"^The archived page\b",
    r"^The reviewed archived page\b",
    r"^FreeODwiki\b",
    r"^PsychonautWiki\b",
    r"^Wikipedia\b",
    r"^The source\b",
    r"^A study cited by\b",
    # Chinese equivalents
    r"^证据来自",
    r"^证据为",
    r"^所引",
    r"^该归档页面",
    r"^归档页面",
    r"^FreeODwiki",
    r"^PsychonautWiki",
    r"^维基",
]
BOILER_RE = [re.compile(p, re.I) for p in BOILER]


def boiler_in(text):
    """The self-referential openers, matched at the start of the text only.

    `^` in a pattern used with re.search still matches anywhere, so the
    check has to be done with re.match.
    """
    return [b.pattern for b in BOILER_RE if b.match(text)]

# --- 2. jargon ------------------------------------------------------------
# term -> plain-language replacement the writer should use instead
JARGON = {
    "pre-systemic elimination": "first-pass metabolism (the liver breaks some down before the drug reaches the blood)",
    "presystemic metabolism": "first-pass metabolism",
    "biotransformation": "metabolism",
    "biotransformation products": "metabolites",
    "metabolised": "broken down",
    "metabolized": "broken down",
    "metabolises": "breaks down",
    "metabolizes": "breaks down",
    "hydroxylation": "adding a hydroxyl group",
    "demethylation": "removing a methyl group",
    "dealkylation": "removing an alkyl group",
    "N-demethylation": "removing an N-methyl group",
    "O-demethylation": "removing an O-methyl group",
    "glucuronidation": "attaching glucuronic acid",
    "sulphation": "attaching sulphate",
    "sulfation": "attaching sulfate",
    "conjugation": "attaching a small molecule",
    "glucuronide": "glucuronic acid attached",
    "sulphate conjugate": "sulphate",
    "sulfate conjugate": "sulfate",
    "pharmacokinetic": "how the body handles the drug",
    "pharmacokinetics": "how the body handles the drug",
    "pharmacokinetic properties": "how the body handles the drug",
    "elimination half-life": "half-life",
    "terminal half-life": "half-life",
    "terminal phase half-life": "half-life",
    "plasma elimination half-life": "half-life",
    "volume of distribution": "how much the drug spreads through the body",
    "apparent volume of distribution": "how much the drug spreads through the body",
    "clearance": "how fast the body clears the drug",
    "systemic bioavailability": "how much of the dose reaches the blood",
    "absolute bioavailability": "how much of the dose reaches the blood",
    "oral bioavailability": "how much of an oral dose reaches the blood",
    "first-pass effect": "the liver breaking some down on the way",
    "first pass metabolism": "the liver breaking some down on the way",
    "enzyme": "enzyme",
    "cytochrome P450": "CYP",
    "pseudocholinesterase": "cholinesterase",
    "CYP": "CYP",
    "enterohepatic circulation": "the gut and liver passing the drug back and forth",
    "enterohepatic recirculation": "the gut and liver passing the drug back and forth",
    "urinary excretion": "excretion in urine",
    "faecal excretion": "excretion in faeces",
    "fecal excretion": "excretion in faeces",
    "in vivo": "in the body",
    "in vitro": "in the lab",
    "post-mortem": "after death",
    "postmortem": "after death",
    "autopsy material": "material from post-mortem cases",
    "forensic toxicology": "toxicology in deaths and drug seizures",
    "toxicological": "toxicology",
    "routinely": "",
    "predominantly": "mostly",
    "predominant": "main",
    "approximately": "about",
    "subsequently": "then",
    "additionally": "also",
    "consequently": "so",
    "utilising": "using",
    "utilize": "use",
    "administration": "giving",
    "administered": "given",
    "reconstitution": "mixing",
    "sodium salt": "sodium salt",
    "free base": "free base",
    "matrix": "sample",
    "analyte": "compound",
    "screening": "screening",
    "constituent": "component",
}


def walk(v, path, out):
    if isinstance(v, str):
        out.append((path, v))
    elif isinstance(v, dict):
        for k, x in v.items():
            walk(x, f"{path}.{k}", out)
    elif isinstance(v, list):
        for i, x in enumerate(v):
            walk(x, f"{path}[{i}]", out)


def main():
    rows = []
    for p in sorted(BASE.rglob("*.json")):
        try:
            d = json.loads(p.read_bytes().decode("utf-8-sig"))
        except Exception:
            continue
        if not isinstance(d, dict):
            continue
        strings = []
        walk(d, "$", strings)
        for path, s in strings:
            low = s.lower()
            hits_b = boiler_in(s)
            hits_j = sorted({t for t in JARGON if re.search(rf"\b{re.escape(t)}\b", low)})
            if hits_b or hits_j:
                rows.append((str(p.relative_to(BASE)), path, hits_b, hits_j, s))

    print(f"records with boilerplate or jargon: {len(rows)}")
    n_b = sum(1 for r in rows if r[2])
    n_j = sum(1 for r in rows if r[3])
    print(f"  with self-referential boilerplate: {n_b}")
    print(f"  with jargon: {n_j}")
    print()
    for rel, path, hb, hj, s in rows:
        tags = []
        if hb:
            tags.append("BOILERPLATE")
        if hj:
            tags.append("JARGON")
        print(f"{rel}  {path}  [{' '.join(tags)}]")
        if hb:
            print(f"    boiler: {hb}")
        if hj:
            print(f"    jargon: {hj}")


if __name__ == "__main__":
    main()
