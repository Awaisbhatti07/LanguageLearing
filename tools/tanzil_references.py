#!/usr/bin/env python3
"""Read-only cross-publisher numbered-reference comparison for a pinned Tanzil candidate.

Checks reference identities only, never text equivalence or app-import approval.
The separate publisher's raw archive stays in ignored local storage.
"""

import argparse
from datetime import datetime, timezone
import hashlib
import io
import json
from pathlib import Path, PurePosixPath
import stat
import sys
import zipfile
import zlib

import tanzil_source as source


PUBLISHER = "King Fahd Complex for the Printing of the Holy Quran"
PUBLISHER_PAGE = "https://qurancomplex.gov.sa/quran-dev/"
ARCHIVE_URL = "https://download.qurancomplex.gov.sa/resources_dev/kfgqpc_hafs_v30.zip"
# Published on the official developer page, not calculated from the Tanzil candidate.
ARCHIVE_SHA256 = "227e6b1564d980f2bd09c2c35ebfb0330ac268c79a7c247cd1ab665bc635f245"
JSON_MEMBER = "kfgqpc_hafs_v30-data/kfgqpc_hafs_v30.json"
ARCHIVE_LIMIT = 16 * 1024 * 1024
MEMBER_LIMIT = 8 * 1024 * 1024
ENTRY_LIMIT = 256
GATES = {"app_import": "NOT_APPROVED", "m4_m5_review": "PENDING", "qualified_quranic_review": "PENDING",
         "text_equivalence": "NOT_CHECKED", "word_alignment_meanings_morphology": "NOT_CHECKED",
         "reference_data_redistribution": "NOT_APPROVED"}


def inventory_digest(references):
    """Hash ordered numeric pairs without publishing the source data or verse text."""
    encoded = json.dumps(sorted(references), separators=(",", ":"), ensure_ascii=True).encode("ascii")
    return hashlib.sha256(encoded).hexdigest()


def parse_inventory(data):
    try:
        # A possible UTF-8 BOM is an encoding marker, not a modification of raw bytes.
        records = json.loads(data.decode("utf-8-sig", errors="strict"),
                             object_pairs_hook=source.unique_object,
                             parse_constant=lambda _: (_ for _ in ()).throw(source.SourceError("Invalid reference JSON constant")))
    except (UnicodeDecodeError, json.JSONDecodeError, RecursionError) as error:
        raise source.SourceError("Malformed reference inventory JSON") from error
    source.require(isinstance(records, list) and 0 < len(records) <= source.MAX_RECORDS,
                   "Empty, oversized or unsupported reference record list")
    references = []
    previous = (0, 0)
    for record in records:
        source.require(isinstance(record, dict), "Reference record must be an object")
        chapter, ayah = record.get("sura_no"), record.get("aya_no")
        source.require(type(chapter) is int and type(ayah) is int
                       and 1 <= chapter <= source.MAX_CHAPTERS and 1 <= ayah <= source.MAX_RECORDS,
                       "Reference identifiers must be bounded positive integers")
        current = (chapter, ayah)
        expected = (previous[0], previous[1] + 1) if chapter == previous[0] else (previous[0] + 1, 1)
        source.require(current == expected, "Duplicate, missing or unordered publisher reference")
        references.append(current)
        previous = current
    return references


def reference_inventory(archive_data, expected_sha256):
    source.require(source.sha256(archive_data) == expected_sha256,
                   "Independent archive does not match the published pin")
    try:
        with zipfile.ZipFile(io.BytesIO(archive_data)) as archive:
            entries = archive.infolist()
            source.require(0 < len(entries) <= ENTRY_LIMIT, "Unexpected archive entry count")
            names = set()
            for entry in entries:
                name = entry.filename
                path = PurePosixPath(name)
                source.require(name not in names and not path.is_absolute()
                               and ".." not in path.parts and "\\" not in name and ":" not in name
                               and not stat.S_ISLNK(entry.external_attr >> 16),
                               "Duplicate or unsafe reference archive member")
                names.add(name)
            source.require(JSON_MEMBER in names, "Expected official Hafs JSON member is missing")
            info = archive.getinfo(JSON_MEMBER)
            source.require(not info.is_dir() and not (info.flag_bits & 1)
                           and info.compress_type in (zipfile.ZIP_STORED, zipfile.ZIP_DEFLATED)
                           and 0 < info.file_size <= MEMBER_LIMIT,
                           "Unsupported or oversized reference member")
            # No extractall: fonts, documents, SQL and all other members stay unopened.
            with archive.open(info) as member:
                data = member.read(MEMBER_LIMIT + 1)
            source.require(0 < len(data) <= MEMBER_LIMIT and len(data) == info.file_size,
                           "Incomplete or oversized reference member")
    except (zipfile.BadZipFile, zipfile.LargeZipFile, RuntimeError, NotImplementedError, EOFError, zlib.error) as error:
        raise source.SourceError("Invalid reference archive") from error
    references = parse_inventory(data)
    return references, {"publisher": PUBLISHER, "publisher_page_url": PUBLISHER_PAGE,
                        "archive_url": ARCHIVE_URL, "archive_sha256": expected_sha256,
                        "archive_bytes": len(archive_data), "member": JSON_MEMBER,
                        "member_sha256": source.sha256(data), "member_bytes": len(data),
                        "chapter_count": references[-1][0], "numbered_reference_count": len(references),
                        "reference_inventory_sha256": inventory_digest(references)}


def compare_references(bundle, expected_manifest_sha256, reference_archive):
    # Verify the old candidate against its separately retained pin. Its manifest is never rewritten.
    checks = source.verify_candidate(bundle, expected_manifest_sha256)
    archive_data = source.read_file(reference_archive, ARCHIVE_LIMIT)
    expected, reference = reference_inventory(archive_data, ARCHIVE_SHA256)
    # The Tanzil parser already proved uniqueness, ordering and per-chapter contiguity.
    # Its observed lengths therefore identify every actual numbered record in that snapshot.
    observed = [(chapter, ayah) for chapter, count in
                enumerate(checks["ayah_counts_by_chapter_observed"], 1) for ayah in range(1, count + 1)]
    observed_set, expected_set = set(observed), set(expected)
    missing, unexpected = sorted(expected_set - observed_set), sorted(observed_set - expected_set)
    return {"schema_version": 1, "kind": "independent-publisher-numbered-reference-comparison",
            "scope": "Numbered chapter/ayah identities only; no text or unnumbered-opening comparison",
            "result": "MATCH" if not missing and not unexpected else "MISMATCH",
            "tanzil": {"manifest_sha256": expected_manifest_sha256,
                       "chapter_count": checks["chapter_count_observed"],
                       "numbered_reference_count": len(observed),
                       "reference_inventory_sha256": inventory_digest(observed)},
            "independent_reference": reference,
            "comparison": {"matched_reference_count": len(observed_set & expected_set),
                           "missing_reference_count": len(missing), "unexpected_reference_count": len(unexpected),
                           "missing_references": missing, "unexpected_references": unexpected},
            "independence_limit": "Separate publisher and artifact; shared Hafs tradition, not an independent textual lineage claim",
            "gates": GATES}


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("bundle", type=Path)
    parser.add_argument("--expected-manifest-sha256", required=True)
    parser.add_argument("--reference-archive", type=Path, required=True,
                        help="Privately retained official archive; must match the fixed publisher checksum")
    args = parser.parse_args(argv)
    try:
        result = compare_references(args.bundle, args.expected_manifest_sha256, args.reference_archive)
        result["compared_at_utc"] = datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ")
        print(json.dumps(result, indent=2, sort_keys=True))
        return 0 if result["result"] == "MATCH" else 1
    except (source.SourceError, OSError) as error:
        print(f"Independent reference check failed: {error}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
