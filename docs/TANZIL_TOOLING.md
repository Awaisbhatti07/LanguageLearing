# Tanzil-only source tooling

User authorization: 2026-10-27. This is a source-candidate tool, outside the Android application. M4/M5 qualified-review and physical-audio gates remain pending; Quranic app import, morphology, meanings and learning screens are not approved by this tool.

`tools/tanzil_source.py` uses Python's standard library, without installing dependencies. It downloads only the evaluated official Tanzil Uthmani v1.1 XML selection: pause marks/sajdah/tatweel enabled, rub-el-hizb/alternate sequential tanween disabled. It captures the official download page and license page alongside the source. HTTPS redirects are restricted to the exact Tanzil host; sizes, encoding, release, form options, source structure and usage terms are checked. A changed release or usage terms requires review; no fallback/mirror is selected.

## Commands

Review [official terms](https://tanzil.net/docs/text_license) and the [source evaluation](QURANIC_SOURCE_EVALUATION.md), then explicitly acquire:

```sh
PYTHONDONTWRITEBYTECODE=1 python3 tools/tanzil_source.py acquire --accept-tanzil-terms
```

The tool prints a new ignored `.local/quranic/tanzil/<timestamp>-<hash>/` directory and its manifest SHA-256. Retain that pin separately from the candidate. Existing directories are never replaced. An interrupted/invalid candidate may retain raw files but cannot pass offline verification without a complete matching manifest. Preserve incomplete directories for diagnosis; do not promote them by editing receipts.

Offline verification is read-only, performs no network requests and requires the separately retained pin:

```sh
PYTHONDONTWRITEBYTECODE=1 python3 tools/tanzil_source.py verify <candidate-directory> --expected-manifest-sha256 <separately-retained-pin>
```

Do not derive the expected pin from the candidate being checked. The recorded real acquisition's directory/pin are in [the Git-tracked receipt](evidence/m6-tanzil-2026-10-27/acquisition-receipt.json). If local data is lost, another acquisition creates a new candidate; it does not recreate or silently replace the previous receipt.

Focused offline tests, under existing routine project-unit approval:

```sh
PYTHONDONTWRITEBYTECODE=1 python3 -m unittest discover -s tools/tests -v
```

All fixtures are synthetic and nonreligious. No Android build/emulator run is needed when only this tool changes.

## Candidate files and guarantees

- `tanzil-uthmani.xml`: exactly the downloaded bytes, including its full embedded notice.
- `download.html` and `text-license.html`: exact captured page bytes and HTTP provenance in the manifest.
- `NOTICE.txt`: complete embedded XML-comment notice, preserving its comment decoration.
- `LICENSE_PAGE_NOTICE.txt`: complete decoded notice block from the captured license page, with whitespace retained.
- `manifest.json`: source version/options, UTC acquisition time, requested/final URLs, file sizes/hashes, structural observations and explicit pending gates. Written last; its mere presence does not establish integrity.

The live XML and license page use different copyright years/descriptive wording. Both notices are retained separately; only source comment decoration and whitespace are ignored when comparing the entire usage-terms text. Punctuation or additional usage terms are not ignored. The raw snapshots are never rewritten to reconcile notices.

Strict XML checks reject DTD/entity declarations, unexpected processing instructions, unsupported structures, duplicate/gapped/out-of-order references, empty text, invalid UTF-8 and text fields that would undergo literal attribute-whitespace normalization. Standard XML entity decoding is used; original logical code points, harakat, tatweel and separate chapter-opening fields are retained. No source text is printed. No opening text is appended or counted as a new ayah.

A matching manifest pin detects changes relative to that saved acquisition. It does **not** prove source authenticity, independently check the full reference inventory or grant religious/linguistic approval. Contiguous references cannot detect a well-formed missing final record without an independently checked inventory. Observed chapter/ayah/opening totals and a length-prefixed decoded-field digest are diagnostic, not word frequencies or rankings. The acquisition tool always leaves app import unapproved and its captured independent-reference/qualified-review state pending. The separate report below records the later numeric check without rewriting that historical manifest.

Raw files/notices remain ignored and outside app assets. Only code, synthetic tests, non-text receipts and documentation are committed. Corpus data, translations, recordings, roots/lemmas, meanings and learner databases are untouched.

## Independent numbered-reference comparison

The user separately approved this verification scope on 2026-10-27. `tools/tanzil_references.py` is a read-only, offline checker against the separately published [King Fahd Complex Hafs archive](https://download.qurancomplex.gov.sa/resources_dev/kfgqpc_hafs_v30.zip). Its checksum is pinned from the [official developer page](https://qurancomplex.gov.sa/quran-dev/), not inferred from the Tanzil candidate. Keep the reference archive under ignored `.local` storage; app redistribution is not approved.

The actual saved comparison can be reproduced without another download:

```sh
PYTHONDONTWRITEBYTECODE=1 python3 tools/tanzil_references.py .local/quranic/tanzil/20261027T062636Z-8c5aeae20363a98f --expected-manifest-sha256 8ddf2ddedd8ea8ded0c7406266a72be7c1c117e545d7ef9670d945a1fac76cd4 --reference-archive .local/quranic/references/20261027T065235Z-kfgqpc-hafs-227e6b1564d9/reference.zip
```

The tool checks the old candidate pin and the fixed published archive SHA-256 before reading only the expected JSON member in memory. It validates numeric reference order/uniqueness/contiguity and compares every actual chapter/ayah identity. Missing/unexpected pairs are reported and cause failure, including well-formed final truncation or wrong chapter boundaries at equal totals. It never extracts archive files, installs fonts, calls a network service, rewrites a manifest or imports app content. Changed publisher artifacts require explicit source/pin review.

Focused new unit checks:

```sh
PYTHONDONTWRITEBYTECODE=1 python3 -m unittest discover -s tools/tests -p test_tanzil_references.py -v
```

[Actual report/method/limits](evidence/m6-reference-2026-10-27/README.md): 13 tests and the actual 6,236-pair/114-chapter comparison passed; no missing/unexpected references. The original candidate's pending inventory state is a historical acquisition receipt and remains unchanged. The new report records this later numeric check without promoting text/opening equivalence, linguistic/religious review, meanings/morphology, redistribution or app-import gates. M4/M5 review remains pending.

## Strict source Unicode difference audit

Continue source-only verification with `tools/tanzil_text_audit.py`, using the same already-pinned artifacts. No download or source write is performed:

```sh
PYTHONDONTWRITEBYTECODE=1 python3 tools/tanzil_text_audit.py .local/quranic/tanzil/20261027T062636Z-8c5aeae20363a98f --expected-manifest-sha256 8ddf2ddedd8ea8ded0c7406266a72be7c1c117e545d7ef9670d945a1fac76cd4 --reference-archive .local/quranic/references/20261027T065235Z-kfgqpc-hafs-227e6b1564d9/reference.zip
```

The command checks both pins/reference sets first, then compares decoded Unicode fields exactly. It reports the full raw-field result and a separate body comparison after validating/excluding only the reference's one U+0020 separator, U+06DD and matching Arabic-Indic ayah number suffix. No other whitespace, code points, harakat, text order or opening fields are changed. Exit 0 means exact raw equality, exit 1 means differences found, and exit 2 means an integrity/structure failure. None grants religious review or app-import approval.

[Actual diagnostic report](evidence/m6-text-audit-2026-10-27/README.md): all 6,236 numbered pairs are present; raw fields all differ; trailer-excluded bodies have 64 exact matches and 6,172 differences. All 53 tooling tests passed. The report uses bounded code-point edits, offsets and hashes without full ayah strings. The 112 separate Tanzil opening fields remain uncompared. These findings need qualified source/annotation assessment; no automatic Unicode equivalence or correction rule is approved. M4/M5 review and Quranic app import stay pending.

## Private complete edit queue

`tools/tanzil_review_queue.py` exports complete code-point edit lists only under ignored project `.local` storage, without changing either source. Example recorded invocation:

```sh
PYTHONDONTWRITEBYTECODE=1 python3 tools/tanzil_review_queue.py .local/quranic/tanzil/20261027T062636Z-8c5aeae20363a98f --expected-manifest-sha256 8ddf2ddedd8ea8ded0c7406266a72be7c1c117e545d7ef9670d945a1fac76cd4 --reference-archive .local/quranic/references/20261027T065235Z-kfgqpc-hafs-227e6b1564d9/reference.zip --output-directory .local/quranic/reviews/20261027-source-differences-v1
```

Existing output directories are rejected. Each edit record must exactly reconstruct its reference comparison body and match both field hashes; output bytes are hashed and re-read before the completion summary. Source-only Unicode grouping and short-pattern counts are mechanical, not approved replacements. Runtime/UCD version and alignment limits are recorded. Exit 0 means completed diagnostic export, never source equivalence or religious approval. [Actual bounded summary and limits](evidence/m6-review-queue-2026-10-27/README.md): 6,172 records/60,917 edits, 66 tests passed. Full queue stays private; app import and reviews remain pending.
