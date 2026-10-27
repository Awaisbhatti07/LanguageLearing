"""Nonreligious synthetic Unicode fixtures for the source-only strict text audit.

No scripture quotation, network request, real source fixture or learner storage.
"""

from contextlib import redirect_stdout, redirect_stderr
import io
import json
from pathlib import Path
import sys
import tempfile
import unittest
from unittest.mock import patch

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
import tanzil_text_audit as audit
import tanzil_references as reference
import tanzil_source as source
from test_tanzil_source import snapshots, xml, STAMP
from test_tanzil_references import archive


def publisher_body(text, ayah=1):
    return text + ' \u06dd' + str(ayah).translate(audit.ARABIC_INDIC)


class TanzilTextAuditTest(unittest.TestCase):
    def setUp(self):
        self.temporary = tempfile.TemporaryDirectory()
        self.addCleanup(self.temporary.cleanup)
        self.root = Path(self.temporary.name).resolve()
        self.bundle = self.root / 'candidate'
        self.archive_file = self.root / 'reference.zip'

    def candidate(self, text='SYNTHETIC TOOL TEST', opening=False):
        raw = xml(text)
        if opening:
            raw = raw.replace(b"<aya index='1'", b"<aya bismillah='SYNTHETIC SEPARATE OPENING' index='1'")
        pin, _ = source.write_candidate(self.bundle, snapshots(raw), STAMP)
        return pin

    def run_audit(self, pin, publisher_text):
        data = archive(records=[{'sura_no': 1, 'aya_no': 1, 'aya_text_unicode': publisher_text}])
        self.archive_file.write_bytes(data)
        with patch.object(reference, 'ARCHIVE_SHA256', source.sha256(data)):
            return audit.audit_text(self.bundle, pin, self.archive_file)

    def test_number_trailer_is_diagnostic_and_does_not_turn_raw_fields_into_equal_text(self):
        text = 'SYNTHETIC TOOL TEST'
        report = self.run_audit(self.candidate(text), publisher_body(text))
        self.assertEqual('DIFFERENCES_FOUND', report['result'])
        checks = report['comparison']
        self.assertEqual(0, checks['raw_field_exact_matches'])
        self.assertEqual(1, checks['raw_field_differences'])
        self.assertEqual(1, checks['body_exact_matches_excluding_only_validated_number_trailer'])
        self.assertEqual(0, checks['body_differences_excluding_only_validated_number_trailer'])
        self.assertEqual('PENDING', report['gates']['text_equivalence'])
        self.assertEqual('NOT_APPROVED', report['gates']['app_import'])
        self.assertEqual('PENDING', report['gates']['m4_m5_review'])
        self.assertNotIn(text, json.dumps(report))

    def test_combining_sequences_are_not_nfc_normalized(self):
        original = 'SYNTHETIC Cafe\u0301 ا\u0654'
        report = self.run_audit(self.candidate(original), publisher_body('SYNTHETIC Café أ'))
        checks = report['comparison']
        self.assertEqual(1, checks['body_differences_excluding_only_validated_number_trailer'])
        self.assertNotEqual(checks['tanzil_text_inventory_sha256'], checks['publisher_body_text_inventory_sha256'])
        edits = checks['difference_samples'][0]['body_edits']
        self.assertTrue(any('U+0301' in edit['tanzil_codepoints_prefix'] for edit in edits))
        self.assertEqual('NONE', report['normalization'].split(';')[0])

    def test_missing_harakat_and_letter_or_annotation_changes_remain_differences(self):
        text = 'SYNTHETIC مَرْحَبًا'
        for other in ['SYNTHETIC مرحبا', 'SYNTHETIC مَرْحَبًى', 'SYNTHETIC مَرْحَبًا\u06e1']:
            checks = audit.compare_fields({(1, 1): text}, {(1, 1): publisher_body(other)}, {})
            self.assertEqual(1, checks['body_differences_excluding_only_validated_number_trailer'])

    def test_exact_body_whitespace_is_not_trimmed_or_collapsed(self):
        for other in [' SYNTHETIC', 'SYNTHETIC ', 'SYNTHETIC\n', 'SYNTHETIC\u00a0']:
            checks = audit.compare_fields({(1, 1): 'SYNTHETIC'}, {(1, 1): publisher_body(other)}, {})
            self.assertEqual(1, checks['body_differences_excluding_only_validated_number_trailer'])
        self.assertEqual('SYNTHETIC ', audit.numbered_body(publisher_body('SYNTHETIC '), 1))

    def test_number_trailers_require_the_exact_current_reference_number_and_separator(self):
        invalid = ['SYNTHETIC', 'SYNTHETIC \u06dd٢', 'SYNTHETIC \u06dd٠١',
                   'SYNTHETIC\u06dd١', 'SYNTHETIC \u06dd1', 'SYNTHETIC \u06dd١ ',
                   ' \u06dd١', 123, None, 'SYNTHETIC\x00 \u06dd١', 'SYNTHETIC\ud800 \u06dd١']
        for value in invalid:
            with self.subTest(value=repr(value)), self.assertRaises(source.SourceError):
                audit.numbered_body(value, 1)
        self.assertEqual('SYNTHETIC', audit.numbered_body(publisher_body('SYNTHETIC', 123), 123))

    def test_openings_are_kept_separate_uncompared_and_never_added_to_numbered_text(self):
        report = self.run_audit(self.candidate(opening=True), publisher_body('SYNTHETIC TOOL TEST'))
        self.assertEqual(1, report['comparison']['tanzil_separate_opening_field_count'])
        self.assertEqual(1, report['comparison']['body_exact_matches_excluding_only_validated_number_trailer'])
        self.assertEqual('NOT_CHECKED', report['gates']['unnumbered_opening_equivalence'])
        self.assertNotIn('SYNTHETIC SEPARATE OPENING', json.dumps(report))

    def test_diagnostic_samples_are_bounded_and_have_codepoint_offsets_instead_of_raw_text(self):
        left = {(1, i): 'SYNTHETIC A' for i in range(1, 30)}
        right = {(1, i): publisher_body('SYNTHETIC B', i) for i in range(1, 30)}
        checks = audit.compare_fields(left, right, {})
        self.assertEqual(29, checks['raw_field_differences'])
        self.assertEqual(audit.SAMPLE_LIMIT, len(checks['difference_samples']))
        self.assertTrue(checks['samples_truncated'])
        edit = checks['difference_samples'][0]['body_edits'][0]
        self.assertEqual([10, 11], edit['tanzil_codepoint_range'])
        self.assertEqual(['U+0041'], edit['tanzil_codepoints_prefix'])
        self.assertEqual(['U+0042'], edit['publisher_codepoints_prefix'])
        self.assertNotIn('SYNTHETIC A', json.dumps(checks))

    def test_edit_analysis_is_bounded_without_skipping_exact_comparison_or_hashing(self):
        sample = audit.difference_sample((1, 1), 'A' * 5000, publisher_body('B' * 5000), 'B' * 5000)
        self.assertIn('body_edits_omitted', sample)
        self.assertEqual([], sample['body_edits'])
        self.assertNotEqual(sample['tanzil_sha256'], sample['publisher_body_sha256'])
        sample = audit.difference_sample((1, 1), 'A' * 100, publisher_body('B' * 100), 'B' * 100)
        self.assertEqual(audit.EDIT_CODEPOINT_LIMIT, len(sample['body_edits'][0]['tanzil_codepoints_prefix']))
        self.assertTrue(sample['body_edits'][0]['codepoint_prefixes_truncated'])

    def test_character_diagnostics_and_hashes_do_not_ignore_references_or_field_boundaries(self):
        self.assertNotEqual(audit.text_inventory_digest({(1, 1): 'AB', (1, 2): 'C'}),
                            audit.text_inventory_digest({(1, 1): 'A', (1, 2): 'BC'}))
        self.assertNotEqual(audit.text_inventory_digest({(1, 1): 'A'}), audit.text_inventory_digest({(2, 1): 'A'}))
        checks = audit.compare_fields({(1, 1): 'A'}, {(1, 1): publisher_body('B')}, {})
        self.assertEqual([{'codepoint': 'U+0041', 'occurrences': 1}], checks['tanzil_only_body_codepoints'])
        self.assertEqual([{'codepoint': 'U+0042', 'occurrences': 1}], checks['publisher_only_body_codepoints'])

    def test_wrong_candidate_or_archive_pin_blocks_audit_before_text_claims(self):
        pin = self.candidate()
        self.archive_file.write_bytes(archive())
        with self.assertRaises(source.SourceError):
            audit.audit_text(self.bundle, '0' * 64, self.archive_file)
        with self.assertRaises(source.SourceError):
            audit.audit_text(self.bundle, pin, self.archive_file)

    def test_numbered_reference_mismatch_prevents_text_audit(self):
        pin = self.candidate()
        data = archive()  # Synthetic reference has two records, candidate only one.
        self.archive_file.write_bytes(data)
        with patch.object(reference, 'ARCHIVE_SHA256', source.sha256(data)), self.assertRaises(source.SourceError):
            audit.audit_text(self.bundle, pin, self.archive_file)

    def test_source_rewrite_after_initial_verification_is_rejected(self):
        pin = self.candidate()
        verify = source.verify_candidate
        def racing_verify(*args):
            result = verify(*args)
            (self.bundle / source.SOURCE_FILE).write_bytes(xml('SYNTHETIC CHANGED AFTER CHECK'))
            return result
        with patch.object(source, 'verify_candidate', side_effect=racing_verify), self.assertRaises(source.SourceError):
            self.run_audit(pin, publisher_body('SYNTHETIC TOOL TEST'))

    def test_audit_is_read_only_offline_and_leaves_original_pending_manifest_intact(self):
        pin = self.candidate()
        before = {p.name: (p.read_bytes(), p.stat().st_mtime_ns) for p in self.bundle.iterdir()}
        with patch.object(source, 'fetch', side_effect=AssertionError('Network forbidden')):
            self.run_audit(pin, publisher_body('SYNTHETIC TOOL TEST'))
        self.assertEqual(before, {p.name: (p.read_bytes(), p.stat().st_mtime_ns) for p in self.bundle.iterdir()})
        manifest = json.loads((self.bundle / source.MANIFEST_FILE).read_bytes())
        self.assertEqual('NOT_APPROVED', manifest['gates']['app_import'])
        self.assertEqual('PENDING', manifest['gates']['independent_reference_inventory'])

    def test_cli_uses_distinct_nonzero_codes_for_differences_and_integrity_failure(self):
        pin = self.candidate()
        data = archive(records=[{'sura_no': 1, 'aya_no': 1, 'aya_text_unicode': publisher_body('SYNTHETIC TOOL TEST')}])
        self.archive_file.write_bytes(data)
        args = [str(self.bundle), '--expected-manifest-sha256', pin, '--reference-archive', str(self.archive_file)]
        output = io.StringIO()
        with patch.object(reference, 'ARCHIVE_SHA256', source.sha256(data)), redirect_stdout(output):
            self.assertEqual(1, audit.main(args))
        self.assertEqual('DIFFERENCES_FOUND', json.loads(output.getvalue())['result'])
        self.assertNotIn('SYNTHETIC TOOL TEST', output.getvalue())
        with redirect_stderr(io.StringIO()):
            self.assertEqual(2, audit.main(args))


if __name__ == '__main__':
    unittest.main()
