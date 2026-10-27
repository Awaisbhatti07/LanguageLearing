# Independent numbered-reference comparison — 2026-10-27

User scope: independently check the saved Tanzil reference inventory, retaining M4/M5 review and Quranic app-import gates.

[Comparison report](reference-comparison.json): **MATCH**, all **6,236 numbered references / 114 chapters**, **zero missing / zero unexpected**. The ordered numeric-pair inventory digests agree: `0cb7c54648be4ce17587a3c20f18e083be9b3e3bc3405423208b38a24178e726`.

## Sources and integrity

- Original Tanzil candidate: ignored `.local/quranic/tanzil/20261027T062636Z-8c5aeae20363a98f`, unchanged manifest pin `8ddf2ddedd8ea8ded0c7406266a72be7c1c117e545d7ef9670d945a1fac76cd4`, retained separately in the [original receipt](../m6-tanzil-2026-10-27/acquisition-receipt.json).
- Independent publisher: [King Fahd Complex developer page](https://qurancomplex.gov.sa/quran-dev/), which lists the Hafs dataset's `sura_no`/`aya_no` fields and publishes its SHA-256. Exact archive: [official Hafs download](https://download.qurancomplex.gov.sa/resources_dev/kfgqpc_hafs_v30.zip).
- Downloaded archive: **10,155,774 bytes**, SHA-256 **227e6b1564d980f2bd09c2c35ebfb0330ac268c79a7c247cd1ab665bc635f245**, matching the checksum read from the official page before comparison. Private evidence is under `.local/quranic/references/20261027T065235Z-kfgqpc-hafs-227e6b1564d9/` (`reference.zip`, `acquisition.json`). A separate attempt to recapture the full publisher HTML timed out; no local publisher-page snapshot is claimed.
- Only `kfgqpc_hafs_v30-data/kfgqpc_hafs_v30.json` is read in memory from the ZIP. Member: **3,691,596 bytes**, SHA-256 **d7adf8aeeef5a7b6798060706a55735dffbcdb542b290a3907bd9f70c498f4d3**. Nothing is extracted to disk; fonts/documents/SQL remain unopened and uninstalled.

## What was checked

The old candidate first passes its separately pinned byte/notice/structure checks. Those checks establish that each actual Tanzil chapter/ayah index is unique, ordered and contiguous. Their checked ranges therefore identify its actual numbered-reference set. The comparison reads the other publisher's numeric identifiers independently, rejects invalid types/duplicates/gaps/order and compares every pair; it does not use the Tanzil totals as expected reference data.

The CLI is offline/read-only. Its fixed published archive pin is checked before ZIP/JSON parsing; unsupported, unsafe, oversized, duplicate or corrupt members are rejected. A mismatch prints the exact missing/unexpected numeric pairs and exits unsuccessfully. Matching global totals alone are insufficient. Thirteen synthetic/nonreligious tests passed, covering well-formed truncation, extra/missing final references, changed chapter boundaries at equal totals, tampering, archive safety and pending gates. Exact commands and initial failures are in `docs/TEST_RESULTS.md`.

## Limits retained

This is a separately published numeric reference-inventory match within the selected Hafs dataset's numbering. It is not evidence of a separate historical textual lineage, letter/harakat equivalence, unnumbered-opening equivalence, word alignment, meanings, frequencies, religious/linguistic review or redistribution approval. The old acquisition manifest remains unchanged with its historical pending state; this new report records the later, narrower check. M4/M5 reviews and Quranic app import remain pending. No Android code/assets/schema/dependency/learner data changed, and no Android build/runtime checks were repeated.

Raw third-party source data remains ignored/private. The Git-tracked report contains only provenance, hashes, numeric comparison results and pending gates.
