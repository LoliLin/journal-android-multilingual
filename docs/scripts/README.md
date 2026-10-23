# Substance safety tooling

Helper scripts for filling in the safety fields of the substance records in
`app/src/main/assets/substances`. Each one does one job; the pipeline for a
single substance is: find a source, extract the relevant section, write the
root record and the `zh_cn` overlay, register the source, verify.

## Sources

### `emc-fetch.mjs` — UK eMC product information

```bash
node tools/emc-fetch.mjs drugs.txt emc-out.txt 3
```

Searches medicines.org.uk for each drug name, follows the matching product
pages and writes the product name plus SmPC section 5.2 for each hit.

The eMC search endpoint returns server-rendered HTML, so the product links can
be parsed directly. Always check the section 1 product name before using a
section: a wrong product id returns a different drug's SmPC. Combination
products (for example Anadin Extra, which is aspirin, paracetamol and
caffeine) describe the mixture and must not be used for a single-ingredient
record.

Note that DailyMed, FDA and EMA are unreachable from this machine (TLS or
anti-bot blocks), so eMC is currently the only product-information source that
responds.

### `epmc-batch.mjs` — peer-reviewed literature

```bash
node tools/epmc-batch.mjs drugs.txt epmc-out 3
```

Searches EuropePMC for papers mentioning each drug together with metabolic
terms, and writes the matching paragraphs from the abstract or from the
open-access full text.

EuropePMC's `FULL_TEXT:` field prefix is not accepted by the search API; plain
boolean terms match across title, abstract and full text.

The paragraphs returned often describe a *different* compound that the paper
mentions in passing, so every excerpt must be checked against the drug before
it is written into a record.

### `epmc-pmc.mjs` — one article's full text

```bash
node tools/epmc-pmc.mjs PMC6461066 acetylfentanyl furanylfentanyl
```

Fetches the JATS XML of one open-access article and prints every paragraph
mentioning the given drugs.

## Writing

### `verify-batch.py` — integrity gate

```bash
python tools/verify-batch.py Diazepam Zolpidem
```

Checks that no top-level key was lost, that no placeholder text survived, that
the file has no BOM and no mixed line endings, and that the text contains no
Hangul characters.

The Hangul check must use the real blocks (jamo U+1100-U+11FF and
U+3130-U+318F, syllables U+AC00-U+D7A3, extended-A U+A960-U+A97F, extended-B
U+D7B0-U+D7FF). A range of U+3131..U+D7A3 numerically spans the CJK
ideographs and flags every Chinese character as mojibake.

### `register-ledger.py` — evidence ledger

```bash
python tools/register-ledger.py "Diazepam|https://www.medicines.org.uk/emc/product/101913/smpc"
python tools/register-ledger.py --lit "MDPV|https://europepmc.org/article/PMC/6212520"
```

Inserts into `docs/substance-metabolism-sources.json` with a minimal diff that
keeps the plain-ASCII sort order of `reviewed` intact, and removes the name
from `notYetVerified`. `--lit` marks a peer-reviewed literature source;
the default is `product-information`.

### `gen-candidates.py` / `gen-rc-candidates.py` — gap lists

Write the names that still need work to a file: `gen-candidates.py` for
prescription-style records with a plausible SmPC, `gen-rc-candidates.py` for
research-chemical and tentative records, where literature is the only route.

### `check-names.py` / `show-hits.py` / `summarize.py` — inspection

```bash
python tools/check-names.py Diazepam Zolpidem     # is it a gap, and is zh_cn present?
python tools/show-hits.py emc-out.txt Diazepam    # print one drug's fetched section
python tools/summarize.py emc-out.txt             # which drugs had hits, and what are they?
```

### `scan_jargon.py` / `scan-changed.py` — wording check

```bash
python tools/scan_jargon.py                       # every record: boilerplate and jargon
python tools/scan-changed.py                      # only the fields this branch changed
```

The first flags fields that open by talking about the source ("Evidence is
from ...") or that use clinical vocabulary. The second compares against the
merge base, so it only reports what this branch actually introduced.

The boilerplate patterns are matched with `re.match`, not `re.search`: `^` in
a searched pattern still matches anywhere, which made the scan report every
mention of a wiki name rather than only the ones opening a field.

### `check-keys.py` — no field lost

```bash
python tools/check-keys.py
```

Compares every changed record against the baseline and reports dropped
top-level keys, fields that were emptied, and lists that got shorter. Run
this before committing a bulk edit.

### `rewrite-src.py` — the hand-written rewrite table

```bash
python tools/rewrite-src.py
```

Holds the plain-language rewrites for the fields whose wording had to be
judged one by one. The table is keyed by `record::field` or
`record::field[index]`. Edit the table and re-run to apply it; the script
refuses to touch a record whose current text does not match what it
expects, so a stale table cannot silently overwrite something else.

It has two sections. `BY_TEXT` is keyed by the current wording and applies
wherever that wording appears, which is what the repeated sentences across
records needed. `EDITS` is keyed by record and field.

### `rewrite-zh.py` — the Chinese overlays

```bash
python tools/rewrite-zh.py
```

The same pass for the Chinese records, done in Chinese rather than by
translating the English. The source name and its 的 go, and the sentence
starts with the substance. Where the source name carried a real
limitation -- that the record is for a prescription product or a
particular formulation -- the limitation moves to the end of the sentence
as a note.

### `de-jargon.py` — the clinical vocabulary

```bash
python tools/de-jargon.py [--dry]
```

Replaces clearance, volume of distribution, half-life, bioavailability,
pharmacokinetics and first-pass effect with plain wording. The names of the
metabolic reactions -- hydroxylation, demethylation, glucuronidation,
dealkylation, conjugation, sulfation -- are left alone: that is how a
pathway is written down, and there is no everyday word for them.

The replacements are phrases, not single words, so the sentence stays
grammatical. Only the fields this branch changed are rewritten.

### `check-broken.py` — did a substitution break a sentence?

```bash
python tools/check-broken.py
```

Replacing a noun with a clause does not always fit the sentence it lands
in. "the volume of distribution is 3.3-6.8 l/kg" becomes "the how much it
spreads through the body is 3.3-6.8 l/kg". This looks for the shapes that
come out of such a substitution, so they can be rewritten by hand.

Run it after `de-jargon.py`. It reports the whole tree, not just the
branch, because a substitution can land in a record the branch never
touched.

## Conventions

- `metabolism` goes in the language overlay, `metabolismSources` stays in the
  root record. The root record holds the English text.
- A field with no supporting evidence stays empty. Do not infer a pathway from
  a related compound.
- Scope the text to the product the label covers: the formulation, route and
  population the label describes.
- Root records use CRLF and no BOM. Write through the file tools, not through
  a shell pipeline, so non-ASCII text survives.
- Write plain language. The provenance lives in `metabolismSources` and in the
  evidence ledger, so the text does not need to say where it came from. Keep
  enzyme names, metabolite names, numbers and units -- those are what a reader
  looks up. Drop the clinical framing around them: "volume of distribution"
  is "how much the drug spreads through the body", "clearance" is "how fast
  the body clears it", and "metabolised" is "broken down".
- When a field says nothing was found, say so plainly ("nothing is known
  about how this is broken down in people") rather than describing the
  source.
- A source reference that carries a real limitation stays, but as a note at
  the end of the sentence: "this record is for a prescription product" is
  information; "the label says" is not.
