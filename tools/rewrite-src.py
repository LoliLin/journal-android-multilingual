#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Hand-written rewrites of the source-subject sentences.

The source name goes; the facts, warnings and hedges stay. Where the source
was the thing being described ("its dose table is user-reported"), the
sentence is rewritten to say what that means without pointing at a page.

Two kinds of entry:

  BY_TEXT   the same wording appears on several records, so the replacement
            is keyed by the current text and applies wherever it appears.
  EDITS     keyed by "record::field" or "record::field[index]".

Both are applied by exact match, so a stale entry cannot silently overwrite
something else: if the current text is not what the table expects, the
record is skipped and reported.
"""
import json
import pathlib
import re
import sys

sys.stdout.reconfigure(encoding="utf-8", errors="replace")
ROOT = pathlib.Path("app/src/main/assets/substances")

# --------------------------------------------------------------- by text
BY_TEXT = {
    "FreeODwiki strongly recommends harm reduction, and advises independent "
    "research before consumption to make sure combinations of two or more "
    "substances are safe.":
        "Harm reduction is strongly recommended, as is independent research "
        "before taking it, to make sure combinations of two or more "
        "substances are safe.",

    "FreeODwiki strongly recommends harm reduction and independent research.":
        "Harm reduction and independent research are strongly recommended.",

    "FreeODwiki strongly recommends precautions and harm reduction for this "
    "potent psychedelic.":
        "Precautions and harm reduction are strongly recommended for this "
        "potent psychedelic.",

    "FreeODwiki strongly recommends preparation and harm reduction for this "
    "potent, long-lasting psychedelic.":
        "Preparation and harm reduction are strongly recommended for this "
        "potent, long-lasting psychedelic.",

    "FreeODwiki strongly recommends independent research and harm reduction, "
    "and advises against prolonged or high-dose use.":
        "Independent research and harm reduction are strongly recommended, "
        "and prolonged or high-dose use is advised against.",

    "FreeODwiki strongly recommends harm reduction and advises against "
    "prolonged or high-dose use.":
        "Harm reduction is strongly recommended, and prolonged or high-dose "
        "use is advised against.",

    "FreeODwiki strongly recommends harm reduction given the limited "
    "pharmacology, metabolism and toxicity data.":
        "Harm reduction is strongly recommended, given how little is known "
        "about the pharmacology, metabolism and toxicity.",

    "FreeODwiki strongly recommends harm reduction; it emphasizes careful "
    "dose measurement because synthetic cannabinoids are active in the "
    "milligram range.":
        "Harm reduction is strongly recommended, and careful dose measurement "
        "is emphasised, because synthetic cannabinoids are active in the "
        "milligram range.",

    "FreeODwiki recommends becoming familiar with and using harm-reduction "
    "practices despite describing low toxicity at reasonable doses.":
        "Becoming familiar with and using harm-reduction practices is "
        "recommended, even though toxicity is described as low at reasonable "
        "doses.",

    "FreeODwiki strongly recommends harm reduction; effects can be "
    "unpredictable and severe psychological reactions are possible.":
        "Harm reduction is strongly recommended; effects can be unpredictable "
        "and severe psychological reactions are possible.",

    "FreeODwiki strongly recommends precautions and harm reduction for this "
    "poorly studied psychedelic.":
        "Precautions and harm reduction are strongly recommended for this "
        "poorly studied psychedelic.",

    "FreeODwiki strongly recommends harm reduction and independent research; "
    "its toxicity subsection is explicitly an unreliable stub.":
        "Harm reduction and independent research are strongly recommended; "
        "the toxicity subsection is explicitly an unreliable stub.",

    "FreeODwiki strongly recommends harm reduction; avoid excessive doses "
    "and repeated redosing because they are considered to substantially "
    "increase toxicity.":
        "Harm reduction is strongly recommended. Excessive doses and repeated "
        "redosing should be avoided, as they are considered to substantially "
        "increase toxicity.",

    "FreeODwiki strongly recommends harm reduction and independent research "
    "because human evidence is limited.":
        "Harm reduction and independent research are strongly recommended, "
        "because human evidence is limited.",

    "FreeODwiki urges caution and harm reduction because MDEA may be "
    "habit-forming and its pharmacology, metabolism and toxicity data are "
    "nearly absent.":
        "Caution and harm reduction are urged, because MDEA may be "
        "habit-forming and there is almost no data on its pharmacology, "
        "metabolism or toxicity.",

    "FreeODwiki strongly discourages high doses and repeated redosing and "
    "suggests one to three months between uses.":
        "High doses and repeated redosing are strongly discouraged, and one "
        "to three months between uses is suggested.",

    "FreeODwiki strongly discourages high doses, use over consecutive days "
    "and combinations with substances known to increase psychosis risk.":
        "High doses, use over consecutive days, and combinations with "
        "substances known to increase psychosis risk are strongly "
        "discouraged.",

    "FreeODwiki recommends harm reduction because evidence is sparse.":
        "Harm reduction is recommended, because the evidence is sparse.",

    "FreeODwiki recommends harm reduction for this poorly studied "
    "substance.":
        "Harm reduction is recommended for this poorly studied substance.",

    "FreeODwiki advises independent research and avoiding dangerous drug "
    "combinations; interaction information is very limited.":
        "Independent research and avoiding dangerous drug combinations are "
        "advised; interaction information is very limited.",

    "FreeODwiki recommends independent research and harm reduction; the oral "
    "dose table is placeholder-only and must not be interpreted as "
    "quantitative guidance.":
        "Independent research and harm reduction are recommended. The oral "
        "dose table is placeholder-only and must not be interpreted as "
        "quantitative guidance.",

    # Chinese text that was written into an English record by mistake
    "FreeODwiki 强烈建议对这种研究不足的物质采取预防和伤害减少措施；剂量表是教育用途用户/资源资料，应交叉核实。":
        "Precautions and harm reduction are strongly recommended for this "
        "poorly studied substance. The dose table is educational user and "
        "resource material and should be checked against other sources.",

    "FreeODwiki strongly recommends harm reduction, and advises independent "
    "research before ingestion to ensure combinations of two or more "
    "substances are safe.":
        "Harm reduction is strongly recommended, as is independent research "
        "before ingestion to make sure combinations of two or more substances "
        "are safe.",

    "FreeODwiki strongly recommends harm reduction, and advises independent "
    "research to ensure combinations of two or more substances are safe "
    "before use.":
        "Harm reduction is strongly recommended, as is independent research "
        "to make sure combinations of two or more substances are safe before "
        "use.",

    "FreeODwiki advises conducting independent research before consuming, to "
    "make sure combinations of two or more substances are safe, and strongly "
    "recommends harm reduction.":
        "Independent research before consuming is advised, to make sure "
        "combinations of two or more substances are safe, and harm reduction "
        "is strongly recommended.",

    "FreeODwiki's subjective-effects section is a subjective-effects index "
    "built on anecdotal user reports and contributor analysis, to be taken "
    "with healthy skepticism. Effects may not occur in a predictable or "
    "reliable way, that higher doses are more likely to produce the full "
    "spectrum, and that adverse effects become more likely as dose "
    "increases, potentially including addiction, serious injury or death.":
        "The subjective-effects index is built on anecdotal user reports and "
        "contributor analysis, and should be taken with healthy skepticism. "
        "Effects may not occur in a predictable or reliable way, higher doses "
        "are more likely to produce the full spectrum, and adverse effects "
        "become more likely as the dose increases, potentially including "
        "addiction, serious injury or death.",

    "FreeODwiki strongly recommends appropriate harm reduction, given the "
    "potent and long-lasting stimulant effects, habit-forming potential and "
    "unknown toxicity profile.":
        "Appropriate harm reduction is strongly recommended, given the "
        "potent and long-lasting stimulant effects, the habit-forming "
        "potential and the unknown toxicity profile.",

    "FreeODwiki strongly recommends harm reduction, since toxicity and "
    "long-term health effects have not been studied and human use history is "
    "very limited.":
        "Harm reduction is strongly recommended, since toxicity and long-term "
        "health effects have not been studied and the history of human use is "
        "very limited.",

    "FreeODwiki strongly recommends harm reduction given the substance's "
    "extreme novelty and unknown toxicity profile.":
        "Harm reduction is strongly recommended, given how new the substance "
        "is and the unknown toxicity profile.",

    "FreeODwiki strongly recommends harm reduction, and advises conducting "
    "independent research before ingestion to ensure combinations of two or "
    "more substances are safe.":
        "Harm reduction is strongly recommended, as is independent research "
        "before ingestion to make sure combinations of two or more substances "
        "are safe.",

    "FreeODwiki strongly recommends harm reduction given limited human-use "
    "history and sparse pharmacology, metabolism and toxicity data.":
        "Harm reduction is strongly recommended, given the limited history of "
        "human use and the sparse data on pharmacology, metabolism and "
        "toxicity.",

    "FreeODwiki strongly recommends harm reduction given limited human-use "
    "history and sparse human data.":
        "Harm reduction is strongly recommended, given the limited history of "
        "human use and the sparse human data.",

    "FreeODwiki strongly recommends harm reduction given the near absence of "
    "pharmacological, metabolic and toxicity data.":
        "Harm reduction is strongly recommended, given how little is known "
        "about the pharmacology, metabolism and toxicity.",

    "FreeODwiki strongly recommends adequate preparation and harm reduction "
    "for this potent, dose-sensitive, long-lasting psychedelic.":
        "Adequate preparation and harm reduction are strongly recommended for "
        "this potent, dose-sensitive, long-lasting psychedelic.",

    "FreeODwiki dose information is an educational user/resource "
    "compilation, not a recommendation; it advises checking other sources and "
    "starting low because of individual differences.":
        "The dose information is an educational user and resource "
        "compilation, not a recommendation. Checking other sources and "
        "starting low, because of individual differences, is advised.",

    "FreeODwiki dose information is collected from user and web resources "
    "for education and should be verified elsewhere.":
        "The dose information is collected from user and web resources for "
        "education and should be verified elsewhere.",

    "FreeODwiki strongly recommends harm reduction, and advises independent "
    "research before ingestion to make sure combinations of two or more "
    "substances are safe.":
        "Harm reduction is strongly recommended, as is independent research "
        "before ingestion to make sure combinations of two or more substances "
        "are safe.",

    "FreeODwiki strongly advises against prolonged or high-dose use of "
    "synthetic cannabinoids and recommends harm reduction.":
        "Prolonged or high-dose use of synthetic cannabinoids is strongly "
        "advised against, and harm reduction is recommended.",

    "FreeODwiki strongly discourages high-dose or consecutive multi-day use; "
    "dose tables are educational user/resource material.":
        "High-dose or consecutive multi-day use is strongly discouraged, and "
        "the dose tables are educational user and resource material.",

    "FreeODwiki strongly discourages high-dose or consecutive multi-day use; "
    "dose data are user/resource educational material.":
        "High-dose or consecutive multi-day use is strongly discouraged, and "
        "the dose data are user and resource educational material.",

    "FreeODwiki strongly recommends harm reduction because human evidence is "
    "limited.":
        "Harm reduction is strongly recommended, because the human evidence "
        "is limited.",

    "FreeODwiki has not fully approved the MXPr page and warns its dose and "
    "risk information may be incorrect.":
        "The MXPr page has not been fully approved, and its dose and risk "
        "information is warned as possibly incorrect.",

    "FreeODwiki has not fully approved this page and warns that dosage, "
    "duration, effects, toxicity and other risk claims may be incorrect.":
        "This page has not been fully approved, and its dosage, duration, "
        "effects, toxicity and other risk claims are warned as possibly "
        "incorrect.",

    "FreeODwiki has not fully approved the page and warns its dosage, "
    "duration, effects, toxicity and other risk information may be "
    "inaccurate.":
        "The page has not been fully approved, and its dosage, duration, "
        "effects, toxicity and other risk information is warned as possibly "
        "inaccurate.",

    "FreeODwiki dose data are educational user/resource material, not a "
    "recommendation; exact toxic dose is unknown.":
        "The dose data are educational user and resource material, not a "
        "recommendation, and the exact toxic dose is unknown.",

    "FreeODwiki dose information is educational user/resource material, not a "
    "recommendation; cross-check independently.":
        "The dose information is educational user and resource material, not a "
        "recommendation; check it independently.",

    "FreeODwiki presents subjective effects based on user reports and notes "
    "effects may be unpredictable; adverse effects may become more likely as "
    "dose increases.":
        "Subjective effects are presented as coming from user reports, and "
        "effects may be unpredictable; adverse effects may become more likely "
        "as the dose increases.",

    "FreeODwiki strongly recommends harm reduction because human safety "
    "evidence is limited.":
        "Harm reduction is strongly recommended, because the human safety "
        "evidence is limited.",

    "The source is an unpublished, unreviewed discussion draft. It claims "
    "long-term synthetic cannabinoid misuse is associated with multiple "
    "deaths and more dangerous adverse effects and toxicity than cannabis, "
    "and advises strongly against prolonged or high-dose use; these claims "
    "carry the source's explicit warning that information may be incorrect.":
        "The information comes from an unpublished, unreviewed discussion "
        "draft. It claims long-term synthetic cannabinoid misuse is "
        "associated with multiple deaths and more dangerous adverse effects "
        "and toxicity than cannabis, and advises strongly against prolonged "
        "or high-dose use; these claims carry an explicit warning that the "
        "information may be incorrect.",

    # A limiting preamble. The product description carries a real
    # limitation -- that the record is for a prescription product, an OTC
    # sleep aid, a particular formulation -- so it stays, but it moves to
    # the end where it reads as a note rather than as a preamble.
    "The cited DailyMed product is an OTC oral sleep aid. Its product "
    "directions are for occasional sleeplessness and are not recreational "
    "dose guidance; no recreational threshold or duration is established "
    "here.":
        "Its product directions are for occasional sleeplessness and are not "
        "recreational dose guidance; no recreational threshold or duration "
        "is established here. (This record is for an OTC oral sleep aid.)",

    "The cited evidence is for prescription methadone hydrochloride oral "
    "concentrate used in opioid-dependence treatment (DailyMed label "
    "updated 2026-07-29). It is not a recreational dosing guide, and its "
    "warnings are about opioid-related harm and overdose.":
        "It is not a recreational dosing guide, and its warnings are about "
        "opioid-related harm and overdose. (This record is for prescription "
        "methadone hydrochloride oral concentrate used in opioid-dependence "
        "treatment.)",

    "The cited in-vitro work reports deacylation of 1B-LSD to LSD; the "
    "archived page names CYP1A2 and CYP3A4 and notes findings in human "
    "serum and rats. This does not establish whole-body human "
    "pharmacokinetics.":
        "Deacylation of 1B-LSD to LSD has been reported in in-vitro work; "
        "CYP1A2 and CYP3A4 are named, with findings in human serum and "
        "rats. This does not establish whole-body human pharmacokinetics.",

    "A study cited by FreeODwiki reportedly found LSD formation after "
    "incubating 1cP-LSD with human serum. This supports a possible "
    "conversion to LSD but does not establish whole-body human "
    "pharmacokinetics or prove that all effects are mediated by LSD.":
        "LSD formation was reportedly found after incubating 1cP-LSD with "
        "human serum. This supports a possible conversion to LSD but does not "
        "establish whole-body human pharmacokinetics or prove that all "
        "effects are mediated by LSD.",

    "The cited evidence is for prescription methadone hydrochloride oral "
    "concentrate used in opioid-dependence treatment (DailyMed label "
    "updated 2026-07-29; relevant warning sections revised 2025-12). "
    "Methadone has delayed and prolonged respiratory-depressant effects, can "
    "accumulate with repeated use, and has a narrow therapeutic index. The "
    "label warns of fatal respiratory depression, QT prolongation/torsades, "
    "fatal accidental ingestion, and potentially fatal exposure increases "
    "from CYP interactions. Benzodiazepines, alcohol, and other CNS "
    "depressants increase sedation and respiratory-depression risk.":
        "Methadone has delayed and prolonged respiratory-depressant effects, "
        "can accumulate with repeated use, and has a narrow therapeutic "
        "index. The label warns of fatal respiratory depression, QT "
        "prolongation and torsades, fatal accidental ingestion, and "
        "potentially fatal exposure increases from CYP interactions. "
        "Benzodiazepines, alcohol and other central nervous system "
        "depressants increase sedation and respiratory-depression risk. "
        "(This record is for prescription methadone hydrochloride oral "
        "concentrate used in opioid-dependence treatment.)",
}

# ---------------------------------------------------------------- by file
EDITS = {
    "root/5-MeO-DMT.json::toxicities[0]": (
        "The toxicity and harm section is under construction and may contain "
        "incomplete and even dangerously wrong information. The toxicity and "
        "long-term health effects of recreational 5-MeO-DMT appear not to "
        "have been researched in any scientific context and the exact toxic "
        "dose is unknown, because its history of human use is limited. "
        "Anecdotal evidence suggests trying it alone at low to moderate doses "
        "and using it sparingly is unlikely to produce negative health "
        "effects, though this cannot be fully guaranteed."),
    "root/5-MeO-DMT.json::generalRisks": (
        "The toxicity section is under construction and possibly dangerously "
        "wrong. Toxicity and long-term effects appear unresearched with an "
        "unknown toxic dose, and anecdotal reports suggest the substance is "
        "only appropriate for users experienced with powerful psychedelics "
        "such as DMT, ayahuasca and DPT who have training in safe use."),
    "root/5-MeO-MiPT.json::effectsSummary": (
        "The effects index is built on anecdotal user reports and contributor "
        "analysis, and should be taken with healthy skepticism. Adverse "
        "effects become more likely at higher doses, potentially including "
        "addiction, serious injury or death."),
    "root/5-MeO-αMT.json::toxicities[0]": (
        "Recreational toxicity and long-term health effects of 5-MeO-αMT "
        "appear not to have been studied in any scientific context, and the "
        "exact toxic dose is unclear, because it is a research chemical with "
        "a very short history of human use. One death associated with "
        "5-MeO-αMT is reported, with an unknown dose, and a small number of "
        "unconfirmed reports of hospitalisations after high doses (above 60 "
        "mg orally) have been received."),
    "root/5-MeO-αMT.json::toxicities[1]": (
        "Severe doses are associated with hospitalisation, coma and death. As "
        "a potent monoamine reuptake inhibitor, 5-MeO-αMT can be very "
        "dangerous in overdose or when combined with MAOIs, RIMAs, "
        "stimulants, or any substance that releases or inhibits reuptake of "
        "serotonin and dopamine."),
    "root/5-MeO-αMT.json::effectsSummary": (
        "5-MeO-αMT is described as a synthetic tryptamine producing powerful "
        "and long-lasting psychedelic, empathogenic and stimulant effects. "
        "Moderate to extreme nausea occurs at almost any dose, either "
        "clearing after vomiting or gradually subsiding towards the peak, and "
        "visual effects mostly appear only at high doses and are weak "
        "relative to the accompanying cognitive and somatic effects "
        "compared with LSD or psilocin. The effects index is built on "
        "anecdotal user reports."),
    "root/5-MeO-αMT.json::generalRisks": (
        "One associated death of unknown dose is reported, along with a small "
        "number of unconfirmed hospitalisation reports after high oral doses "
        "above 60 mg, and severe doses are associated with hospitalisation, "
        "coma and death. As a potent monoamine reuptake inhibitor the "
        "substance can be very dangerous in overdose or combined with MAOIs, "
        "RIMAs, stimulants, or serotonin and dopamine releasers or reuptake "
        "inhibitors. Toxicity and long-term effects appear unresearched."),
    "root/5-MeO-αMT.json::longtermRisks": (
        "Recreational toxicity and long-term health effects of 5-MeO-αMT "
        "appear not to have been studied in any scientific context."),
    "root/5-MeO-αMT.json::metabolism": (
        "The pharmacology section is incomplete. The psychedelic effects of "
        "5-MeO-αMT are believed to arise from its acting as a partial agonist "
        "at 5-HT receptors, while the precise roles of these interactions "
        "remain elusive, and the substance is described elsewhere as a potent "
        "monoamine reuptake inhibitor. No specific human metabolic pathway "
        "is given."),
    "root/5F-PB-22.json::dosageRemark": (
        "Separate smoked and oral tables are given. Smoked: threshold 1 mg, "
        "light 1-3 mg, moderate 3-5 mg, strong 5-8 mg, heavy 8 mg+; total 90 "
        "min, onset 10 sec, peak 30 min, offset 1 hour, afterglow 15 min. "
        "Oral: same dose bands, total 2-3 hours, onset 30 min, peak 1.5 "
        "hours, offset 1 hour, afterglow 30 min. This is an educational "
        "user and resource compilation, not a recommendation."),
    "root/6-APDB.json::saferUse[0]": (
        "Starting lower is advised, because of differences in weight, "
        "tolerance, metabolism and individual sensitivity."),
    "root/AB-CHMINACA.json::toxicities[0]": (
        "The entry is an unpublished discussion draft, not fully reviewed by "
        "site administrators, and it explicitly warns that toxicity and other "
        "information may be incorrect. Long-term synthetic cannabinoid misuse "
        "is associated with multiple deaths and more dangerous adverse "
        "effects and toxicity than cannabis, but the draft status requires "
        "caution."),
    "root/AB-CHMINACA.json::generalRisks": (
        "The source is an unpublished, unreviewed discussion draft. It claims "
        "long-term synthetic cannabinoid misuse is associated with multiple "
        "deaths and more dangerous adverse effects and toxicity than "
        "cannabis, and advises strongly against prolonged or high-dose use; "
        "these claims carry the source's explicit warning that information "
        "may be incorrect."),
    "root/ALD-52.json::longtermRisks": (
        "No specific established long-term harm findings are available. "
        "ALD-52 is said to be considered non-toxic by the scientific "
        "community, while unpredictable acute psychological adverse "
        "reactions remain possible."),
    "root/Loperamide.json::saferUse[2]": (
        "Combining with stimulants is also warned about, because of the "
        "added strain on the cardiovascular system, as is combining with "
        "CYP3A4 or CYP2C8 inhibitors, which may raise loperamide levels in "
        "the body."),
    "root/Loperamide.json::saferUse[3]": (
        "The toxicity section is marked as incomplete and may contain "
        "dangerous errors. Check safety information against authoritative "
        "medical sources."),
    "root/Loperamide.json::addictionPotential": (
        "Overall abuse potential is described as low, but physical "
        "dependence and withdrawal may occur after long-term use."),
    "root/LSA.json::dosageRemark": (
        "The seed tables are not standardized LSA doses. Morning glory seeds "
        "are listed as threshold 20-50 seeds/1.5 g, light 50-100 seeds/1.5-3 "
        "g, moderate 100-250 seeds/3-6 g, strong 250-400 seeds/6-10 g, heavy "
        "400+/10 g+; Hawaiian baby woodrose seeds are separately listed as "
        "threshold 1-3, light 3-5, moderate 5-7, strong 7-12, heavy 12+ "
        "seeds. No standardized pure-LSA oral or sublingual dose is given, so "
        "no numeric route dose is encoded. This is educational user and "
        "resource information, not a recommendation."),
    "root/LSA.json::saferUse[0]": (
        "Starting low is recommended, because seed alkaloid content and "
        "individual sensitivity vary, and harm reduction is recommended."),
    "root/LSZ.json::longtermRisks": (
        "No established long-term harm findings are available. LSZ is stated "
        "to be not considered physiologically toxic, while acute "
        "psychological adverse reactions remain possible."),
    "root/LSZ.json::saferUse[0]": (
        "Harm reduction is strongly recommended, because severe psychological "
        "reactions remain possible, especially in people predisposed to "
        "mental illness."),
    "root/MDA.json::longtermRisks": (
        "Neurotoxicity is described as greater than with MDMA or MDEA, and "
        "one to three months between uses is advised to support serotonin "
        "recovery and reduce toxicity."),
    "root/MDNMB.json::addictionPotential": (
        "Addictive potential is described as moderate and abuse potential as "
        "high, with possible psychological dependence after long-term use."),
    "root/MDNMB.json::saferUse[0]": (
        "Both dehydration and excessive water intake should be avoided; "
        "small sips are recommended rather than drinking a large amount at "
        "once."),
    "root/MET.json::dosageRemark": (
        "The user and resource table lists thresholds of 10 mg smoked, 40 mg "
        "oral, and 5 mg insufflated; the light bands begin at 20 mg, 60 mg "
        "and 10 mg respectively. Freebase and fumarate reports differ: "
        "fumarate smoked or vaporized at 15-65 mg and oral at 80-110 mg are "
        "said to produce effects. Data on human pharmacology and toxicity "
        "are very limited, and these figures are educational reports, not "
        "dosing advice."),
    "root/MPT.json::dosageRemark": (
        "The oral dose and duration values are placeholders (three question "
        "marks), not usable measurements. Toxicity and long-term recreational "
        "effects are unstudied and the exact toxic dose is unknown, so no "
        "numeric dose bands are inferred."),
    "root/MXiPr.json::dosageRemark": (
        "The information is not fully approved and explicitly warns that "
        "dose, duration, subjective effects, toxicity and other risks may be "
        "incorrect. Its table is not used as structured dose data pending "
        "independent corroboration."),
    "root/4-AcO-DMT.json::saferUse[0]": (
        "Harm reduction is strongly recommended, and starting at low doses is "
        "advised, since onset, weight, tolerance, metabolism and individual "
        "sensitivity all differ between people."),
    "root/4-FA.json::saferUse[0]": (
        "Harm-reduction measures are strongly recommended. Nasal use is "
        "advised against, because of reported corrosive injury to the nose and "
        "throat, and the dose table is educational, not a recommendation."),
    "root/4-FMA.json::saferUse[0]": (
        "Harm-reduction measures are strongly recommended. Reported "
        "corrosivity may injure the nose and throat if the substance is "
        "insufflated, and the oral dose table is unverified, not a "
        "recommendation."),
    "root/4-HO-DiPT.json::effectsSummary": (
        "The subjective-effects index is built on anecdotal user reports and "
        "contributor analysis, and should be taken with healthy skepticism. "
        "Effects may not occur in a predictable or reliable way, higher doses "
        "are more likely to produce the full spectrum, and adverse effects "
        "become more likely as the dose increases, potentially including "
        "addiction, serious injury or death."),
    "root/4-HO-MET.json::effectsSummary": (
        "Many anecdotal reports indicate that active doses may vary "
        "considerably: some users report very intense experiences at doses as "
        "low as 17 mg, while others report mild experiences at doses as high "
        "as 30 mg. Possible reasons include individual neurochemical and "
        "metabolic differences, and batch quality and physical form such as "
        "hydrochloride versus fumarate."),
    "root/4-HO-MET.json::longtermRisks": (
        "No established long-term findings are available, and the data on its "
        "pharmacology, metabolism and toxicity is very little."),
    "root/4-HO-MET.json::saferUse[0]": (
        "Harm reduction is strongly recommended if the substance is used, "
        "since a risk profile similar to psilocin and 4-AcO-DMT is assumed "
        "without supporting data."),
    "root/4F-MPH.json::toxicities[0]": (
        "The toxicity and harm-potential section is a draft whose "
        "information may be incomplete and may contain dangerous errors. "
        "Recreational toxicity and long-term health effects of 4F-MPH appear "
        "not to have been studied in any scientific context and the exact "
        "toxic dose is unknown, because it is a research chemical with a very "
        "short history of human use. Anecdotal evidence from people who have "
        "tried it suggests that using it only at low to moderate doses and "
        "very occasionally has no attributable negative health effects, "
        "though nothing can be fully guaranteed."),
    "root/4F-MPH.json::effectsSummary": (
        "The subjective-effects index is built on anecdotal user reports and "
        "contributor analysis, and should be taken with healthy skepticism. "
        "Effects may not occur in a predictable or reliable way, higher doses "
        "are more likely to produce the full spectrum, and adverse effects "
        "become more likely as the dose increases, potentially including "
        "addiction, serious injury or death."),
    "root/4F-MPH.json::generalRisks": (
        "The toxicity section is a draft that may contain incomplete and even "
        "dangerously wrong information. Recreational toxicity and long-term "
        "health effects appear not to have been studied in any scientific "
        "context, and independent research before ingestion is advised to "
        "make sure combinations of two or more substances are safe."),
    "root/5-APB.json::effectsSummary": (
        "5-APB is placed among substituted benzofuran substances that are "
        "known to produce euphoria, empathogenic effects, stimulation and "
        "mild psychedelic effects, alongside MDA, 6-APB and 5-EAPB. The "
        "effects index is built on anecdotal user reports, and adverse "
        "effects become more likely as the dose increases, potentially "
        "including addiction, serious injury or death."),
    "root/5-MeO-DiBF.json::longtermRisks": (
        "No established long-term findings are available. The toxicity "
        "profile is unknown and the history of human use was almost absent "
        "before 2015."),
    "root/5-MeO-DiPT.json::toxicities[0]": (
        "The toxicity and harm-potential section is short and may be "
        "incomplete and even extremely wrong. Long-term health effects of "
        "recreational 5-MeO-DiPT use appear not to have been studied in any "
        "scientific context and the exact toxic dose is unclear, because it "
        "is a research chemical with a very short history of human use; its "
        "neurotoxic effects have been studied in rats."),
    "root/5-MeO-DiPT.json::generalRisks": (
        "The toxicity section may contain extremely wrong information. "
        "Overdoses are reported, including one death 3-4 hours after an "
        "apparent rectal overdose and another after colonic administration, "
        "and 5-MeO-DiPT is warned about as a monoamine reuptake inhibitor "
        "for which MAOIs are a potentially dangerous combination that would "
        "very likely unpredictably increase its effects. Taking this "
        "chemical while on prescription MAOIs is strongly advised against."),
    "root/AB-CHMINACA.json::generalRisks": (
        "The source is an unpublished, unreviewed discussion draft. It claims "
        "long-term synthetic cannabinoid misuse is associated with multiple "
        "deaths and more dangerous adverse effects and toxicity than "
        "cannabis, and advises strongly against prolonged or high-dose use; "
        "these claims carry the source's explicit warning that information "
        "may be incorrect."),
    "root/Bromo-DragonFLY.json::saferUse[0]": (
        "Not attempting to use the substance without strict harm reduction, a "
        "trip sitter and oral-only administration is recommended, and "
        "volumetric dosing is specifically mentioned."),
    "root/DET.json::saferUse[0]": (
        "Independent research and harm reduction are recommended, because "
        "DET has little history of human use and no established toxic-dose "
        "data."),
    "root/DMT.json::generalRisks": (
        "Effects may be unpredictable and adverse reactions increase with "
        "the dose. DMT is described as a potent psychedelic, and harm "
        "reduction is recommended."),
    "root/DMT.json::saferUse[0]": (
        "Starting low is advised, because of differences in weight, "
        "tolerance, metabolism and individual sensitivity, and harm "
        "reduction and independent research are recommended."),
    "root/DOB.json::saferUse[0]": (
        "Harm reduction is strongly recommended, because of the sensitive "
        "dose-response and the exceptionally long, unpredictable duration."),
    "root/DOC.json::generalRisks": (
        "Only user-reported dose information is given, and checking other "
        "sources is advised. Effects can vary between individuals."),
    "root/DOI.json::saferUse[0]": (
        "Adequate preparation and harm reduction are strongly recommended "
        "for this potent, dose-sensitive, long-lasting psychedelic."),
    "root/DOM.json::saferUse[0]": (
        "Starting low is advised, because of individual differences, and "
        "harm reduction is recommended for this long-acting psychedelic."),
    "root/DPT.json::saferUse[0]": (
        "Harm reduction is recommended, and the user and resource dose "
        "information should be cross-checked with other sources."),
    "root/FXE.json::toxicities[0]": (
        "The information is not fully approved and explicitly warns that "
        "toxicity and other risk information may be inaccurate, so no "
        "structured toxicology claims are drawn from this provisional page."),
    "root/FXE.json::dosageRemark": (
        "A dose table is present, but it is explicitly stated to be not "
        "fully approved and possibly inaccurate, especially about dosage and "
        "duration. The numbers are not imported into structured ROA data; "
        "only this source-quality warning is kept pending independent "
        "corroboration."),
    "root/GBL.json::toxicities[0]": (
        "GBL is identified as a GHB prodrug, and combining GABAergic "
        "depressants with opioids, benzodiazepines, barbiturates, "
        "gabapentinoids, thienodiazepines or alcohol is warned against as "
        "potentially causing a fatal overdose. A stimulant comedown is "
        "specifically warned about, as it can leave a depressant effect "
        "overwhelming the user and causing respiratory arrest."),
    "root/GBL.json::saferUse[0]": (
        "Combining GBL or GHB with other depressants is strongly advised "
        "against, especially at moderate to heavy doses, because of the risk "
        "of a fatal overdose."),
    "root/GHB.json::saferUse[0]": (
        "Never combining GHB with alcohol, opioids, benzodiazepines or other "
        "depressants is strongly advised, because of the risk of a fatal "
        "overdose."),
    "root/HXE.json::longtermRisks": (
        "No established long-term findings are available, and the history of "
        "human use is short with very limited data on pharmacology, "
        "metabolism and toxicity."),
    "root/IHCH-7113.json::effectsSummary": (
        "The effects index is based on anecdotal user reports and "
        "contributor analysis and should be treated skeptically. Effects may "
        "be unpredictable, and adverse effects are more likely at higher "
        "doses."),
    "root/IHCH-7113.json::generalRisks": (
        "Subjective effects are anecdotal and may be unpredictable, and "
        "adverse effects become more likely at higher doses. Strong and heavy "
        "dose ranges remain undetermined."),
    "root/IHCH-7113.json::saferUse[1]": (
        "Strong and heavy dose bands are left undetermined, and those ranges "
        "are not inferred."),
    "root/Loperamide.json::generalRisks": (
        "High-dose or otherwise supratherapeutic use has been associated with "
        "QT and QRS prolongation, ventricular arrhythmias, syncope, cardiac "
        "arrest and death. Opioid-type respiratory depression may also occur. "
        "The toxicity information is explicitly marked as incomplete and "
        "potentially dangerous, so its details should be treated with "
        "caution."),
    "root/NM-2-AI.json::longtermRisks": (
        "Pharmacology and toxicity information is very limited, and there "
        "are no established long-term findings."),
    "root/NM-2-AI.json::saferUse[0]": (
        "Harm reduction is strongly recommended, because the data is sparse."),
    "root/O-PCE.json::saferUse[0]": (
        "Extreme caution is urged, along with avoiding frequent redosing and "
        "using harm-reduction practices, because the human evidence is "
        "sparse and overdose can occur."),
    "root/PCE.json::generalRisks": (
        "High-dose or multi-day use is strongly discouraged, and severe "
        "doses are warned about as possibly causing confusion and mania."),
    "root/PCE.json::longtermRisks": (
        "Repeated multi-day use is advised against, and no reliable "
        "quantified long-term risk estimate is provided."),
    "root/PCE.json::saferUse[0]": (
        "High-dose or consecutive multi-day use is strongly discouraged, and "
        "the dose tables are educational user and resource material."),
    "root/PCP.json::longtermRisks": (
        "Consecutive multi-day use is discouraged, and reliable quantitative "
        "long-term risk estimates are not provided."),
    "root/PCP.json::saferUse[0]": (
        "High-dose or consecutive multi-day use is strongly discouraged, and "
        "the dose data are user and resource educational material."),
    "root/PMA.json::generalRisks": (
        "PMA may have weak or easily missed subjective effects, which "
        "encourages redosing, and it can cause dangerous elevation of "
        "temperature and blood pressure, abnormal heart rhythm, "
        "dehydration, seizures and death. It may occur as an adulterant in "
        "MDMA."),
    "root/PMA.json::saferUse[0]": (
        "Avoiding PMA is strongly advised, and reagent testing MDMA samples "
        "is recommended, because PMA may occur as an adulterant."),
    "root/PMA.json::saferUse[1]": (
        "No numeric duration and no numeric strong-dose range are given, and "
        "neither is inferred."),
    "root/PRO-LAD.json::longtermRisks": (
        "Pharmacology, metabolism and toxicity information is very limited, "
        "and there are no established long-term findings in humans."),
    "root/PRO-LAD.json::saferUse[0]": (
        "Harm reduction is strongly recommended, because the human evidence "
        "is limited."),
    "root/SAM-e.json::effectsSummary": (
        "Subjective effects are based on user reports and contributor "
        "analysis and should be treated skeptically; higher doses may "
        "increase adverse effects."),
    "root/SAM-e.json::generalRisks": (
        "Dose information is presented as educational user and resource "
        "data, and independent cross-checking is advised; the "
        "subjective-effect material is anecdotal."),
    "root/SR-17018.json::effectsSummary": (
        "The information is an informal, non-clinical discussion of "
        "SR-17018 for opioid withdrawal and tolerance management, not a "
        "validated medical source. It contains self-experiment claims and a "
        "reported death after reduced opioid tolerance, and no dosing "
        "protocol is transcribed."),
    "root/STS-135.json::toxicities[0]": (
        "There is little human toxicology information, the exact toxic dose "
        "is listed as unknown, and the dose information comes from user and "
        "resource reports."),
    "root/STS-135.json::saferUse[0]": (
        "The dose data are educational and should be cross-checked; the exact "
        "toxic dose is unknown."),
    "root/THJ-018.json::saferUse[0]": (
        "The dose data are educational user and resource material, not a "
        "recommendation, and the exact toxic dose is unknown."),
    "root/THJ-2201.json::saferUse[0]": (
        "The dose data are educational user and resource material, not a "
        "recommendation, and the exact toxic dose is unknown."),
    "root/U-47700.json::saferUse[0]": (
        "Harm reduction and extreme caution are strongly urged, and "
        "combining U-47700 with other substances is warned against, "
        "especially at moderate to heavy levels."),
    "root/U-47700.json::saferUse[2]": (
        "The risk of overdose rises after a break in use, as tolerance "
        "falls."),
    "root/\u03b1MT.json::toxicities[0]": (
        "Data on the pharmacology, metabolism and toxicity of alpha-MT is "
        "limited and the history of non-medical human use is short. A few "
        "unverified hospitalisation reports above 60 mg are cited, along with "
        "22 deaths associated with alpha-MT in England and Wales from 2012 "
        "to early 2015; these are reported associations, not proof of "
        "causation."),
    "root/\u03b1MT.json::effectsSummary": (
        "Dose and duration for alpha-MT come from user and resource "
        "material; data on the pharmacology, metabolism and toxicity is "
        "limited and the history of non-medical human use is short."),
    "root/\u03b1MT.json::generalRisks": (
        "Harm reduction is recommended, and adverse effects may become more "
        "likely as the dose increases; subjective reports are anecdotal and "
        "unpredictable."),
    "root/\u03b1MT.json::saferUse[0]": (
        "Harm reduction is strongly recommended, because the human safety "
        "evidence is limited."),
    "root/\u03b2k-2C-B.json::toxicities[0]": (
        "Recreational toxicity and long-term health effects have not been "
        "studied, and the exact toxic dose is unknown."),
    "root/\u03b2k-2C-B.json::generalRisks": (
        "No negative health effects have been clearly attributed in the "
        "infrequent low-to-moderate anecdotal reports, but the conclusions "
        "are uncertain and the toxicity is unstudied."),
    "root/\u03b2k-2C-B.json::saferUse[0]": (
        "Caution and harm reduction are recommended, because the history of "
        "human use is short and the toxicity is unstudied; occasional "
        "low and moderate reports are not safety evidence."),
}


def apply_text_edits():
    """Apply the by-text replacements everywhere they occur."""
    count = 0
    for p in sorted(ROOT.glob("*/*.json")):
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
        for old, new in BY_TEXT.items():
            old_item = json.dumps(old, ensure_ascii=False)
            new_item = json.dumps(new, ensure_ascii=False)
            if old_item in out:
                out = out.replace(old_item, new_item)
                touched.append(".")
        if not touched:
            continue
        try:
            json.loads(out)
        except Exception as exc:
            print(f"SKIP {p}: invalid JSON ({exc})")
            continue
        blob = out.encode("utf-8")
        if had_bom:
            blob = b"\xef\xbb\xbf" + blob
        p.write_bytes(blob)
        count += 1
        print(f"  {p.parent.name}/{p.stem}  (by text)")
    return count


def main():
    changed = 0
    skipped = 0
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
        old = v[int(idx)] if (isinstance(v, list) and idx) else v
        if not isinstance(old, str):
            print(f"  {rel} {where}: not a string, skipped")
            skipped += 1
            continue
        if old == new:
            print(f"  {rel} {where}: already matches")
            continue
        old_item = json.dumps(old, ensure_ascii=False)
        new_item = json.dumps(new, ensure_ascii=False)
        if old_item not in text:
            print(f"  {rel} {where}: current text differs from the table, skipped")
            skipped += 1
            continue
        out = text.replace(old_item, new_item, 1)
        try:
            json.loads(out)
        except Exception as exc:
            print(f"SKIP {rel}: invalid JSON ({exc})")
            skipped += 1
            continue
        blob = out.encode("utf-8")
        if had_bom:
            blob = b"\xef\xbb\xbf" + blob
        p.write_bytes(blob)
        changed += 1
        print(f"  {rel} {where}")

    by_text = apply_text_edits()
    print(f"\nchanged {changed} by file, {by_text} by text, skipped {skipped}")


if __name__ == "__main__":
    main()
