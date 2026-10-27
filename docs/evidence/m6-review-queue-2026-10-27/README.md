# Private complete source-edit queue — 2026-10-27

[Bounded public summary](review-summary.json) records a completed diagnostic export, not accepted text equivalence or replacement mappings. Both raw source artifacts and old receipts are unchanged.

Private queue: `.local/quranic/reviews/20261027-source-differences-v1/edits.jsonl`, **8,385,610 bytes**, SHA-256 **2fd79315d96e2fc303be5a733f8bc5f077036465ed1cfc1f8b797af644d161ad**. It contains all **6,172 differing bodies**, with **60,917 exact edit operations**; each record reconstructed the comparison body exactly before writing. The completed queue bytes were then re-read and their hash checked. Its completion summary is written last. Interrupted exports remain private/incomplete; existing directories are never overwritten.

Mechanical edit groups: 46,125 mark-only, 10,254 letter-involving and 4,538 other nonletter operations. References with such operations: 6,101 / 4,344 / 2,706 respectively; these reference groups overlap. Unicode categories are not semantic or religious judgments: marks can include hamza signs and modifier letters include tatweel. No group/pattern is automatically harmless or approved for substitution.

The runtime is recorded: Python 3.15.0, Unicode database 17.0.0. The queue uses `difflib.SequenceMatcher` with autojunk disabled; its alignment is not claimed unique, minimal or linguistically informed. Code-point ranges/payloads and both field hashes make every record exactly reconstructable from the original. Full edit payloads stay ignored/private; only the bounded summary/provenance enters Git. Long fields exceeding the analysis limit cause rejection before export, rather than an incomplete queue being called complete.

All **66 tooling tests** passed (26 acquisition, 13 numbered-reference, 14 strict audit, 13 queue). Tests cover exact edit reconstruction and tampering, preserved combining sequences/whitespace, source-read races, path/overwrite/symlink protection, incomplete-output handling, bounds, no network/source edits and unchanged approval gates.

Source provenance/pins remain those in [the prior strict audit](../m6-text-audit-2026-10-27/README.md). Only validated reference-number trailers are excluded in comparison copies; neither source is normalized, repaired or replaced. The 64 already-exact bodies produce no edit records. The 112 separate opening fields remain uncompared. M4/M5, qualified Quranic/text/annotation/rights assessment and app import remain pending. No Android build/runtime operation or source download occurred for this export.

Qualified review should record reviewer identity/qualification, exact source/queue hashes, findings and scope before any acceptance. The queue provides evidence for that assessment; its completion grants no approval. The user subsequently delegated the next development choice; M7 dictionary/grammar may proceed using existing English/Fusha content while these gates remain pending.
