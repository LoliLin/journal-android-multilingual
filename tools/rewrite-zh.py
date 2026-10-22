#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Rewrite the Chinese overlays that open by naming the source.

The Chinese text was a close translation of the English, so it carried the
same framing: "PsychonautWiki 强烈建议..." names the source as the subject
of the sentence. Removing the name and adjusting the verb gives a direct
statement:

    "PsychonautWiki 强烈建议采取减害措施。"
      -> "强烈建议采取减害措施。"
    "该页面警告不要使用高剂量。"
      -> "警告不要使用高剂量。"

Where the source name carries a real limitation -- that the record is for a
prescription product, or an OTC sleep aid, or a particular formulation --
the limitation stays, but it moves to the end of the sentence where it reads
as a note rather than as a preamble:

    "所引 DailyMed 来源为处方口服托莫西汀胶囊（更新于 2026-05-25）。它不是兴奋剂..."
      -> "它不是兴奋剂...（依据：处方口服托莫西汀胶囊，DailyMed，更新于 2026-05-25）"

Usage: python rewrite-zh.py [--dry]
"""
import json
import pathlib
import re
import sys

sys.stdout.reconfigure(encoding="utf-8", errors="replace")
ROOT = pathlib.Path("app/src/main/assets/substances")
DRY = "--dry" in sys.argv

FIELDS = ("metabolism", "generalRisks", "longtermRisks", "saferUse",
          "dosageRemark", "effectsSummary", "addictionPotential", "summary",
          "toxicities")
ARRAYS = ("toxicities", "saferUse", "longtermRisks", "generalRisks",
          "crossTolerances")

# A sentence that starts by saying which product the record is about. The
# product description carries a real limitation -- that the record is for a
# prescription product, or a particular formulation or indication -- so it
# stays, but it moves to the end of the sentence where it reads as a note.
LIMITING = re.compile(
    r"^(?:所引(?:的)?(?:来源|DailyMed\s*来源)|据\s*DailyMed\s*来源|"
    r"据所引来源|所引来源|所引\s*DailyMed\s*来源|所引标签)\s*"
    r"(专指|针对|为|是)\s*"
    r"(.+?)([。；])"
    r"(.*)$", re.S)

SOURCE_NAMES = re.compile(
    r"^(?:FreeODwiki|PsychonautWiki|維基百科|维基百科|"
    r"該歸檔頁面|该归档页面|歸檔頁面|归档页面|該頁面|该页面|"
    r"頁面|页面|來源|来源|資料|资料|該資料|该资料)\s*"
    r"的?")

PAGE_REF = [
    (r"該頁面的", "該"), (r"該頁面中", "其中"), (r"頁面的", "該"),
    (r"頁面中", "其中"), (r"該頁面", "該資料"), (r"頁面(?:上|中)?", "該資料"),
    (r"其頁面", "該資料"),
    (r"该页面的", "该"), (r"该页面中", "其中"), (r"页面的", "该"),
    (r"页面中", "其中"), (r"该页面", "该资料"), (r"页面(?:上|中)?", "该资料"),
    (r"其页面", "该资料"),
]


def rewrite(text):
    out = text.strip()

    # A limiting preamble becomes a note at the end of the sentence.
    m = LIMITING.match(out)
    if m:
        product, punct, rest = m.group(2), m.group(3), m.group(4).strip()
        note = f"（本记录对应：{product}）"
        if rest:
            out = rest.rstrip("。；;") + punct + note
        else:
            out = f"{product}{punct}{note}"

    out = SOURCE_NAMES.sub("", out, count=1)
    out = re.sub(r"^(?:DailyMed|dailymed)\s*", "", out, count=1)

    # The source name in the middle: "据 DailyMed 标签提示 X" -> "标签提示 X"
    out = re.sub(r"^(?:据|根据)\s*(?:DailyMed|dailymed|所引)?\s*"
                 r"(?:标签|來源|来源|资料|資料)\s*", "", out, count=1)
    out = re.sub(r"^(?:所引|所引用的)\s*(?:标签|來源|来源|资料|資料)\s*", "", out, count=1)

    # "所引 DailyMed 标签警示 X" and "所引 DailyMed 胶囊标签警示 X"
    out = re.sub(r"^所引\s*(?:DailyMed|dailymed)?\s*"
                 r"((?:胶囊|膠囊)?标签|證據|证据|來源|来源|資料|资料)\s*", r"\1", out, count=1)
    # "所引 DailyMed 证据针对 X" -> "证据针对 X"
    out = re.sub(r"^所引\s*(?:DailyMed|dailymed)?\s*", "", out, count=1)

    for pat, rep in PAGE_REF:
        out = re.sub(pat, rep, out)

    out = re.sub(r"(?<=[\u4e00-\u9fff])\s+(?=[\u4e00-\u9fff])", "", out)
    out = re.sub(r"[ \t]{2,}", " ", out).strip()
    out = re.sub(r"^\s*，", "", out)
    return out


def main():
    total = 0
    for p in sorted(list(ROOT.glob("zh_cn/*.json")) + list(ROOT.glob("zh_tw/*.json"))):
        try:
            d = json.loads(p.read_bytes().decode("utf-8-sig"))
        except Exception:
            continue
        if not isinstance(d, dict):
            continue
        raw = p.read_bytes()
        had_bom = raw.startswith(b"\xef\xbb\xbf")
        text = raw.decode("utf-8-sig")
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
            print(f"SKIP {p}: invalid JSON ({exc})")
            continue
        if not DRY:
            blob = out.encode("utf-8")
            if had_bom:
                blob = b"\xef\xbb\xbf" + blob
            p.write_bytes(blob)
        total += 1
        print(f"{p.name}  {sorted(set(touched))}")

    print(f"\n{'would change' if DRY else 'changed'} {total} files")


if __name__ == "__main__":
    main()
