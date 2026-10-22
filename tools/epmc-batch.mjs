// Batch: pull metabolism evidence for many drugs from EuropePMC OA full text.
//
// Usage: node epmc-batch.mjs <drugs.txt> <outdir> [maxPapersPerDrug]
import { readFileSync, writeFileSync, mkdirSync } from "node:fs";

const UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64)";
const MET = /(metaboli[sz]|hydrolys|hydroxylat|demethylat|glucuronid|sulfat|sulphat|conjugat|CYP\d|oxidat|excret|eliminat)/i;

async function get(url, accept) {
  for (let a = 1; a <= 3; a++) {
    try {
      const r = await fetch(url, {
        headers: { "User-Agent": UA, Accept: accept },
        signal: AbortSignal.timeout(40000),
      });
      if (!r.ok) throw new Error(`HTTP ${r.status}`);
      return await r.text();
    } catch (e) {
      if (a === 3) throw e;
      await new Promise((res) => setTimeout(res, 700 * a));
    }
  }
}

const drugs = readFileSync(process.argv[2], "utf8")
  .split("\n").map((s) => s.trim()).filter(Boolean);
const outdir = process.argv[3] || "epmc-full";
const maxPapers = Number(process.argv[4] || 3);
mkdirSync(outdir, { recursive: true });

let done = 0;
for (const drug of drugs) {
  const out = [`##### ${drug}`];
  try {
    const q = encodeURIComponent(
      `"${drug}" AND (CYP OR "metabolized" OR "metabolised" OR "hydrolysed" OR "demethylation" OR "biotransformation")`);
    const j = JSON.parse(await get(
      `https://www.ebi.ac.uk/europepmc/webservices/rest/search?query=${q}&format=json&pageSize=12&resultType=core`,
      "application/json"));
    out.push(`# hits: ${j.hitCount}`);
    let used = 0;
    for (const h of j.resultList?.result || []) {
      if (used >= maxPapers) break;
      // abstract first
      const abs = (h.abstractText || "").replace(/<[^>]+>/g, " ").replace(/\s+/g, " ");
      if (abs && MET.test(abs) && abs.toLowerCase().includes(drug.toLowerCase().split(" ")[0])) {
        out.push(`\n=== ${h.title}\n    ${h.authorString || "?"}`);
        out.push(`    ${h.journalTitle || "?"} ${h.pubYear || ""} | PMID ${h.pmid || "-"} | DOI ${h.doi || "-"}`);
        const sents = abs.split(/(?<=[.!?])\s+(?=[A-Z(])/);
        for (const s of sents.filter((x) => MET.test(x) && x.length < 700).slice(0, 3)) out.push(`    - ${s}`);
        used++;
        continue;
      }
      if (!h.pmcid || h.isOpenAccess !== "Y") continue;
      let xml;
      try {
        xml = await get(`https://www.ebi.ac.uk/europepmc/webservices/rest/${h.pmcid}/fullTextXML`, "application/xml");
      } catch { continue; }
      const paras = [...xml.matchAll(/<p[^>]*>([\s\S]*?)<\/p>/g)].map((m) =>
        m[1].replace(/<[^>]+>/g, " ").replace(/&[a-z#0-9]+;/gi, " ").replace(/\s+/g, " ").trim());
      const key = drug.toLowerCase();
      const hp = paras.filter((p) => p.toLowerCase().includes(key) && MET.test(p) && p.length > 60 && p.length < 1500);
      if (!hp.length) continue;
      used++;
      out.push(`\n=== ${h.title}\n    ${h.authorString || "?"}`);
      out.push(`    ${h.journalTitle || "?"} ${h.pubYear || ""} | PMID ${h.pmid || "-"} | PMCID ${h.pmcid} | DOI ${h.doi || "-"}`);
      out.push(`    https://europepmc.org/article/PMC/${h.pmcid.replace("PMC", "")}`);
      for (const p of hp.slice(0, 5)) out.push(`    - ${p}`);
    }
  } catch (e) {
    out.push(`# FAILED: ${e.message}`);
  }
  writeFileSync(`${outdir}/${drug.replace(/[^a-z0-9]/gi, "_")}.txt`, out.join("\n"), "utf8");
  done++;
  console.error(`[${done}/${drugs.length}] ${drug} -> ${out.length} lines`);
}
