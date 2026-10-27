# Strict source Unicode difference audit — 2026-10-27

Continuation scope: source-only verification of the existing pinned Tanzil candidate and independent Hafs archive. M4/M5 review and Quranic app import remain pending. No new download, source repair, normalized replacement or Android change.

[Machine report](unicode-difference-report.json) was produced by `tools/tanzil_text_audit.py`. The CLI deliberately returned **1 / DIFFERENCES_FOUND**, not a text-equivalence pass. Structural/integrity failures instead use exit 2. All **53 tooling unit tests** passed in 0.099s, including 14 new text-audit tests and the affected existing acquisition/reference checks.

| Check | Actual result |
| --- | --- |
| Numbered reference sets | 6,236 pairs / 114 chapters match |
| Raw Unicode text fields | 0 exact matches / 6,236 differences |
| Reference number trailers | All 6,236 have the exact expected `U+0020`, `U+06DD`, Arabic-Indic ayah number suffix |
| Text bodies after excluding only that validated suffix | 64 exact matches / 6,172 differences |
| Separate Tanzil opening fields | 112 retained separately; equivalence not checked |

The raw-field result includes publisher verse numbering. The body result removes only one verified separator/number trailer from a comparison copy. It never trims other spaces, applies NFC, removes harakat, maps letters/symbols or changes source bytes. Residual differences may include publisher encoding/annotation conventions; exact Unicode inequality alone does not establish error or semantic difference. We have not approved a rule set to dismiss or repair them.

The report contains provenance/hashes, numeric results, body-only character-code diagnostics and twelve bounded mismatch samples with code-point offsets/deltas. It does not include full original ayah strings. Sampled edits are capped; long fields retain strict equality/hashing even when detailed edit analysis is omitted. Code-point occurrence diagnostics are not word/lemma/root frequencies. Source-specific annotation differences must not be recast as ordinary Fusha spelling corrections.

## Reproduce offline

```sh
PYTHONDONTWRITEBYTECODE=1 python3 tools/tanzil_text_audit.py .local/quranic/tanzil/20261027T062636Z-8c5aeae20363a98f --expected-manifest-sha256 8ddf2ddedd8ea8ded0c7406266a72be7c1c117e545d7ef9670d945a1fac76cd4 --reference-archive .local/quranic/references/20261027T065235Z-kfgqpc-hafs-227e6b1564d9/reference.zip
```

The original manifest/notice/structure checks and archive published checksum are checked first. The exact XML read for text comparison is re-bound to the externally pinned manifest, preventing a source replacement between initial verification and audit. Reference identities must still match. The archive's bounded pinned JSON reader is shared with the existing inventory checker; no files are extracted or installed.

Tanzil source XML SHA-256: `8c5aeae20363a98f6963720d29fce040ca8b56a8e75f8b564c257fce7f6d0417`.

Official KFGQPC archive SHA-256: `227e6b1564d980f2bd09c2c35ebfb0330ac268c79a7c247cd1ab665bc635f245`.

All raw files, notices and the old acquisition manifest/receipts remain unchanged and ignored. This report is a later diagnostic record, not a replacement for their historical gates. Qualified text/annotation review, opening equivalence, morphology/meanings/rights, M4/M5 reviews and Quranic app import remain pending. The next source decision is how to assess these publisher-specific Unicode differences with qualified evidence; automated normalization is not accepted as approval.
