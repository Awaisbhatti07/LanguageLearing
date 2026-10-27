"""Synthetic nonreligious fixtures for private source-difference review exports."""

from contextlib import redirect_stdout, redirect_stderr
from copy import deepcopy
import io
import json
from pathlib import Path
import sys
import tempfile
import unittest
from unittest.mock import patch

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
import tanzil_review_queue as review
import tanzil_text_audit as audit
import tanzil_references as reference
import tanzil_source as source
from test_tanzil_source import xml, snapshots, STAMP
from test_tanzil_references import archive
from test_tanzil_text_audit import publisher_body


class TanzilReviewQueueTest(unittest.TestCase):
    def setUp(self):
        self.temporary = tempfile.TemporaryDirectory()
        self.addCleanup(self.temporary.cleanup)
        self.root = Path(self.temporary.name).resolve()
        self.bundle = self.root / 'candidate'
        self.archive_file = self.root / 'reference.zip'
        self.output = self.root / '.local' / 'review' / 'new-export'

    def prepare(self, original='SYNTHETIC A', other='SYNTHETIC B'):
        pin, _ = source.write_candidate(self.bundle, snapshots(xml(original)), STAMP)
        data = archive(records=[{'sura_no': 1, 'aya_no': 1, 'aya_text_unicode': publisher_body(other)}])
        self.archive_file.write_bytes(data)
        return pin, source.sha256(data)

    def export(self, pin, reference_pin):
        with patch.object(source, 'PROJECT_ROOT', self.root), patch.object(reference, 'ARCHIVE_SHA256', reference_pin):
            return review.export_queue(self.bundle, pin, self.archive_file, self.output)

    def test_edit_lists_exactly_reconstruct_insertions_deletions_replacements_and_combining_marks(self):
        cases = [('SYNTHETIC A', 'SYNTHETIC B'), ('A', 'BEFORE A'), ('ABC', 'A'),
                 ('Cafe\u0301 ا\u0654', 'Café أ'), ('SYNTHETIC مَرْحَبًا', 'SYNTHETIC مرحبا'),
                 ('A B', 'AB'), ('AB', 'AB '), ('ABABAB', 'AABABB')]
        for original, body in cases:
            with self.subTest(case=(original, body)):
                record = review.edit_record((1, 1), original, body)
                self.assertEqual(body, review.reconstruct_record(original, record))
                self.assertEqual(audit.field_digest(original), record['tanzil_sha256'])
                self.assertNotIn(original, json.dumps(record))

    def test_general_categories_are_mechanical_and_never_rules_for_equivalence(self):
        self.assertEqual('mark_only', review.unicode_group('\u0652', '\u06e1'))
        self.assertEqual('mark_only', review.unicode_group('\u0654', '\u0655'))  # Hamza signs are still marks.
        self.assertEqual('letter_involving', review.unicode_group('\u0640', ''))  # Tatweel is a modifier letter.
        self.assertEqual('letter_involving', review.unicode_group('ى', 'ي'))
        self.assertEqual('other_nonletters', review.unicode_group(' ', ''))
        pattern = review.pattern_summary(((0x0649,), (0x064A,)), 2)
        self.assertIn('NOT_AN_APPROVED_MAPPING', pattern['status'])
        self.assertEqual('PENDING', review.GATES['text_equivalence'])
        self.assertEqual('NOT_APPROVED', review.GATES['mapping_rules'])

    def test_mismatched_preimage_ranges_payload_category_or_output_digest_are_rejected(self):
        original = 'SYNTHETIC A'
        record = review.edit_record((1, 1), original, 'SYNTHETIC B')
        variants = []
        for key, value in [('tanzil_sha256', '0' * 64), ('publisher_body_sha256', '0' * 64),
                           ('tanzil_codepoints', True), ('publisher_body_codepoints', 100)]:
            altered = deepcopy(record); altered[key] = value; variants.append(altered)
        for key, value in [('left', [0, 999]), ('right', [999, 1000]), ('old', [65, 65]),
                           ('new', [True]), ('operation', 'equal'), ('unicode_group', 'mark_only')]:
            altered = deepcopy(record); altered['edits'][0][key] = value; variants.append(altered)
        for altered in variants:
            with self.subTest(record=altered), self.assertRaises(source.SourceError):
                review.reconstruct_record(original, altered)

    def test_overlapping_or_unsorted_edits_and_invalid_scalar_sequences_fail(self):
        original = 'SYNTHETIC A'
        record = review.edit_record((1, 1), original, 'SYNTHETIC B')
        record['edits'].append(deepcopy(record['edits'][0]))
        with self.assertRaises(source.SourceError):
            review.reconstruct_record(original, record)
        for values in [None, [True], [1.0], [-1], [0x110000], [0xD800], [0xDFFF]]:
            with self.subTest(values=values), self.assertRaises(source.SourceError):
                review.scalar_string(values)

    def test_completed_queue_has_a_matching_hash_and_every_row_reconstructs_exactly(self):
        pin, other_pin = self.prepare()
        summary = self.export(pin, other_pin)
        raw = (self.output / review.QUEUE_FILE).read_bytes()
        self.assertEqual(source.sha256(raw), summary['queue']['sha256'])
        self.assertEqual(len(raw), summary['queue']['bytes'])
        self.assertEqual(1, summary['queue']['records'])
        self.assertEqual(1, summary['queue']['exact_reconstructions_checked'])
        row = json.loads(raw)
        self.assertEqual('SYNTHETIC B', review.reconstruct_record('SYNTHETIC A', row))
        self.assertEqual(summary, json.loads((self.output / review.SUMMARY_FILE).read_bytes()))
        self.assertEqual('COMPLETE_DIAGNOSTIC_EXPORT', summary['result'])
        self.assertEqual('PENDING', summary['gates']['m4_m5_review'])
        self.assertEqual('NOT_APPROVED', summary['gates']['app_import'])
        self.assertNotIn('SYNTHETIC A', json.dumps(summary))
        self.assertIn('unicode_data_version', summary['runtime'])

    def test_exact_body_pairs_are_not_added_to_the_edit_queue_or_approved_as_raw_equal(self):
        pin, other_pin = self.prepare(other='SYNTHETIC A')
        summary = self.export(pin, other_pin)
        self.assertEqual(1, summary['body_exact_matches'])
        self.assertEqual(0, summary['raw_field_exact_matches'])
        self.assertEqual(0, summary['queue']['records'])
        self.assertEqual(b'', (self.output / review.QUEUE_FILE).read_bytes())
        self.assertEqual('PENDING', summary['gates']['text_equivalence'])

    def test_pattern_summaries_and_reference_group_counts_are_distinct_and_complete(self):
        pin, other_pin = self.prepare(original='SYNTHETIC A\u0652', other='SYNTHETIC B\u06e1')
        summary = self.export(pin, other_pin)
        self.assertEqual(summary['total_edit_operations'], sum(summary['edit_operations_by_unicode_group'].values()))
        self.assertEqual(1, summary['references_by_unicode_group']['letter_involving'])
        self.assertTrue(summary['reference_group_counts_overlap'])
        self.assertTrue(all(p['status'].endswith('NOT_AN_APPROVED_MAPPING') for p in summary['most_frequent_short_patterns']))

    def test_raw_sources_old_manifests_and_reference_archive_are_never_modified(self):
        pin, other_pin = self.prepare()
        before = {p: (p.read_bytes(), p.stat().st_mtime_ns) for p in list(self.bundle.iterdir()) + [self.archive_file]}
        with patch.object(source, 'fetch', side_effect=AssertionError('Network forbidden')):
            self.export(pin, other_pin)
        self.assertEqual(before, {p: (p.read_bytes(), p.stat().st_mtime_ns) for p in before})
        self.assertEqual('PENDING', json.loads((self.bundle / source.MANIFEST_FILE).read_bytes())['gates']['independent_reference_inventory'])

    def test_existing_export_and_symlink_escape_are_not_overwritten(self):
        pin, other_pin = self.prepare()
        summary = self.export(pin, other_pin)
        with self.assertRaises(FileExistsError):
            self.export(pin, other_pin)
        self.assertEqual(summary, json.loads((self.output / review.SUMMARY_FILE).read_bytes()))
        escape = self.root / 'outside'
        escape.mkdir()
        (self.root / '.local' / 'escape').symlink_to(escape, target_is_directory=True)
        with patch.object(source, 'PROJECT_ROOT', self.root), self.assertRaises(source.SourceError):
            review.private_directory(self.root / '.local' / 'escape' / 'new')
        self.assertEqual([], list(escape.iterdir()))

    def test_output_outside_ignored_project_storage_and_invalid_pins_create_no_queue(self):
        pin, other_pin = self.prepare()
        for bad_output in [self.root / 'app' / 'assets', self.root / '.local']:
            self.output = bad_output
            with self.subTest(path=bad_output), self.assertRaises(source.SourceError):
                self.export(pin, other_pin)
        self.output = self.root / '.local' / 'new'
        with self.assertRaises(source.SourceError):
            self.export('0' * 64, other_pin)
        self.assertFalse(self.output.exists())

    def test_size_limit_keeps_incomplete_queue_without_completion_summary(self):
        pin, other_pin = self.prepare()
        with patch.object(review, 'QUEUE_LIMIT_BYTES', 1), redirect_stderr(io.StringIO()), self.assertRaises(source.SourceError):
            self.export(pin, other_pin)
        self.assertTrue((self.output / review.QUEUE_FILE).exists())
        self.assertFalse((self.output / review.SUMMARY_FILE).exists())

    def test_long_fields_are_rejected_before_export_instead_of_omitting_unreviewed_edits(self):
        pin, other_pin = self.prepare(original='A' * 5000, other='B' * 5000)
        with self.assertRaises(source.SourceError):
            self.export(pin, other_pin)
        self.assertFalse(self.output.exists())
        with self.assertRaises(source.SourceError):
            review.edit_record((1, 1), 'A' * 5000, 'B' * 5000)

    def test_cli_reports_completed_export_without_semantic_approval_and_fails_on_error(self):
        pin, other_pin = self.prepare()
        args = [str(self.bundle), '--expected-manifest-sha256', pin, '--reference-archive', str(self.archive_file),
                '--output-directory', str(self.output)]
        output = io.StringIO()
        with patch.object(source, 'PROJECT_ROOT', self.root), patch.object(reference, 'ARCHIVE_SHA256', other_pin), redirect_stdout(output):
            self.assertEqual(0, review.main(args))
        summary = json.loads(output.getvalue())
        self.assertEqual('COMPLETE_DIAGNOSTIC_EXPORT', summary['result'])
        self.assertEqual('PENDING', summary['gates']['text_equivalence'])
        self.assertNotIn('SYNTHETIC B', output.getvalue())
        with redirect_stderr(io.StringIO()):
            self.assertEqual(1, review.main(args))


if __name__ == '__main__':
    unittest.main()
