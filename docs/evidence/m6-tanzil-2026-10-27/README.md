# Tanzil source-only acquisition evidence — 2026-10-27

The user authorized Tanzil-only acquisition and integrity tooling, with M4/M5 qualified-review/physical-audio gates pending. This evidence does not represent Quranic app import, independent reference completeness or religious/linguistic approval.

[Acquisition receipt](acquisition-receipt.json) contains hashes/sizes, selected official URLs, UTC acquisition time, observed structure and pending gates. It contains no Quran text, translations or annotations. The separately retained manifest pin is:

`8ddf2ddedd8ea8ded0c7406266a72be7c1c117e545d7ef9670d945a1fac76cd4`

The original candidate remains only in ignored `.local/quranic/tanzil/20261027T062636Z-8c5aeae20363a98f/`. Its 1,541,387-byte XML has SHA-256 `8c5aeae20363a98f6963720d29fce040ca8b56a8e75f8b564c257fce7f6d0417`. Both complete notices and raw official HTML snapshots remain with it. The XML/page copyright headers differ; neither was rewritten. Their usage terms match after stripping only XML-comment decoration/whitespace for comparison.

Actual acquisition via the committed tool and read-only offline verification against the separately retained pin both passed. 26 synthetic/nonreligious unit tests passed, covering tampering, preserved Unicode, notice changes, malformed/entity/oversized XML, reference gaps, HTTP limits, restricted redirects, overwrite/symlink protection, read-only/no-network verification and explicit approval gates. Exact commands/failures/corrections are in `docs/TEST_RESULTS.md` and [tool usage](../../TANZIL_TOOLING.md).

The earlier 062158Z/062436Z candidates stopped at notice comparison and remain incomplete/untouched. Their XML hashes match the final raw payload, but they have no completed manifest and cannot be promoted by guessing a verification pin.

Observed 114 chapters, 6,236 ayat and 112 separate opening fields were parsed from the acquired file. These observations are not word frequencies or an independently checked source reference inventory. Contiguous-reference checks cannot establish that a well-formed final record is present; independent inventory/alignment/review remain pending. Original content is never repaired, simplified or merged with opening fields.

No Corpus contact email, paid API, metadata/translation/recording download, project-license change or Android app/schema/dependency/learner-data change occurred. Prior Android test/build/runtime evidence remains historical and was not repeated.
