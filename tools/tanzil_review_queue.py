#!/usr/bin/env python3
"""Export a private, lossless edit queue for assessment of the pinned source differences.

No source correction or equivalence approval. Raw ayah strings are not printed.
The queue is kept only under ignored .local; a bounded summary may be retained in Git.
"""

import argparse
from collections import Counter
from datetime import datetime, timezone
from difflib import SequenceMatcher
import hashlib
import json
from pathlib import Path
import platform
import sys
import unicodedata
import xml.etree.ElementTree as ET

import tanzil_source as source
import tanzil_text_audit as audit


QUEUE_FILE = 'edits.jsonl'
SUMMARY_FILE = 'summary.json'
QUEUE_LIMIT_BYTES = 64 * 1024 * 1024
TOP_PATTERN_LIMIT = 12
SHORT_PATTERN_CODEPOINT_LIMIT = 3
GROUPS = ('letter_involving', 'mark_only', 'other_nonletters')
GATES = {"app_import": "NOT_APPROVED", "m4_m5_review": "PENDING", "qualified_quranic_review": "PENDING",
         "text_equivalence": "PENDING", "mapping_rules": "NOT_APPROVED",
         "unnumbered_opening_equivalence": "NOT_CHECKED", "reference_data_redistribution": "NOT_APPROVED"}


def unicode_group(old, new):
    categories = {unicodedata.category(c)[0] for c in old + new}
    if 'L' in categories:
        return 'letter_involving'
    return 'mark_only' if categories == {'M'} else 'other_nonletters'


def edit_record(key, original, body):
    require = source.require
    require(isinstance(original, str) and isinstance(body, str)
            and max(len(original), len(body)) <= audit.MAX_EDIT_FIELD_CODEPOINTS,
            'Full edit analysis exceeds bounded field size; source review required')
    edits = []
    for tag, a, b, c, d in SequenceMatcher(a=original, b=body, autojunk=False).get_opcodes():
        if tag == 'equal':
            continue
        old, new = original[a:b], body[c:d]
        edits.append({"operation": tag, "left": [a, b], "right": [c, d],
                      "old": [ord(v) for v in old], "new": [ord(v) for v in new],
                      "unicode_group": unicode_group(old, new)})
    record = {"reference": list(key), "tanzil_sha256": audit.field_digest(original),
              "publisher_body_sha256": audit.field_digest(body), "tanzil_codepoints": len(original),
              "publisher_body_codepoints": len(body), "edits": edits}
    reconstructed = reconstruct_record(original, record)
    require(reconstructed == body, 'Edit alignment failed exact reconstruction')
    return record


def scalar_string(values):
    source.require(isinstance(values, list) and len(values) <= audit.MAX_EDIT_FIELD_CODEPOINTS
                   and all(type(v) is int and 0 <= v <= 0x10FFFF and not 0xD800 <= v <= 0xDFFF for v in values),
                   'Invalid Unicode scalar list in edit record')
    return ''.join(chr(v) for v in values)


def reconstruct_record(original, record):
    require = source.require
    require(isinstance(record, dict) and record.get('tanzil_sha256') == audit.field_digest(original)
            and type(record.get('tanzil_codepoints')) is int and record['tanzil_codepoints'] == len(original),
            'Edit record does not describe this exact source field')
    edits = record.get('edits')
    require(isinstance(edits, list) and len(edits) <= 2 * audit.MAX_EDIT_FIELD_CODEPOINTS,
            'Invalid edit list')
    cursor = output_length = 0
    output = []
    for edit in edits:
        require(isinstance(edit, dict), 'Invalid edit operation')
        left, right = edit.get('left'), edit.get('right')
        require(isinstance(left, list) and len(left) == 2 and isinstance(right, list) and len(right) == 2
                and all(type(v) is int for v in left + right), 'Invalid code-point ranges')
        a, b = left
        c, d = right
        require(cursor <= a <= b <= len(original) and 0 <= c <= d <= audit.MAX_EDIT_FIELD_CODEPOINTS,
                'Overlapping or out-of-bounds edit')
        old, new = scalar_string(edit.get('old')), scalar_string(edit.get('new'))
        require(original[a:b] == old and len(new) == d - c, 'Edit payload/range mismatch')
        tag = edit.get('operation')
        require((tag == 'insert' and a == b and c < d)
                or (tag == 'delete' and a < b and c == d)
                or (tag == 'replace' and a < b and c < d), 'Invalid edit type')
        require(edit.get('unicode_group') == unicode_group(old, new), 'Edit category mismatch')
        unchanged = original[cursor:a]
        require(c == output_length + len(unchanged), 'Destination edit offset is inconsistent')
        output.extend((unchanged, new))
        output_length = d
        cursor = b
    output.append(original[cursor:])
    result = ''.join(output)
    require(type(record.get('publisher_body_codepoints')) is int
            and len(result) == record['publisher_body_codepoints']
            and audit.field_digest(result) == record.get('publisher_body_sha256'),
            'Edit reconstruction does not match the reference field')
    return result


def private_directory(path):
    root = source.PROJECT_ROOT.resolve()
    target = path.resolve()
    source.require(target.is_relative_to(root / '.local') and target != root / '.local',
                   'Review queue must remain in project-local ignored storage')
    return target


def pattern_summary(pattern, count):
    old, new = pattern
    def describe(values):
        return [{"codepoint": f'U+{value:04X}', "name": unicodedata.name(chr(value), 'UNASSIGNED'),
                 "general_category": unicodedata.category(chr(value))} for value in values]
    return {"old": describe(old), "new": describe(new), "edit_occurrences": count,
            "status": "MECHANICAL_PATTERN_ONLY; NOT_AN_APPROVED_MAPPING"}


def export_queue(bundle, expected_manifest_sha256, reference_archive, directory):
    originals, publishers, openings, checks, original_sha, reference_receipt = audit.load_verified_fields(
        bundle, expected_manifest_sha256, reference_archive)
    # Validate all trailer fields and resource limits before creating the export directory.
    bodies = {key: audit.numbered_body(value, key[1]) for key, value in publishers.items()}
    source.require(all(max(len(originals[k]), len(bodies[k])) <= audit.MAX_EDIT_FIELD_CODEPOINTS for k in originals),
                   'A field exceeds the full-review analysis limit')
    directory = private_directory(directory)
    directory.mkdir(parents=True, exist_ok=False, mode=0o700)
    queue_hash = hashlib.sha256()
    queue_size = records = raw_equal = body_equal = total_edits = 0
    edit_groups, reference_groups = Counter(), Counter()
    patterns = Counter()
    try:
        with (directory / QUEUE_FILE).open('xb') as queue:
            for key, original in sorted(originals.items()):
                raw_equal += original == publishers[key]
                body_equal += original == bodies[key]
                if original == bodies[key]:
                    continue
                record = edit_record(key, original, bodies[key])
                record_groups = set()
                for edit in record['edits']:
                    group = edit['unicode_group']
                    edit_groups[group] += 1
                    record_groups.add(group)
                    old, new = tuple(edit['old']), tuple(edit['new'])
                    if max(len(old), len(new)) <= SHORT_PATTERN_CODEPOINT_LIMIT:
                        patterns[(old, new)] += 1
                reference_groups.update(record_groups)
                total_edits += len(record['edits'])
                data = (json.dumps(record, separators=(',', ':'), sort_keys=True) + '\n').encode('ascii')
                queue_size += len(data)
                source.require(queue_size <= QUEUE_LIMIT_BYTES, 'Review queue exceeds size limit')
                queue.write(data)
                queue_hash.update(data)
                records += 1
        # Re-read the finished queue bytes before writing the completion summary.
        with (directory / QUEUE_FILE).open('rb') as queue:
            reread = hashlib.sha256()
            for chunk in iter(lambda: queue.read(65536), b''):
                reread.update(chunk)
        source.require(reread.hexdigest() == queue_hash.hexdigest(), 'Written review queue checksum mismatch')
        selected_patterns = sorted(patterns.items(), key=lambda p: (-p[1], p[0]))[:TOP_PATTERN_LIMIT]
        summary = {"schema_version": 1, "kind": "private-lossless-source-edit-queue-summary",
                   "result": "COMPLETE_DIAGNOSTIC_EXPORT",
                   "created_at_utc": datetime.now(timezone.utc).strftime('%Y-%m-%dT%H:%M:%SZ'),
                   "queue_directory": str(directory.relative_to(source.PROJECT_ROOT.resolve())),
                   "queue": {"file": QUEUE_FILE, "bytes": queue_size, "sha256": queue_hash.hexdigest(),
                             "records": records, "exact_reconstructions_checked": records},
                   "source": {"tanzil_manifest_sha256": expected_manifest_sha256, "tanzil_xml_sha256": original_sha,
                              "independent_reference": reference_receipt},
                   "scope": "Exact edit alignment on trailer-excluded comparison copies only; no source normalization/repair",
                   "alignment_method": "Python difflib.SequenceMatcher, autojunk=False; not linguistic or unique/minimal alignment",
                   "runtime": {"python_version": platform.python_version(), "unicode_data_version": unicodedata.unidata_version},
                   "numbered_reference_count": len(originals), "chapter_count": checks['chapter_count_observed'],
                   "raw_field_exact_matches": raw_equal, "body_exact_matches": body_equal,
                   "body_difference_count": records, "total_edit_operations": total_edits,
                   "edit_operations_by_unicode_group": {g: edit_groups[g] for g in GROUPS},
                   "references_by_unicode_group": {g: reference_groups[g] for g in GROUPS},
                   "reference_group_counts_overlap": True,
                   "unicode_group_limit": "Mark-only is not harmless; letter category includes modifier letters such as tatweel. Groups are not semantic judgments.",
                   "most_frequent_short_patterns": [pattern_summary(pattern, count) for pattern, count in selected_patterns],
                   "all_short_pattern_edit_occurrences": sum(patterns.values()),
                   "distinct_short_patterns": len(patterns), "top_pattern_limit": TOP_PATTERN_LIMIT,
                   "separate_opening_fields_uncompared": len(openings), "gates": GATES}
        # Written last; interrupted output is kept without a completion marker.
        with (directory / SUMMARY_FILE).open('xb') as output:
            output.write((json.dumps(summary, indent=2, sort_keys=True) + '\n').encode('utf-8'))
        return summary
    except (source.SourceError, OSError):
        print(f'Incomplete private review export retained at {directory}', file=sys.stderr)
        raise


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('bundle', type=Path)
    parser.add_argument('--expected-manifest-sha256', required=True)
    parser.add_argument('--reference-archive', type=Path, required=True)
    parser.add_argument('--output-directory', type=Path, required=True,
                        help='A NEW directory below project .local; existing output is never overwritten')
    args = parser.parse_args(argv)
    try:
        summary = export_queue(args.bundle, args.expected_manifest_sha256, args.reference_archive, args.output_directory)
        print(json.dumps(summary, indent=2, sort_keys=True))
        # The command produced a complete diagnostic export, not an equivalence pass.
        return 0
    except (source.SourceError, OSError, ET.ParseError) as error:
        print(f'Private review export failed: {error}', file=sys.stderr)
        return 1


if __name__ == '__main__':
    raise SystemExit(main())
