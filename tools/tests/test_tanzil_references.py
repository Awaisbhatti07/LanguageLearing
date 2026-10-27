"""Synthetic, nonreligious fixtures for the separate-publisher inventory comparison.

No real scripture, real reference inventory, network call or learner storage is used.
"""

from contextlib import redirect_stdout, redirect_stderr
import io
import json
from pathlib import Path
import stat
import sys
import tempfile
import unittest
from unittest.mock import patch
import warnings
import zipfile

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
import tanzil_references as reference
import tanzil_source as source
from test_tanzil_source import xml, snapshots, STAMP


def archive(records=None, raw_json=None, member=reference.JSON_MEMBER, extras=None):
    data = json.dumps([{'sura_no': 1, 'aya_no': 1}, {'sura_no': 1, 'aya_no': 2}]
                      if records is None else records).encode() if raw_json is None else raw_json
    output = io.BytesIO()
    with warnings.catch_warnings(), zipfile.ZipFile(output, 'w', compression=zipfile.ZIP_DEFLATED) as zipped:
        warnings.simplefilter('ignore', UserWarning)
        zipped.writestr(member, data)
        for name, value in extras or []:
            zipped.writestr(name, value)
    return output.getvalue()


class TanzilReferenceTest(unittest.TestCase):
    def setUp(self):
        self.temporary = tempfile.TemporaryDirectory()
        self.addCleanup(self.temporary.cleanup)
        self.root = Path(self.temporary.name).resolve()
        self.bundle = self.root / 'candidate'
        self.reference_file = self.root / 'reference.zip'

    def candidate(self, two_records=True, raw=None):
        if raw is None:
            raw = xml(extra="<aya index='2' text='SYNTHETIC SECOND RECORD'/>" if two_records else '')
        pin, _ = source.write_candidate(self.bundle, snapshots(raw), STAMP)
        return pin

    def compare(self, pin, data):
        self.reference_file.write_bytes(data)
        # Only synthetic tests replace the fixed publisher pin; the CLI has no override.
        with patch.object(reference, 'ARCHIVE_SHA256', source.sha256(data)):
            return reference.compare_references(self.bundle, pin, self.reference_file)

    def test_matching_inventory_checks_each_reference_without_promoting_other_gates(self):
        result = self.compare(self.candidate(), archive())
        self.assertEqual('MATCH', result['result'])
        self.assertEqual(2, result['comparison']['matched_reference_count'])
        self.assertEqual([], result['comparison']['missing_references'])
        self.assertEqual(result['tanzil']['reference_inventory_sha256'],
                         result['independent_reference']['reference_inventory_sha256'])
        self.assertEqual('NOT_APPROVED', result['gates']['app_import'])
        self.assertEqual('PENDING', result['gates']['m4_m5_review'])
        self.assertEqual('NOT_CHECKED', result['gates']['text_equivalence'])

    def test_valid_well_formed_missing_final_record_is_detected(self):
        pin = self.candidate(two_records=False)
        source.verify_candidate(self.bundle, pin)  # Structural/byte checks alone still pass.
        result = self.compare(pin, archive())
        self.assertEqual('MISMATCH', result['result'])
        self.assertEqual([[1, 2]], [list(pair) for pair in result['comparison']['missing_references']])
        self.assertEqual(0, result['comparison']['unexpected_reference_count'])

    def test_extra_final_record_and_missing_final_chapter_are_detected(self):
        pin = self.candidate()
        result = self.compare(pin, archive(records=[{'sura_no': 1, 'aya_no': 1}]))
        self.assertEqual([(1, 2)], result['comparison']['unexpected_references'])
        other = archive(records=[{'sura_no': 1, 'aya_no': 1}, {'sura_no': 1, 'aya_no': 2},
                                 {'sura_no': 2, 'aya_no': 1}])
        result = self.compare(pin, other)
        self.assertEqual([(2, 1)], result['comparison']['missing_references'])

    def test_equal_totals_with_different_chapter_boundaries_are_not_accepted(self):
        raw = xml(extra="<aya index='2' text='SYNTHETIC' />").replace(
            b'</quran>', b"<sura index='2' name='SYNTHETIC TWO'><aya index='1' text='SYNTHETIC'/></sura></quran>")
        pin = self.candidate(raw=raw)
        other = archive(records=[{'sura_no': 1, 'aya_no': 1}, {'sura_no': 2, 'aya_no': 1},
                                 {'sura_no': 2, 'aya_no': 2}])
        result = self.compare(pin, other)
        self.assertEqual(result['tanzil']['numbered_reference_count'], result['independent_reference']['numbered_reference_count'])
        self.assertEqual('MISMATCH', result['result'])
        self.assertEqual([(1, 2)], result['comparison']['unexpected_references'])
        self.assertEqual([(2, 2)], result['comparison']['missing_references'])

    def test_candidate_pin_or_source_tampering_prevents_comparison(self):
        pin = self.candidate()
        self.reference_file.write_bytes(archive())
        with self.assertRaises(source.SourceError):
            reference.compare_references(self.bundle, '0' * 64, self.reference_file)
        (self.bundle / source.SOURCE_FILE).write_bytes(b'ALTERED SYNTHETIC BYTES')
        with self.assertRaises(source.SourceError):
            reference.compare_references(self.bundle, pin, self.reference_file)

    def test_archive_checksum_is_checked_before_any_member_parsing(self):
        data = archive()
        with patch.object(reference.zipfile, 'ZipFile') as reader, self.assertRaises(source.SourceError):
            reference.reference_inventory(data, '0' * 64)
        reader.assert_not_called()

    def test_bad_zip_missing_member_duplicate_member_and_unsafe_paths_are_rejected(self):
        cases = [b'NOT A ZIP', archive(member='unexpected.json'),
                 archive(extras=[(reference.JSON_MEMBER, b'[]')]),
                 archive(extras=[('../escape', b'NO EXTRACTION')]),
                 archive(extras=[('/absolute', b'NO EXTRACTION')]),
                 archive(extras=[('C:\\escape', b'NO EXTRACTION')])]
        for data in cases:
            with self.subTest(case=cases.index(data)), self.assertRaises(source.SourceError):
                reference.reference_inventory(data, source.sha256(data))
        self.assertFalse((self.root / 'escape').exists())

    def test_symlink_member_and_archive_bombs_are_rejected(self):
        output = io.BytesIO()
        with zipfile.ZipFile(output, 'w') as zipped:
            info = zipfile.ZipInfo(reference.JSON_MEMBER)
            info.create_system = 3
            info.external_attr = (stat.S_IFLNK | 0o777) << 16
            zipped.writestr(info, b'outside')
        data = output.getvalue()
        with self.assertRaises(source.SourceError):
            reference.reference_inventory(data, source.sha256(data))
        data = archive()
        with patch.object(reference, 'MEMBER_LIMIT', 2), self.assertRaises(source.SourceError):
            reference.reference_inventory(data, source.sha256(data))
        with patch.object(reference, 'ENTRY_LIMIT', 0), self.assertRaises(source.SourceError):
            reference.reference_inventory(data, source.sha256(data))

    def test_corrupt_compressed_member_is_rejected_without_a_partial_match(self):
        data = bytearray(archive())
        with zipfile.ZipFile(io.BytesIO(data)) as zipped:
            info = zipped.getinfo(reference.JSON_MEMBER)
        local = info.header_offset
        name_length = int.from_bytes(data[local + 26:local + 28], 'little')
        extra_length = int.from_bytes(data[local + 28:local + 30], 'little')
        data[local + 30 + name_length + extra_length] ^= 255
        corrupted = bytes(data)
        with self.assertRaises(source.SourceError):
            reference.reference_inventory(corrupted, source.sha256(corrupted))

    def test_reference_schema_types_duplicates_gaps_and_order_are_strict(self):
        cases = [{}, [], [{'sura_no': True, 'aya_no': 1}], [{'sura_no': '1', 'aya_no': 1}],
                 [{'sura_no': 1, 'aya_no': 0}], [{'sura_no': 2, 'aya_no': 1}],
                 [{'sura_no': 1, 'aya_no': 1}, {'sura_no': 1, 'aya_no': 1}],
                 [{'sura_no': 1, 'aya_no': 1}, {'sura_no': 1, 'aya_no': 3}],
                 [{'sura_no': 1, 'aya_no': 2}, {'sura_no': 1, 'aya_no': 1}],
                 [{'sura_no': 1, 'aya_no': 1}, {'sura_no': 3, 'aya_no': 1}]]
        for records in cases:
            with self.subTest(records=records), self.assertRaises(source.SourceError):
                reference.parse_inventory(json.dumps(records).encode())
        for data in [b'\xff', b'[', b'[{"sura_no":1,"sura_no":1,"aya_no":1}]', b'[NaN]']:
            with self.subTest(data=data), self.assertRaises(source.SourceError):
                reference.parse_inventory(data)

    def test_only_numeric_reference_fields_affect_comparison(self):
        pin = self.candidate()
        data = archive(records=[{'sura_no': 1, 'aya_no': 1, 'aya_text_unicode': 'SYNTHETIC DIFFERENT TEXT'},
                                {'sura_no': 1, 'aya_no': 2, 'aya_text_unicode': 'SYNTHETIC OTHER TEXT'}])
        result = self.compare(pin, data)
        self.assertEqual('MATCH', result['result'])
        self.assertEqual('NOT_CHECKED', result['gates']['text_equivalence'])

    def test_comparison_is_offline_and_does_not_modify_candidate_or_receipts(self):
        pin = self.candidate()
        data = archive()
        before = {p.name: (p.read_bytes(), p.stat().st_mtime_ns) for p in self.bundle.iterdir()}
        with patch.object(source, 'fetch', side_effect=AssertionError('Network forbidden')):
            self.compare(pin, data)
        self.assertEqual(before, {p.name: (p.read_bytes(), p.stat().st_mtime_ns) for p in self.bundle.iterdir()})
        original = json.loads((self.bundle / source.MANIFEST_FILE).read_bytes())
        self.assertEqual('PENDING', original['gates']['independent_reference_inventory'])
        self.assertEqual('NOT_APPROVED', original['gates']['app_import'])

    def test_cli_returns_mismatch_failure_and_does_not_print_text_or_approve_import(self):
        pin = self.candidate(two_records=False)
        data = archive()
        self.reference_file.write_bytes(data)
        output = io.StringIO()
        with patch.object(reference, 'ARCHIVE_SHA256', source.sha256(data)), redirect_stdout(output):
            code = reference.main([str(self.bundle), '--expected-manifest-sha256', pin,
                                   '--reference-archive', str(self.reference_file)])
        self.assertEqual(1, code)
        result = json.loads(output.getvalue())
        self.assertEqual('MISMATCH', result['result'])
        self.assertNotIn('SYNTHETIC DIFFERENT TEXT', output.getvalue())
        self.assertEqual('NOT_APPROVED', result['gates']['app_import'])
        with redirect_stderr(io.StringIO()):
            self.assertEqual(1, reference.main([str(self.bundle), '--expected-manifest-sha256', '0' * 64,
                                               '--reference-archive', str(self.reference_file)]))


if __name__ == '__main__':
    unittest.main()
