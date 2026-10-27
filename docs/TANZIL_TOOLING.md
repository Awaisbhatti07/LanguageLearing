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

A matching manifest pin detects changes relative to that saved acquisition. It does **not** prove source authenticity, independently check the full reference inventory or grant religious/linguistic approval. Contiguous references cannot detect a well-formed missing final record without an independently checked inventory. Observed chapter/ayah/opening totals and a length-prefixed decoded-field digest are diagnostic, not word frequencies or rankings. The tool always leaves app import unapproved and independent reference/qualified review pending.

Raw files/notices remain ignored and outside app assets. Only code, synthetic tests, non-text receipts and documentation are committed. Corpus data, translations, recordings, roots/lemmas, meanings and learner databases are untouched.
