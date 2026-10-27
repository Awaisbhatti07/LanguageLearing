#!/usr/bin/env python3
"""Offline strict Unicode difference audit of the two already-pinned source artifacts.

Reports numeric/code-point diagnostics, never raw Quran text or source corrections.
Differences produce exit status 1; structural/integrity failures produce status 2.
"""

import argparse
from collections import Counter
from datetime import datetime, timezone
from difflib import SequenceMatcher
import hashlib
import json
from pathlib import Path
import struct
import sys
import xml.etree.ElementTree as ET

import tanzil_references as reference
import tanzil_source as source


MAX_FIELD_CODEPOINTS = 64000
MAX_EDIT_FIELD_CODEPOINTS = 4096
SAMPLE_LIMIT = 12
EDIT_SAMPLE_LIMIT = 4
EDIT_CODEPOINT_LIMIT = 12
ARABIC_INDIC = str.maketrans('0123456789', '\u0660\u0661\u0662\u0663\u0664\u0665\u0666\u0667\u0668\u0669')
GATES = {"app_import": "NOT_APPROVED", "m4_m5_review": "PENDING",
         "qualified_quranic_review": "PENDING", "text_equivalence": "PENDING",
         "unnumbered_opening_equivalence": "NOT_CHECKED", "word_alignment_meanings_morphology": "NOT_CHECKED",
         "reference_data_redistribution": "NOT_APPROVED"}


def field_digest(value):
    return hashlib.sha256(value.encode('utf-8')).hexdigest()


def codepoints(value):
    return [f'U+{ord(character):04X}' for character in value]


def numbered_body(value, ayah):
    source.require(isinstance(value, str) and 0 < len(value) <= MAX_FIELD_CODEPOINTS
                   and '\x00' not in value and '\ufffd' not in value
                   and not any(0xD800 <= ord(c) <= 0xDFFF for c in value),
                   'Invalid publisher Unicode text field')
    # The selected official field encodes one ASCII separator, END OF AYAH and the
    # matching Arabic-Indic number. No trim/normalization is applied to the body.
    trailer = ' \u06dd' + str(ayah).translate(ARABIC_INDIC)
    source.require(value.endswith(trailer), 'Publisher verse-number trailer missing or mismatched')
    body = value[:-len(trailer)]
    source.require(body.strip() and len(body) < len(value), 'Empty publisher ayah body')
    return body


def text_inventory_digest(fields):
    digest = hashlib.sha256()
    for (chapter, ayah), value in sorted(fields.items()):
        data = value.encode('utf-8')
        digest.update(struct.pack('!III', chapter, ayah, len(data)))
        digest.update(data)
    return digest.hexdigest()


def difference_sample(key, original, publisher, body):
    result = {"reference": list(key), "tanzil_sha256": field_digest(original),
              "publisher_raw_sha256": field_digest(publisher), "publisher_body_sha256": field_digest(body),
              "tanzil_codepoints": len(original), "publisher_raw_codepoints": len(publisher),
              "publisher_body_codepoints": len(body), "body_edits": []}
    if max(len(original), len(body)) > MAX_EDIT_FIELD_CODEPOINTS:
        result['body_edits_omitted'] = 'Field exceeds bounded edit-analysis size; equality/hashes still checked'
        return result
    opcodes = [op for op in SequenceMatcher(a=original, b=body, autojunk=False).get_opcodes() if op[0] != 'equal']
    result['body_edit_count'] = len(opcodes)
    for tag, left_start, left_end, right_start, right_end in opcodes[:EDIT_SAMPLE_LIMIT]:
        result['body_edits'].append({"operation": tag,
                                    "tanzil_codepoint_range": [left_start, left_end],
                                    "publisher_body_codepoint_range": [right_start, right_end],
                                    "tanzil_codepoints_prefix": codepoints(original[left_start:min(left_end, left_start + EDIT_CODEPOINT_LIMIT)]),
                                    "publisher_codepoints_prefix": codepoints(body[right_start:min(right_end, right_start + EDIT_CODEPOINT_LIMIT)]),
                                    "codepoint_prefixes_truncated": max(left_end-left_start, right_end-right_start) > EDIT_CODEPOINT_LIMIT})
    result['body_edits_truncated'] = len(opcodes) > EDIT_SAMPLE_LIMIT
    return result


def compare_fields(originals, publisher_fields, openings):
    source.require(originals and set(originals) == set(publisher_fields),
                   'Text comparison requires exactly matching numbered-reference sets')
    raw_equal = body_equal = 0
    samples = []
    original_characters, body_characters = Counter(), Counter()
    bodies = {}
    for key, original in sorted(originals.items()):
        publisher = publisher_fields[key]
        body = numbered_body(publisher, key[1])
        raw_equal += original == publisher
        body_equal += original == body
        original_characters.update(original)
        body_characters.update(body)
        bodies[key] = body
        if original != publisher and len(samples) < SAMPLE_LIMIT:
            samples.append(difference_sample(key, original, publisher, body))
    return {"raw_field_exact_matches": raw_equal, "raw_field_differences": len(originals) - raw_equal,
            "validated_reference_number_trailers": len(originals),
            "body_exact_matches_excluding_only_validated_number_trailer": body_equal,
            "body_differences_excluding_only_validated_number_trailer": len(originals) - body_equal,
            "tanzil_text_inventory_sha256": text_inventory_digest(originals),
            "publisher_raw_text_inventory_sha256": text_inventory_digest(publisher_fields),
            "publisher_body_text_inventory_sha256": text_inventory_digest(bodies),
            "text_inventory_digest_method": "ordered-u32-reference-and-utf8-length-prefixed-fields-v1",
            "tanzil_separate_opening_field_count": len(openings),
            "opening_comparison": "NOT_CHECKED; never merged with numbered ayat or inferred from publisher text",
            "tanzil_only_body_codepoints": [{"codepoint": f'U+{ord(c):04X}', "occurrences": count}
                                            for c, count in sorted(original_characters.items()) if c not in body_characters],
            "publisher_only_body_codepoints": [{"codepoint": f'U+{ord(c):04X}', "occurrences": count}
                                               for c, count in sorted(body_characters.items()) if c not in original_characters],
            "difference_samples": samples, "sample_limit": SAMPLE_LIMIT,
            "samples_truncated": len(originals) - raw_equal > len(samples)}


def load_verified_fields(bundle, expected_manifest_sha256, reference_archive):
    """Return immutable-source comparison inputs bound to both retained artifact pins."""
    checks = source.verify_candidate(bundle, expected_manifest_sha256)
    # Bind the exact XML read for this audit to the externally pinned old manifest.
    manifest_bytes = source.read_file(bundle / source.MANIFEST_FILE, source.JSON_LIMIT)
    source.require(source.sha256(manifest_bytes) == expected_manifest_sha256, 'Candidate manifest changed during audit')
    manifest = json.loads(manifest_bytes)
    original_bytes = source.read_file(bundle / source.SOURCE_FILE, source.XML_LIMIT)
    source.require(source.sha256(original_bytes) == manifest['files'][source.SOURCE_FILE]['sha256'],
                   'Candidate source changed during audit')
    # No transformations: verified XML supplies standard decoded attribute values.
    root = ET.fromstring(original_bytes)
    originals = {(int(ch.get('index')), int(ay.get('index'))): ay.get('text') for ch in root for ay in ch}
    openings = {(int(ch.get('index')), int(ay.get('index'))): ay.get('bismillah')
                for ch in root for ay in ch if 'bismillah' in ay.attrib}
    archive_data = source.read_file(reference_archive, reference.ARCHIVE_LIMIT)
    payload, publisher_receipt = reference.reference_payload(archive_data, reference.ARCHIVE_SHA256)
    refs = reference.parse_inventory(payload)
    records = json.loads(payload.decode('utf-8-sig'))
    publisher = {(row['sura_no'], row['aya_no']): row.get('aya_text_unicode') for row in records}
    source.require(set(originals) == set(refs), 'Numbered references differ; text audit cannot proceed')
    return originals, publisher, openings, checks, source.sha256(original_bytes), publisher_receipt


def audit_text(bundle, expected_manifest_sha256, reference_archive):
    originals, publisher, openings, checks, original_sha256, publisher_receipt = load_verified_fields(
        bundle, expected_manifest_sha256, reference_archive)
    comparison = compare_fields(originals, publisher, openings)
    return {"schema_version": 1, "kind": "strict-source-unicode-difference-audit",
            "result": "EXACT_MATCH" if comparison['raw_field_differences'] == 0 else "DIFFERENCES_FOUND",
            "normalization": "NONE; no trim, NFC, letter/harakat/symbol replacement or source rewriting",
            "diagnostic_body_scope": "Only one U+0020 separator + U+06DD + the exact expected Arabic-Indic verse number excluded",
            "numbered_reference_result": "MATCH", "numbered_reference_count": len(originals),
            "chapter_count": checks['chapter_count_observed'],
            "tanzil_manifest_sha256": expected_manifest_sha256,
            "tanzil_xml_sha256": original_sha256,
            "independent_reference": publisher_receipt, "comparison": comparison, "gates": GATES,
            "limit": "Unicode inequality does not establish source error or semantic difference; qualified assessment remains pending"}


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('bundle', type=Path)
    parser.add_argument('--expected-manifest-sha256', required=True)
    parser.add_argument('--reference-archive', type=Path, required=True)
    args = parser.parse_args(argv)
    try:
        report = audit_text(args.bundle, args.expected_manifest_sha256, args.reference_archive)
        report['audited_at_utc'] = datetime.now(timezone.utc).strftime('%Y-%m-%dT%H:%M:%SZ')
        print(json.dumps(report, indent=2, sort_keys=True))
        return 0 if report['result'] == 'EXACT_MATCH' else 1
    except (source.SourceError, OSError, ET.ParseError) as error:
        print(f'Source text audit failed: {error}', file=sys.stderr)
        return 2


if __name__ == '__main__':
    raise SystemExit(main())
