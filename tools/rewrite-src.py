#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Hand-written rewrites of the source-subject sentences.

The source name goes; the facts, warnings and hedges stay. Where the source
was the thing being described ("its dose table is user-reported"), the
sentence is rewritten to say what that means without pointing at a page.
"""
import json
import pathlib
import re
import sys

sys.stdout.reconfigure(encoding="utf-8", errors="replace")
ROOT = pathlib.Path("app/src/main/assets/substances")

# "path/to/record.json::field" or "path::field[index]" -> new text
EDITS = {
    "root/1B-LSD.json::addictionPotential": (
        "Dependence potential is inferred to be low from LSD, but no formal "
        "study of 1B-LSD dependence has been done."),
    "root/1cP-AL-LAD.json::saferUse[0]": (
        "Precautions and harm-reduction measures are strongly recommended for "
        "this potent psychedelic. Effects and toxicity are poorly "
        "characterized, and combination risks should be checked. The dose "
        "table is educational, not a recommendation."),
    "root/1cP-LSD.json::toxicities[0]": (
        "The toxicity section is an incomplete stub that may contain "
        "dangerous errors, and gives no reliable toxic-dose figure."),
    "root/1cP-LSD.json::saferUse[0]": (
        "Harm-reduction measures and independent research are strongly "
        "recommended. The substance is poorly studied, there are risks for "
        "people with underlying mental illness, and the toxicity section may "
        "contain dangerous errors. The dose table is educational, not a "
        "recommendation."),
    "root/1cP-MiPLA.json::addictionPotential": (
        "Addiction potential is inferred to be low by analogy with LSD, but "
        "this has not been formally studied and is not established for "
        "1cP-MiPLA."),
    "root/1cP-MiPLA.json::dosageRemark": (
        "The user-reported dose and duration figures may be inaccurate and "
        "are for educational purposes, not use advice. Verify against "
        "independent sources; the information itself is not fully reviewed."),
    "root/1P-ETH-LAD.json::saferUse": (
        "Starting low, checking combinations independently and using "
        "harm-reduction measures are recommended. Individual sensitivity "
        "varies, and the dose table is user-reported educational "
        "information, not advice."),
    "root/1P-LSD.json::saferUse": (
        "Independent research and harm-reduction measures are recommended, "
        "and individual sensitivity varies. People with a personal or family "
        "history of mental illness are advised to avoid use, particularly "
        "outside medical supervision; that warning comes from an unreviewed "
        "toxicity section. Interaction information is extrapolated from LSD "
        "and may not cover 1P-LSD-specific risks."),
    "root/1V-LSD.json::saferUse": (
        "Starting low, because individual responses vary, is recommended, as "
        "are independent research and harm-reduction measures. People with a "
        "personal or family history of mental illness are advised to avoid "
        "use, especially outside medical supervision; that warning comes "
        "from a stub toxicity section. Interaction information is "
        "extrapolated from LSD."),
    "root/25C-NBOH.json::saferUse[0]": (
        "Independent research and harm-reduction measures are advised, and "
        "insufflation is warned against because of the compound's high "
        "potency. The toxicity section is a stub that may contain dangerous "
        "errors."),
    "root/25C-NBOMe.json::saferUse[0]": (
        "Harm-reduction measures are strongly recommended, and insufflation, "
        "vaporization and tinctures are to be avoided because dose "
        "measurement is difficult and these routes are associated with "
        "overdose deaths."),
    "root/25D-NBOMe.json::saferUse[0]": (
        "Harm-reduction measures are strongly recommended, and insufflation "
        "is warned against because measuring microgram quantities precisely "
        "is difficult. The dose information is educational rather than a "
        "recommendation."),
    "root/25I-NBOH.json::saferUse[0]": (
        "Independent research and harm-reduction measures are recommended, "
        "and high doses and insufflation are warned against because of the "
        "compound's potency. The toxicity section is a stub that may contain "
        "dangerous errors."),
    "root/25I-NBOMe.json::saferUse[0]": (
        "Harm-reduction measures are strongly recommended, and insufflation, "
        "vaporization and tinctures are warned against because dose "
        "measurement is difficult and fatal overdoses have been reported. "
        "Seek medical help for a suspected overdose."),
    "root/25N-NBOMe.json::saferUse[0]": (
        "Precautions and harm-reduction measures are strongly recommended. "
        "The dose information is based on user reports and external "
        "resources for education, not a recommendation, and limited reports "
        "of low or moderate occasional use do not establish safety."),
    "root/2C-B-FLY.json::saferUse[0]": (
        "Harm-reduction measures and independent research are recommended. "
        "The dose table is compiled from user reports and resources for "
        "education, not medical advice, and anecdotal reports of low-to-"
        "moderate occasional use do not establish safety."),
    "root/2C-B.json::metabolism": (
        "No specific human metabolic findings or pathway are described, and "
        "no pathway is inferred from related compounds."),
    "root/2C-C.json::saferUse[0]": (
        "Starting low, independent research and harm-reduction measures are "
        "recommended. The dose information is user-reported educational "
        "material, not a recommendation, and anecdotal reports do not "
        "establish safety."),
    "root/2C-D.json::saferUse[0]": (
        "Starting low is advised, as are independent research and "
        "harm-reduction measures. The dose information is educational rather "
        "than a recommendation, and anecdotal reports do not establish "
        "safety."),
    "root/2C-D.json::metabolism": (
        "No specific human metabolic findings or pathway are described, and "
        "no pathway is inferred from related compounds."),
    "root/2C-E.json::effectsSummary": (
        "Anecdotal reports describe strong visual effects, time distortion, "
        "introspection, ego dissolution and euphoria. Users describe the "
        "effects as unpredictable and dose-sensitive, with potentially "
        "uncomfortable body load, nausea and physical discomfort."),
    "root/2C-EF.json::effectsSummary": (
        "Anecdotal user reports describe strong visual effects, stimulation "
        "and sometimes uncomfortable body load, including nausea and "
        "physical discomfort. Effects are unpredictable and dose-sensitive; "
        "these reports are anecdotal."),
    "root/2C-EF.json::saferUse[0]": (
        "Independent research and harm-reduction practices are advised, "
        "including accurate volumetric measurement. None of this guarantees "
        "safety."),
    "root/2C-H.json::dosageRemark": (
        "Oral dosage and total duration are unknown, and no established dose "
        "data are available."),
    "root/2C-H.json::saferUse[0]": (
        "Harm-reduction practices and accurate measurement are advised. No "
        "dose is established."),
    "root/3-FA.json::saferUse[0]": (
        "Starting low is recommended, and harm-reduction measures are "
        "strongly recommended. The dose table is educational user-report "
        "information, not a recommendation."),
    "root/3-FEA.json::saferUse[0]": (
        "Starting low is advised, and harm-reduction measures are strongly "
        "recommended. The dose tables are source-reported information, not a "
        "recommendation."),
    "root/3-FMA.json::saferUse[0]": (
        "Independent research and harm-reduction measures are recommended. "
        "The toxicity section is marked as a stub that may contain highly "
        "inaccurate information, and the dose table is user-reported "
        "educational material, not a recommendation."),
    "root/3-FMA.json::metabolism": (
        "No specific human metabolic findings or pathway are described, and "
        "no pathway is assumed from similar compounds."),
    "root/3-FPM.json::saferUse[0]": (
        "Harm-reduction measures and extra caution while assessing "
        "individual responses are strongly recommended. Severe flu-like "
        "aftereffects have been reported in some users, and combining 3-FPM "
        "with 25x-NBOMe/NBOH, depressants, or other listed risky "
        "combinations is warned against."),
    "root/3-HO-PCE.json::saferUse[0]": (
        "Harm-reduction measures, starting at a very low dose, and accurate "
        "volumetric measurement are strongly recommended, because of the "
        "substance's potency. Multi-day use and redosing before being fully "
        "sober are advised against, exceeding the listed dose ranges is "
        "warned against, and combining it with depressants is discouraged; "
        "caution is urged with stimulants, psychedelics and other "
        "dissociatives."),
    "root/3-HO-PCP.json::saferUse[0]": (
        "Very low initial doses, accurate volumetric measurement and "
        "harm-reduction measures are recommended. Multi-day use and redosing "
        "before being fully sober are advised against, exceeding the listed "
        "dose range is warned against, and combining with other substances, "
        "especially depressants, stimulants, psychedelics and other "
        "dissociatives, is discouraged."),
    "root/3-Me-PCP.json::dosageRemark": (
        "The information is not fully approved and may be inaccurate, "
        "especially regarding doses, durations, subjective effects, toxicity "
        "and other risks. Its numerical route tables are not transcribed as "
        "structured dose data."),
    "root/3-Me-PCPy.json::metabolism": (
        "No specific human metabolic finding or pathway is provided for "
        "3-Me-PCPy."),
    "root/3-MeO-PCE.json::saferUse[0]": (
        "High doses and multi-day use are strongly advised against."),
    "root/3-MeO-PCMo.json::saferUse[0]": (
        "Harm reduction is strongly recommended, to make sure the intended "
        "dose is what actually gets taken."),
    "root/3-MeO-PCP.json::effectsSummary": (
        "Anecdotal user reports describe dissociative effects that can build "
        "toward a hole-like state of emptiness, space and void, along with "
        "visual separation and dissociation of consciousness. Higher doses "
        "are more likely to produce a full spectrum along with adverse "
        "effects."),
    "root/3-MeO-PCP.json::metabolism": (
        "No specific human metabolic finding or pathway is given for "
        "3-MeO-PCP, and its pharmacology is described as incomplete."),
    "root/3-MMC.json::saferUse[0]": (
        "Harm-reduction measures are strongly recommended, and the dose "
        "tables are unverified educational information, not a recommendation."),
    "root/3C-E.json::saferUse[0]": (
        "Independent research and harm-reduction measures are recommended, as "
        "is starting low. The dose table is user and resource material for "
        "education rather than a recommendation, and the toxicity section is "
        "marked as a stub that may contain dangerous errors."),
    "root/3C-E.json::metabolism": (
        "No specific human metabolic findings or pathway are described, and "
        "no pathway is inferred from related compounds."),
}


def main():
    changed = 0
    for key, new in EDITS.items():
        rel, _, where = key.partition("::")
        p = ROOT / rel
        if not p.exists():
            print(f"MISSING {rel}")
            continue
        raw = p.read_bytes()
        had_bom = raw.startswith(b"\xef\xbb\xbf")
        text = raw.decode("utf-8-sig")
        d = json.loads(text)
        m = re.match(r"^(\w+)(?:\[(\d+)\])?$", where)
        field, idx = m.group(1), m.group(2)
        v = d.get(field)
        if isinstance(v, list):
            i = int(idx)
            old = v[i]
        else:
            old = v
        if not isinstance(old, str):
            print(f"  {rel} {where}: not a string, skipped")
            continue
        if old == new:
            print(f"  {rel} {where}: already matches")
            continue
        old_item = json.dumps(old, ensure_ascii=False)
        new_item = json.dumps(new, ensure_ascii=False)
        if old_item not in text:
            print(f"  {rel} {where}: text not found, skipped")
            continue
        out = text.replace(old_item, new_item, 1)
        try:
            json.loads(out)
        except Exception as exc:
            print(f"SKIP {rel}: invalid JSON ({exc})")
            continue
        blob = out.encode("utf-8")
        if had_bom:
            blob = b"\xef\xbb\xbf" + blob
        p.write_bytes(blob)
        changed += 1
        print(f"  {rel} {where}")
    print(f"\nchanged {changed}")


if __name__ == "__main__":
    main()
