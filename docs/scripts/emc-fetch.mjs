// emc pipeline: search -> product ids -> SmPC page -> name + section 5.2.
// Usage: node emc-fetch.mjs <drugs.txt> <out.txt> [perDrug]
// Writes UTF-8 output to a file (Windows console is GBK).
import { readFileSync, writeFileSync } from "node:fs";

const UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64)";
const buf = [];
const log = (s = "") => buf.push(s);

async function get(url) {
  for (let a = 1; a <= 3; a++) {
    try {
      const r = await fetch(url, { headers: { "User-Agent": UA }, signal: AbortSignal.timeout(30000) });
      if (!r.ok) throw new Error(`HTTP ${r.status}`);
      return await r.text();
    } catch (e) {
      if (a === 3) throw e;
      await new Promise((res) => setTimeout(res, 800 * a));
    }
  }
}

function toText(html) {
  return html
    .replace(/<script[\s\S]*?<\/script>/gi, " ")
    .replace(/<style[\s\S]*?<\/style>/gi, " ")
    .replace(/<[^>]+>/g, " ")
    .replace(/&nbsp;/g, " ").replace(/&amp;/g, "&").replace(/&lt;/g, "<").replace(/&gt;/g, ">")
    .replace(/&#(\d+);/g, (_, d) => String.fromCharCode(+d))
    .replace(/&#x([\da-f]+);/gi, (_, h) => String.fromCharCode(parseInt(h, 16)))
    .replace(/\s+/g, " ");
}

function section52(t) {
  const i = t.search(/5\.2\s*Pharmacokinetic/i);
  if (i < 0) return null;
  const rest = t.slice(i + 10);
  const m = rest.search(/5\.3\s*Preclinical/i);
  return (m > 0 ? rest.slice(0, m) : rest.slice(0, 2200)).trim().slice(0, 2200);
}

function name1(t) {
  const i = t.search(/1\.\s*Name of the medicinal product/i);
  if (i < 0) return "(not found)";
  const seg = t.slice(i, i + 140);
  const m = seg.search(/\d\.\s*Qualitative and quantitative/i);
  return (m > 0 ? seg.slice(0, m) : seg).replace(/^1\.\s*Name of the medicinal product\s*/i, "").trim();
}

const file = process.argv[2];
const out = process.argv[3] || "emc-out.txt";
const drugs = readFileSync(file, "utf8").split("\n").map((s) => s.trim()).filter(Boolean);
const perDrug = Number(process.argv[4] || 4);

for (const drug of drugs) {
  log(`\n##### ${drug}`);
  let ids = [];
  try {
    const html = await get(`https://www.medicines.org.uk/emc/search?q=${encodeURIComponent(drug)}`);
    const m = new Map();
    for (const hit of html.matchAll(/href="(\/emc\/product\/(\d+)[^"]*)"/g)) {
      if (!m.has(hit[2])) m.set(hit[2], hit[1]);
    }
    ids = [...m.keys()];
    log(`  search -> ${ids.length} product ids`);
  } catch (e) {
    log(`  SEARCH FAILED: ${e.message}`);
    continue;
  }
  for (const id of ids.slice(0, perDrug)) {
    try {
      const t = toText(await get(`https://www.medicines.org.uk/emc/product/${id}/smpc`));
      const nm = name1(t);
      const s52 = section52(t);
      log(`\n  -- [${id}] ${nm}`);
      log(s52 ? `     5.2: ${s52}` : "     5.2: (not found)");
    } catch (e) {
      log(`  -- [${id}] FETCH FAILED: ${e.message}`);
    }
  }
}

writeFileSync(out, buf.join("\n"), "utf8");
console.log(`wrote ${out} (${buf.length} lines)`);
