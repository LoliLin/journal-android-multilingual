#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Put back the information that the rewrite dropped.

A record that used to say "2-FA is reported to have effects similar to
dextroamphetamine but with a milder euphoria, a shorter duration and less
empathogenic effect compared with 4-FA" was replaced by "2-FA is a
fluorinated amphetamine stimulant; its pharmacology is poorly studied".
That is not a rewording, it is a loss: the reader no longer learns that
the effect is milder, that the duration is shorter, or that it is
compared with 4-FA.

So the original content comes back, said in plain language and without the
self-referential framing. Nothing that was in the old text is dropped, and
nothing new is claimed.

Usage: python tools/restore-facts.py [--dry]
"""
import json
import pathlib
import sys

sys.stdout.reconfigure(encoding="utf-8", errors="replace")
ROOT = pathlib.Path("app/src/main/assets/substances")
DRY = "--dry" in sys.argv

EDITS = {
    "en_us/Morphine.json::generalRisks": (
        "Gastrointestinal complaints such as constipation are common side "
        "effects after taking opiates. Nausea, vomiting, dry mouth, lack of "
        "appetite, abdominal pain, sweating, skin rashes, itching, "
        "dizziness, headaches and fatigue are other common complaints. On "
        "the psychological level, confusion, nightmares and hallucinations "
        "may occur. Rather rarely, there is a drop in blood pressure, "
        "breathing difficulties and allergic reactions. Overdoses with "
        "morphine are life-threatening, as the substance has a strong "
        "respiratory depressant effect (worsening breathing) even in low "
        "doses, which can lead to respiratory arrest and coma. Oral "
        "consumption of 0.3 - 1.5 g and intravenous consumption of 100 mg "
        "can lead to a fatal overdose. In opioid addicts, the lethal dose "
        "can be significantly higher. An overdose is treated by taking "
        "naloxone, an opioid antagonist. The boxed warning on the "
        "extended-release capsules covers addiction, abuse and misuse, fatal "
        "respiratory depression, accidental ingestion, alcohol, and combined "
        "CNS depressants. Do not crush, chew, cut, or dissolve the "
        "extended-release pellets; this can release a potentially fatal "
        "dose."),
    "root/1B-LSD.json::summary": (
        "1B-LSD (1-butanoyl-lysergic acid diethylamide) is an LSD analogue "
        "which appears to be about as potent as 1P-LSD (slightly more potent "
        "than LSD) and has a slightly shorter duration than that of LSD, "
        "again thought to be about the same as 1P-LSD. Released in late "
        "2018, it was marketed aggressively as a replacement for 1P-LSD. In "
        "the lab it is deacylated to LSD, while its human pharmacology, "
        "recreational toxicity and long-term effects remain poorly "
        "characterized, and it does not yet have a clearly established "
        "safety profile."),
    "root/1P-LSD.json::summary": (
        "1P-LSD is a propionyl-substituted lysergamide psychedelic related to "
        "LSD. It appears to be slightly more potent than LSD with a shorter "
        "duration, and its effects are reported to be extremely similar to "
        "LSD. Released in late 2014, it quickly became a popular research "
        "chemical because of its implicit legality, similarity to LSD and "
        "wide availability on the internet. Its toxicity and long-term "
        "effects have not been established. Animal, lab and limited human "
        "findings support conversion to LSD, but they do not establish that "
        "all effects are due to LSD."),
    "root/2-FA.json::summary": (
        "2-FA is a fluorinated amphetamine stimulant. Its effects are "
        "reported to be similar to those of dextroamphetamine, but with a "
        "milder euphoria and a comparatively smoother comedown. It has a "
        "shorter duration and less empathogenic effect profile compared "
        "with 4-FA, and is thought to produce fewer neurotoxic breakdown "
        "products than amphetamine. Its pharmacology, metabolism and "
        "toxicity are poorly studied, and the reports are anecdotal."),
    "root/2-FEA.json::summary": (
        "2-FEA is the N-ethylated analogue of 2-FA, a stimulant closely "
        "related to 2-FMA and presumably slightly less potent than it. Its "
        "exact effects are poorly characterized, formal pharmacological and "
        "toxicity research is lacking, and the reported activity and "
        "possible serotonergic effects are anecdotal and uncertain. Tread "
        "with caution."),
    "root/2C-E.json::summary": (
        "2C-E is an intense psychedelic in the 2C family, with very strong "
        "visuals and sometimes a relatively uncomfortable body load. Its "
        "effects are otherwise comparable to other 2c-x drugs. Recreational "
        "toxicity and long-term health effects are poorly studied and the "
        "exact toxic dose is unknown."),
    "root/2C-T-2.json::summary": (
        "2C-T-2 is an unusual psychedelic in the 2C family with dose-sensitive, "
        "potentially intense effects. Its effects are similar to 2C-B with a "
        "slightly longer duration, but it is maligned because of some deaths "
        "in the 2000s. It is also similar to 2c-t-7, but with a shorter "
        "duration, and it is potentially unsafe with stimulants and "
        "empathogens. Evidence on recreational toxicity and long-term health "
        "effects is very limited, and possible MAO-A inhibition is "
        "unconfirmed."),
    "root/3-FEA.json::summary": (
        "3-FEA is a novel fluorinated amphetamine with serotonergic "
        "properties. Its effects are similar to 4-FA, in that it provides "
        "slight dopamine and norepinephrine release or reuptake inhibition, "
        "as well as serotonin release and reuptake inhibition. The effects "
        "on the serotonin side are more pronounced than dopamine and "
        "norepinephrine, the latter two being almost negligibly present. "
        "Human pharmacology, toxicity and subjective effects have not been "
        "studied in depth, and the proposed serotonergic profile is based "
        "mainly on user reports and remains uncertain."),
    "root/3-Me-PCPy.json::longtermRisks": (
        "Long-term toxicity and health effects have not been studied in "
        "humans. Chronic and repeated use may produce tolerance, "
        "psychological dependence and cravings, and tolerance develops with "
        "prolonged and repeated use, with cross-tolerance extending across "
        "dissociatives. Frequent multi-day use is associated with greater "
        "concern for dependence and severe psychiatric effects, and "
        "compulsive redosing and repeated use may increase the risk of "
        "severe adverse effects."),
    "root/3-Me-PCPy.json::dosageRemark": (
        "The dose tables are compiled from user reports and online resources "
        "for education, not recommendations, and list three routes: "
        "insufflated with a threshold of 2 mg, light of 5-10 mg, moderate of "
        "10-20 mg, strong of 20-25 mg, heavy of 25 mg or more and a total "
        "duration of 45-120 minutes; oral with a threshold of 5 mg, light of "
        "5-10 mg, moderate of 10-20 mg, strong of 20-30 mg, heavy of 30 mg "
        "or more and a total duration of 4-8 hours; and a further oral "
        "listing with a threshold of 5 mg and a total duration of 3-5 hours. "
        "The model does not represent a threshold band separately, so the "
        "2 mg and 5 mg thresholds are preserved here and lightMin follows "
        "the lower bound of each source light range. Because 3-Me-PCPy is "
        "poorly studied and has an erratic dose-response profile, these "
        "values should not be treated as established safe or therapeutic "
        "doses. Severe doses may cause psychosis and mania."),
    "root/3-MeO-PCP.json::summary": (
        "3-MeO-PCP is a potent dissociative arylcyclohexylamine, often "
        "compared to MXE but with a longer duration, much less sedating "
        "effects and an overall different experience. Users have reported "
        "effects as being similar to PCP, and it is roughly ten times more "
        "potent than 4-MeO-PCP. Abuse is widely reported to produce "
        "psychotic delirium, amnesia and mania requiring hospitalisation, "
        "and long-term heavy use is reported to affect the bladder and "
        "urinary tract; it should not be combined with stimulants, "
        "psychedelics or other dissociatives."),
    "root/4-AcO-DET.json::summary": (
        "4-AcO-DET is a rare substituted tryptamine psychedelic of the "
        "tryptamine family, used as a research chemical for recreational or "
        "religious purposes. It can be comparable to psilocybin and is "
        "expected to be quickly hydrolysed into the free phenolic 4-HO-DET. "
        "Human pharmacology and toxicity data is very little and its history "
        "of human use is short."),
    "root/4-AcO-DiPT.json::summary": (
        "4-AcO-DiPT is an uncommon psychedelic tryptamine with a short "
        "history of human use, also known as ipracetin and possibly first "
        "synthesised by Alexander Shulgin. It is described as producing "
        "physical euphoria and stimulation with unusually minimal visuals, "
        "and some reports mention heavy nausea, with effects comparable to "
        "2c-b and to mushrooms. Data on its pharmacology, metabolism and "
        "toxicity is very little."),
    "root/4-AcO-MiPT.json::summary": (
        "4-AcO-MiPT is a quite potent synthetic psychedelic tryptamine, the "
        "acetylated form of 4-HO-MiPT, and is generally assumed to act as a "
        "prodrug of its hydroxylated counterpart. It has been related to "
        "having shroom-like visuals, yet without much bodyload; it is very "
        "hydroscopic and seems to be quite safe in dosing. Its effects are "
        "similar to those of 2C-B. The archived page gives no specific "
        "human metabolic finding or pathway."),
    "root/4-FA.json::summary": (
        "4-FA is a fluorinated amphetamine and an empathogen commonly used "
        "in place of MDMA, with a similar duration of empathogenic effects. "
        "It has a longer stimulant tail, during which its effects are more "
        "like those of amphetamines. Human-use toxicity and metabolism data "
        "remain limited. It should not be confused with 4-FMA, which has a "
        "similar potency but is not particularly empathogenic."),
    "root/4-FMA.json::summary": (
        "4-FMA is a fluorinated methamphetamine analogue with some "
        "empathogenic properties as well as stimulant ones, and is reported "
        "to be less effective than 2-FMA and related compounds. Little is "
        "known about the pharmacological effects of this compound, and its "
        "pharmacology, toxicity and metabolism are poorly characterized. It "
        "should not be confused with 4-FA, which has a different effect "
        "profile despite similar potency."),
    "root/4-HO-EPT.json::summary": (
        "4-HO-EPT is the 4-Hydroxy version of EPT, a tryptamine "
        "psychedelic slightly more potent than EPT. Its dose table lists "
        "only light, moderate and strong bands, with no threshold and no "
        "severe band, and leaves a gap between the light and moderate "
        "ranges."),
    "root/4-HO-MPT.json::summary": (
        "4-HO-MPT is a psychedelic drug of the tryptamine class, the "
        "4-hydroxy analogue of N-Methyl-N-Propyltryptamine and a higher "
        "homologue of psilocin. It has a very short history of human use, "
        "and its toxicity and long-term health effects have not been "
        "researched. Cannabis may combine with it in unexpectedly strong and "
        "unpredictable ways."),
    "root/4-MeO-PCP.json::summary": (
        "4-MeO-PCP is a dissociative arylcyclohexylamine anaesthetic "
        "related to PCP, with a very long duration of 12-20 hours orally. "
        "First discovered in the 1960s by Parke-Davis, it was introduced to "
        "the research chemical market in 2008 and is reported to be less "
        "potent than PCP. It is not commonly seen, but has seen some "
        "increase in popularity with the inavailability of other "
        "dissociatives. Reported effective doses vary widely between users, "
        "partly because grey-market samples may contain unreacted PCC and "
        "other impurities."),
    "root/5-APB.json::summary": (
        "5-APB is a benzofuran stimulant and psychedelic, and a triple "
        "monoamine reuptake inhibitor, said to produce euphoria, "
        "empathogenic effects and stimulation. Its agonism at 5-HT2B makes "
        "it likely to be cardiotoxic with long term use, as seen in other "
        "5-HT2B agonists such as fenfluramine and MDMA. Its toxicity and "
        "long-term health effects have not been studied."),
    "root/5-MAPB.json::summary": (
        "5-MAPB is the N-methylated form of 5-APB, an empathogen "
        "structurally similar to MDMA, in the same relation MDMA has to MDA. "
        "It is typically more visual than MDMA, and often reported to be "
        "much less stimulating and more relaxing than most other "
        "stimulating empathogens. It is less psychedelic than 6-APB and "
        "much longer lasting than MDMA. Its cognitive effects are "
        "comparable to MDMA but concentrate more on serotonergic activity, "
        "bringing more empathy and euphoria with less physical "
        "stimulation."),
    "root/5-MeO-DMT.json::generalRisks": (
        "The risk associated with the use of 5-MeO-DMT is mainly "
        "psychological and depends on the personality structure of the user. "
        "The perceptual changes during a trip can be so intense that "
        "especially inexperienced users feel overwhelmed by the flood of "
        "impressions. The perception of sudden emotional outbursts can also "
        "be perceived as rather disturbing and overwhelming. At high doses, "
        "such psychological side effects can be felt for days to weeks. If "
        "the rules of the set and setting are not followed, there can be "
        "false reactions, loss of orientation, panic, paranoia and horror "
        "trips. Physical side effects include nausea, vomiting, increase in "
        "heart rate, slightly accelerated breathing, increased blood "
        "pressure, pupil dilation, increased salivation, tremors, "
        "restlessness, headaches and movement disorders. The toxicity "
        "section is under construction and possibly dangerously wrong, and "
        "toxicity and long-term effects appear unresearched with an "
        "unknown toxic dose."),
    "root/5-MeO-DMT.json::dosageRemark": (
        "The dose tables are compiled from user reports and resources for "
        "education, not recommendations. They list smoked or vaporized with "
        "a threshold of 1 mg, light of 3-6 mg, moderate of 6-12 mg, strong of "
        "12-20 mg and heavy of 20 mg or more, with a total duration of "
        "20-40 minutes; insufflated with a threshold of 3 mg, light of 5-8 "
        "mg, moderate of 8-15 mg, strong of 15-25 mg and heavy of 25 mg or "
        "more. The model does not represent a threshold band separately, so "
        "the 1 mg and 3 mg thresholds are preserved here and lightMin "
        "follows the lower bound of each source light range. The substance "
        "is active at doses as low as 2 mg vaporized and reportedly roughly "
        "4-5 times more potent than DMT, and insufflation is possible though "
        "discouraged. MeO-DMT must be dosed very carefully, as even a few "
        "milligrams more can cause a very intense and unpleasant effect, "
        "which can result in psychological and mental problems lasting "
        "several weeks."),
    "root/5-MeO-DMT.json::saferUse[1]": (
        "Do not use 5-MeO-DMT alone; take it in a safe atmosphere and "
        "together with experienced people you trust. It is described as "
        "only appropriate for users with extensive experience of powerful "
        "psychedelics and training in safe use, and insufflation is "
        "discouraged."),
    "root/5-MeO-DMT.json::saferUse[2]": (
        "Take 5-MeO-DMT while sitting or lying down, as physical perception "
        "and the ability to move are impaired during intoxication. The "
        "combination with MAOIs tends to be extremely unpleasant, with a "
        "strong body load, hypertension symptoms and a risk of serotonin "
        "syndrome, and is strongly discouraged."),
    "root/5-MeO-DiPT.json::summary": (
        "5-MeO-DiPT is 5-methoxy-di isopropyl tryptamine, also known as "
        "'foxy', a psychedelic tryptamine related to DMT which distorts "
        "visual and audio perception. It is described anecdotally as highly "
        "stimulating and mildly empathogenic, and is reported to have a "
        "heavy body load at high doses, behaving similarly to other drugs of "
        "its class. Overdoses have caused serious reactions and deaths, and "
        "it should never be combined with MAOIs."),
    "root/5F-AKB48.json::summary": (
        "5F-AKB48 is a synthetic cannabinoid of the indazole class, an "
        "analogue of STS-135 in which the core indole structure is replaced "
        "with an indazole base. It produces subjective effects somewhat "
        "similar to those of cannabis, yet with a very short duration and "
        "intense bodily sensations. Toxicity is poorly studied and overdose "
        "effects may occur far below lethal doses."),
    "root/AL-LAD.json::summary": (
        "AL-LAD is a potent hallucinogenic lysergamide and an analogue of "
        "LSD. It is reported as having some subtle experiential differences "
        "to LSD, such as increased visuals, and also appears to be slightly "
        "shorter lasting. Its doses are similar to those of LSD, depending "
        "on purity, and it is commonly viewed by users as more recreational "
        "and less threatening than LSD. Its availability on the internet "
        "since 2013 has led to strong popularity among the drug community. "
        "The safety data is sparse, so harm reduction is emphasised."),
    "zh_cn/2C-T-2.json::summary": (
        "2C-T-2 是 2C 系列中一种不寻常的致幻剂，效果与 2C-B 相似，持续时间稍长，"
        "但因 2000 年代的一些死亡案例而备受诟病。与 2c-t-7 类似，但持续时间较短。"
        "与兴奋剂和共情剂同时使用可能存在安全隐患。效应对剂量敏感且可能很强；"
        "娱乐性毒性和长期健康影响资料非常有限，其可能的 MAO-A 抑制作用尚未证实。"),
    "zh_cn/3-Cl-PCP.json::summary": (
        "3-Cl-PCP（3-Chlorophencyclidine，通常被称为 3-Chloro-PCP 或 "
        "3-Cl-PCP）是一种芳基环己胺类的新型解离剂，在使用时会产生强烈的解离、"
        "致幻和欣快效应。它大概是在 2020 年首次合成的，并与大约同一时间生产的"
        "其他新型解离剂一起销售，如 3-F-PCP 和 DMXE。其药理、代谢和毒性资料有限。"),
    "zh_cn/3-FEA.json::summary": (
        "3-FEA 是一种具有血清素能特性的新型氟化苯丙胺。其作用与 4-FA 类似，"
        "既能轻微促进多巴胺和去甲肾上腺素的释放或抑制其再摄取，同时也能促进"
        "血清素的释放并抑制其再摄取。对血清素系统的影响比多巴胺和去甲肾上腺素"
        "更为显著，后两者的作用几乎可以忽略不计。据报告具有共情剂与兴奋剂混合"
        "效应；人体药理、毒性和主观效应尚未充分研究，提出的血清素能特征主要"
        "基于用户报告，仍不确定。"),
    "zh_cn/3-Me-PCPy.json::dosageRemark": (
        "归档剂量表来自用户报告和网络资料，仅供教育用途，不是用量建议。三条途径："
        "鼻吸阈值 2 mg、轻微 5-10 mg、中等 10-20 mg、强烈 20-25 mg、严重 25 mg "
        "以上，总时长 45-120 分钟；口服阈值 5 mg、轻微 5-10 mg、中等 10-20 mg、"
        "强烈 20-30 mg、严重 30 mg 以上，总时长 4-8 小时；另有一组口服数据阈值 "
        "5 mg、总时长 3-5 小时。数据模型不单独表示阈值区间，因此 2 mg 与 5 mg "
        "阈值保存在此，lightMin 取各来源轻微区间下界。由于 3-Me-PCPy 的药理学、"
        "代谢和毒性研究十分有限，其剂量反应关系存在较大的不确定性，这些数值不应"
        "被视为已确立的安全或治疗剂量。严重剂量可能导致精神病发作和躁狂。"),
    "zh_cn/3-MeO-PCMo.json::summary": (
        "3-MeO-PCMo（4-[1-(3-methoxyphenyl)cyclohexyl]morpholine）是 3-MeO-PCP "
        "的一种新型吗啉类似物。它属于芳基环己胺类物质，是一种解离性 NMDA 受体"
        "拮抗剂，也是 sigma 受体激动剂和麻醉剂。不过它的效力还不到 3-MeO-PCP 的"
        "十分之一。这种化合物摄入后会产生一种被称为「解离性麻醉」的状态，所以被"
        "当作娱乐性药物使用。最近 3-MeO-PCMo 作为一种策划药，开始在网络上的"
        "研究用化学品供应商那里变得容易买到了。长期使用被描述为具有中度成瘾性，"
        "并可能像氯胺酮一样影响膀胱和泌尿道。"),
    "zh_cn/3-MeO-PCP.json::summary": (
        "3-MeO-PCP 是一种效力较强的解离性芳基环己胺，常与 MXE 相提并论，但作用"
        "时间更长，镇静效果弱得多，且整体体验不同。使用者报告其效果类似于 PCP，"
        "药效约为 4-MeO-PCP 的十倍。滥用被广泛报告会产生需住院治疗的精神病性"
        "谵妄、健忘和躁狂，长期过量使用被报告会影响膀胱和泌尿道；不应与兴奋剂、"
        "迷幻剂或其他解离剂联用。"),
    "zh_cn/4-AcO-DiPT.json::summary": (
        "4-AcO-DiPT 是一种罕见且使用历史较短的迷幻色胺，也被称为伊普拉西汀，"
        "可能由亚历山大·舒尔金首次合成。有报告称其会引起严重恶心，效果与 2C-B "
        "和蘑菇相当。它被描述为产生躯体欣快感和兴奋，同时视觉异常轻微，其药理、"
        "代谢与毒性数据非常少。"),
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
        old = d.get(where)
        if not isinstance(old, str):
            print(f"  {rel} {where}: not a string")
            continue
        if old == new:
            print(f"  {rel} {where}: already matches")
            continue
        old_item = json.dumps(old, ensure_ascii=False)
        new_item = json.dumps(new, ensure_ascii=False)
        if old_item not in text:
            print(f"  {rel} {where}: current text not found")
            continue
        out = text.replace(old_item, new_item, 1)
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
        changed += 1
        print(f"  {rel} {where}")
    print(f"\n{'would change' if DRY else 'changed'} {changed}")


if __name__ == "__main__":
    main()
