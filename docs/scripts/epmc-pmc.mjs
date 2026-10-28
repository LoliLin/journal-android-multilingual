// Fetch the OA full text of one PMC article and print every paragraph that
// mentions a named drug, so the metabolism statements can be quoted exactly.
//
// Usage: node epmc-pmc.mjs <PMCID> [drug1 drug2 ...]
import { writeFileSync } from "node:fs";

const UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64)";
const pmcid = process.argv[2];
const drugs = process.argv.slice(3).map((d) => d.toLowerCase());

const r = await fetch(
  `https://www.ebi.ac.uk/europepmc/webservices/rest/${pmcid}/fullTextXML`,
  { headers: { "User-Agent": UA, Accept: "application/xml" }, signal: AbortSignal.timeout(45000) }
);
if (!r.ok) throw new Error(`HTTP ${r.status}`);
const xml = await r.text();

const paras = [...xml.matchAll(/<p[^>]*>([\s\S]*?)<\/p>/g)].map((m) =>
  m[1].replace(/<[^>]+>/g, " ").replace(/&[a-z#0-9]+;/gi, " ").replace(/\s+/g, " ").trim());

const wanted = drugs.length ? drugs : null;
const keep = paras.filter((p) => {
  if (!wanted) return true;
  const low = p.toLowerCase();
  return wanted.some((d) => low.includes(d));
});

const out = keep.map((p) => `- ${p}`).join("\n");
writeFileSync(`pmc-${pmcid}.txt`, out, "utf8");
console.error(`wrote pmc-${pmcid}.txt (${keep.length} paragraphs)`);
