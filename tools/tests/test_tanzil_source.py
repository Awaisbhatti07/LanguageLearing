"""Offline tests. Every source-shaped document here is a NONRELIGIOUS synthetic fixture.

These fixtures are not Quran text, references, licenses or a trusted source inventory.
No test fetches data from Tanzil or reads app/learner storage.
"""

from contextlib import redirect_stderr, redirect_stdout
from html import escape
import io
import json
from pathlib import Path
import sys
import tempfile
import unittest
from unittest.mock import patch
from urllib.error import URLError
from urllib.request import Request
from xml.sax.saxutils import quoteattr

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
import tanzil_source as t


# Deliberately synthetic notice text with the anchors needed to exercise matching.
NOTICE = ("\nTanzil Quran Text\nCopyright (C) SYNTHETIC TEST ONLY\n"
          "License: Creative Commons Attribution 3.0\nTERMS OF USE:\n"
          "SYNTHETIC NOTICE. CHANGING IT IS NOT ALLOWED. tanzil.net\n")
NOTICE_HTML = ("<html><pre class='code'>" + escape(NOTICE) + "</pre></html>").encode()
PAGE = ("<html>Version 1.1<form action='/pub/download/index.php'>"
        "<select name='quranType'><option value='uthmani'>Uthmani</option></select>"
        "<select name='outType'><option value='xml'>XML</option></select>"
        + "".join(f"<input name='{name}' value='true'>" for name in ('marks', 'sajdah', 'tatweel', 'agree'))
        + "</form></html>").encode()
STAMP = "2026-10-27T00:00:00Z"


def xml(text="SYNTHETIC NONRELIGIOUS TOOL TEST", extra=""):
    copyright_and_terms = "Copyright" + NOTICE.split("Copyright", 1)[1]
    return ("<?xml version='1.0' encoding='utf-8'?>\n<!--Tanzil Quran Text (Version 1.1)\n"
            + copyright_and_terms + "-->\n<quran><sura index='1' name='SYNTHETIC'>"
            + f"<aya index='1' text={quoteattr(text)}/>{extra}</sura></quran>").encode()


def snapshots(data=None, page=PAGE, notice=NOTICE_HTML):
    return {name: t.Snapshot(content, t.URLS[name], t.URLS[name], "text/plain; charset=utf-8")
            for name, content in ((t.SOURCE_FILE, xml() if data is None else data),
                                  ("download.html", page), ("text-license.html", notice))}


class Response(io.BytesIO):
    def __init__(self, data, headers=None, status=200, url=t.SOURCE_URL):
        super().__init__(data)
        self.headers = {} if headers is None else headers
        self.status = status
        self.url = url

    def geturl(self):
        return self.url


class TanzilSourceTest(unittest.TestCase):
    def setUp(self):
        self.temporary = tempfile.TemporaryDirectory()
        self.addCleanup(self.temporary.cleanup)
        self.root = Path(self.temporary.name).resolve()
        self.bundle = self.root / "candidate"

    def write(self, source=None):
        return t.write_candidate(self.bundle, snapshots(source), STAMP)

    def repin(self, change):
        path = self.bundle / t.MANIFEST_FILE
        manifest = json.loads(path.read_bytes())
        change(manifest)
        path.write_bytes(json.dumps(manifest).encode())
        return t.sha256(path.read_bytes())

    def test_raw_unicode_notice_and_opening_are_preserved_without_normalization(self):
        own_text = "SYNTHETIC: ا\u0654 / اختبارُ الأداة / & \u200d"
        raw = xml(own_text).replace(b"<aya index='1'", b"<aya bismillah='SYNTHETIC OPENING' index='1'")
        pin, checks = self.write(raw)
        self.assertEqual(raw, (self.bundle / t.SOURCE_FILE).read_bytes())
        self.assertEqual(NOTICE.encode(), (self.bundle / t.PAGE_NOTICE_FILE).read_bytes())
        embedded = t.check_embedded_notice(raw.decode(), NOTICE.encode())
        self.assertEqual(embedded, (self.bundle / t.NOTICE_FILE).read_bytes())
        self.assertEqual(1, checks['separate_opening_fields_observed'])
        self.assertEqual(checks, t.verify_candidate(self.bundle, pin))
        normalized = raw.replace("ا\u0654".encode(), "أ".encode())
        self.assertNotEqual(checks['decoded_fields_sha256'], t.inspect_xml(normalized, NOTICE.encode())['decoded_fields_sha256'])

    def test_offline_verification_is_read_only_and_makes_no_network_request(self):
        pin, checks = self.write()
        before = {p.name: (p.read_bytes(), p.stat().st_mtime_ns) for p in self.bundle.iterdir()}
        with patch.object(t, 'fetch', side_effect=AssertionError('Network forbidden')):
            self.assertEqual(checks, t.verify_candidate(self.bundle, pin))
        self.assertEqual(before, {p.name: (p.read_bytes(), p.stat().st_mtime_ns) for p in self.bundle.iterdir()})
        self.assertEqual('PENDING', t.GATES['independent_reference_inventory'])
        self.assertEqual('NOT_APPROVED', t.GATES['app_import'])

    def test_missing_wrong_or_self_rewritten_manifest_pin_is_rejected(self):
        pin, _ = self.write()
        for invalid in ['', 'abc', '0' * 64, pin.upper()]:
            with self.subTest(pin=invalid), self.assertRaises(t.SourceError):
                t.verify_candidate(self.bundle, invalid)
        changed = xml('SYNTHETIC CHANGED BYTES')
        (self.bundle / t.SOURCE_FILE).write_bytes(changed)
        new_pin = self.repin(lambda m: m['files'][t.SOURCE_FILE].update(bytes=len(changed), sha256=t.sha256(changed)))
        self.assertNotEqual(pin, new_pin)
        with self.assertRaisesRegex(t.SourceError, 'supplied pin'):
            t.verify_candidate(self.bundle, pin)

    def test_each_saved_source_or_notice_change_breaks_integrity(self):
        for name in list(t.URLS) + [t.NOTICE_FILE, t.PAGE_NOTICE_FILE]:
            with self.subTest(file=name):
                self.bundle = self.root / name.replace('.', '-')
                pin, _ = self.write()
                with (self.bundle / name).open('ab') as output:
                    output.write(b'changed')
                with self.assertRaises(t.SourceError):
                    t.verify_candidate(self.bundle, pin)

    def test_pinned_manifest_cannot_promote_approval_change_selection_or_paths(self):
        self.write()
        original = (self.bundle / t.MANIFEST_FILE).read_bytes()
        changes = [lambda m: m['gates'].update(app_import='APPROVED'),
                   lambda m: m['selection'].update(quranType='simple'),
                   lambda m: m.update(source_version='2.0'),
                   lambda m: m.update(schema_version=True),
                   lambda m: m['files'].update({'../../other': m['files'].pop(t.NOTICE_FILE)}),
                   lambda m: m['requests'][t.SOURCE_FILE].update(final_url='https://example.com/source'),
                   lambda m: m['requests'][t.SOURCE_FILE].update(requested_url=t.DOWNLOAD_PAGE),
                   lambda m: m['checks'].update(ayah_count_observed=True)]
        for change in changes:
            with self.subTest(change=change):
                (self.bundle / t.MANIFEST_FILE).write_bytes(original)
                with self.assertRaises(t.SourceError):
                    t.verify_candidate(self.bundle, self.repin(change))

    def test_tampered_notice_rejected_even_if_hash_and_manifest_are_replaced(self):
        self.write()
        altered = NOTICE.encode() + b'EXTRA NOTICE CHANGE'
        (self.bundle / t.NOTICE_FILE).write_bytes(altered)
        pin = self.repin(lambda m: m['files'][t.NOTICE_FILE].update(bytes=len(altered), sha256=t.sha256(altered)))
        with self.assertRaisesRegex(t.SourceError, 'Saved notice'):
            t.verify_candidate(self.bundle, pin)

    def test_symlink_payload_and_manifest_are_rejected(self):
        pin, _ = self.write()
        for name in [t.SOURCE_FILE, t.MANIFEST_FILE]:
            path = self.bundle / name
            content = path.read_bytes()
            other = self.root / ('outside-' + name)
            path.rename(other)
            path.symlink_to(other)
            with self.subTest(name=name), self.assertRaises(t.SourceError):
                t.verify_candidate(self.bundle, pin)
            path.unlink()
            path.write_bytes(content)

    def test_existing_candidate_is_never_overwritten(self):
        pin, _ = self.write()
        before = (self.bundle / t.SOURCE_FILE).read_bytes()
        with self.assertRaises(FileExistsError):
            self.write(xml('SYNTHETIC REPLACEMENT'))
        self.assertEqual(before, (self.bundle / t.SOURCE_FILE).read_bytes())
        t.verify_candidate(self.bundle, pin)

    def test_failed_candidate_retains_raw_bytes_without_completion_manifest(self):
        bad = b'not XML'
        with redirect_stderr(io.StringIO()), self.assertRaises(t.SourceError):
            self.write(bad)
        self.assertEqual(bad, (self.bundle / t.SOURCE_FILE).read_bytes())
        self.assertFalse((self.bundle / t.MANIFEST_FILE).exists())
        with self.assertRaises(t.SourceError):
            t.verify_candidate(self.bundle, '0' * 64)

    def test_xml_rejects_entities_encodings_processing_instructions_and_malformed_data(self):
        base = xml()
        cases = [base.replace(b'<quran>', b'<!DOCTYPE quran [<!ENTITY x "EXPAND">]><quran>'),
                 base.replace(b'<quran>', b'<?external target?><quran>'),
                 base.replace(b'<quran>', b'<quran xmlns="other">'),
                 base.replace(b'</quran>', b''), base + b'\xff', base + b'\x00',
                 base.decode().encode('utf-16'), base.replace(b'utf-8', b'latin-1'),
                 base.replace(b'SYNTHETIC NONRELIGIOUS TOOL TEST', b'&unknown;'),
                 base.replace(b'SYNTHETIC NONRELIGIOUS TOOL TEST', b'SYNTHETIC\nWRAPPED FIELD'),
                 base.replace(b'<quran>', b'<quran>unexpected mixed text')]
        for raw in cases:
            with self.subTest(raw=cases.index(raw)), self.assertRaises(t.SourceError):
                t.inspect_xml(raw, NOTICE.encode())

    def test_xml_rejects_reference_gaps_duplicates_unknown_attributes_and_empty_fields(self):
        base = xml()
        cases = [base.replace(b"index='1'", b"index='2'", 1),
                 base.replace(b"index='1'", b"index='01'", 1),
                 base.replace(b"<aya index='1'", b"<aya index='2'"),
                 base.replace(b"<aya index='1'", b"<aya unexpected='x' index='1'"),
                 base.replace(b'text="SYNTHETIC NONRELIGIOUS TOOL TEST"', b'text=" "'),
                 base.replace(b"name='SYNTHETIC'", b"name=''"),
                 xml(extra="<aya index='1' text='SYNTHETIC DUPLICATE'/>"),
                 xml(extra="<aya index='3' text='SYNTHETIC GAP'/>"),
                 xml(extra="<aya index='2' text='SYNTHETIC' bismillah='SYNTHETIC LATE OPENING'/>"),
                 base.replace(b'/></sura>', b'><nested/></aya></sura>')]
        for number, raw in enumerate(cases):
            with self.subTest(case=number), self.assertRaises(t.SourceError):
                t.inspect_xml(raw, NOTICE.encode())

    def test_structural_counts_do_not_claim_independent_completeness(self):
        pin, checks = self.write(xml(extra="<aya index='2' text='SYNTHETIC SECOND RECORD'/>"))
        self.assertEqual([2], checks['ayah_counts_by_chapter_observed'])
        self.assertEqual(2, checks['ayah_count_observed'])
        self.assertEqual('PENDING', json.loads((self.bundle / t.MANIFEST_FILE).read_bytes())['gates']['independent_reference_inventory'])
        t.verify_candidate(self.bundle, pin)

    def test_xml_and_record_resource_limits_are_enforced(self):
        with patch.object(t, 'XML_LIMIT', 10), self.assertRaises(t.SourceError):
            t.inspect_xml(xml(), NOTICE.encode())
        with patch.object(t, 'MAX_RECORDS', 1), self.assertRaises(t.SourceError):
            t.inspect_xml(xml(extra="<aya index='2' text='SYNTHETIC'/>"), NOTICE.encode())
        with patch.object(t, 'MAX_CHAPTERS', 0), self.assertRaises(t.SourceError):
            t.inspect_xml(xml(), NOTICE.encode())

    def test_notice_and_source_release_mismatches_are_not_repaired(self):
        cases = [xml().replace(b'Version 1.1', b'Version 2.0'),
                 xml().replace(b'CHANGING IT IS NOT ALLOWED', b'MODIFIED TERMS'),
                 xml().replace(b'<!--', b'<!--SYNTHETIC OTHER NOTICE--><!--').replace(b'Tanzil Quran Text', b'Other Text')]
        for raw in cases:
            with self.subTest(raw=cases.index(raw)), self.assertRaises(t.SourceError):
                t.inspect_xml(raw, NOTICE.encode())
        with self.assertRaises(t.SourceError):
            t.notice_from_page(b'<pre>Incomplete notice</pre>')
        with self.assertRaises(t.SourceError):
            t.notice_from_page(NOTICE_HTML + NOTICE_HTML)

    def test_distinct_notice_headers_are_preserved_and_extra_usage_terms_are_rejected(self):
        raw = xml().replace(b'Copyright (C) SYNTHETIC TEST ONLY', b'Copyright (C) DIFFERENT SYNTHETIC HEADER')
        pin, _ = self.write(raw)
        self.assertIn(b'DIFFERENT SYNTHETIC HEADER', (self.bundle / t.NOTICE_FILE).read_bytes())
        self.assertEqual(NOTICE.encode(), (self.bundle / t.PAGE_NOTICE_FILE).read_bytes())
        t.verify_candidate(self.bundle, pin)
        changed_terms = raw.replace(b'tanzil.net', b'tanzil.net ADDITIONAL USAGE CONDITION')
        with self.assertRaises(t.SourceError):
            t.inspect_xml(changed_terms, NOTICE.encode())

    def test_official_comment_decoration_is_preserved_without_ignoring_term_punctuation(self):
        raw = xml()
        start, end = raw.index(b'<!--') + 4, raw.index(b'-->')
        comment = '#================\n' + '\n'.join('# ' + line for line in raw[start:end].decode().splitlines()) + '\n#================\n'
        decorated = raw[:start] + comment.encode() + raw[end:]
        pin, _ = self.write(decorated)
        self.assertEqual(comment.encode(), (self.bundle / t.NOTICE_FILE).read_bytes())
        t.verify_candidate(self.bundle, pin)
        with self.assertRaises(t.SourceError):
            t.inspect_xml(decorated.replace(b'NOT ALLOWED.', b'NOT ALLOWED!'), NOTICE.encode())

    def test_download_page_release_and_required_options_must_match(self):
        for raw in [PAGE.replace(b'1.1', b'2.0'), PAGE.replace(b'uthmani', b'simple'),
                    PAGE.replace(b'name=\'marks\'', b'name=\'other\''),
                    PAGE.replace(b'/pub/download/index.php', b'https://elsewhere.invalid/')]:
            with self.subTest(raw=raw), self.assertRaises(t.SourceError):
                t.check_download_page(raw)

    def test_http_read_rejects_oversize_truncated_encoded_empty_and_non_success_data(self):
        cases = [Response(b'12345'), Response(b'123', {'Content-Length': '9999'}),
                 Response(b'1', {'Content-Length': '3'}), Response(b'1', {'Content-Length': '-1'}),
                 Response(b'1', {'Content-Encoding': 'gzip'}), Response(b''),
                 Response(b'1', status=206), Response(b'1', url='http://tanzil.net/source')]
        for response in cases:
            with self.subTest(response=response), self.assertRaises(t.SourceError):
                t.read_response(response, 4)
        self.assertEqual(b'123', t.read_response(Response(b'123', {'Content-Length': '3'}), 4))

    def test_redirects_reject_external_hosts_credentials_and_http_before_following(self):
        request = Request(t.SOURCE_URL)
        for url in ['http://tanzil.net/source', 'https://tanzil.net.evil.invalid/source',
                    'https://user@tanzil.net/source', 'https://tanzil.net:443/source',
                    'file:///private/tmp/source', 'https://tanzil.net/source\nX:secret']:
            with self.subTest(url=url), self.assertRaises(t.SourceError):
                t.TanzilRedirects().redirect_request(request, None, 302, 'Found', {}, url)
        follow = t.TanzilRedirects().redirect_request(request, None, 302, 'Found', {}, 'https://tanzil.net/source.xml')
        self.assertEqual('https://tanzil.net/source.xml', follow.full_url)

    def test_fetch_uses_restricted_redirect_handler_and_bounded_response(self):
        response = Response(b'123', {'Content-Length': '3'})
        with patch.object(t, 'build_opener') as factory:
            factory.return_value.open.return_value = response
            result = t.fetch(t.SOURCE_URL, 4)
            self.assertIsInstance(factory.call_args.args[0], t.TanzilRedirects)
            self.assertEqual(30, factory.return_value.open.call_args.kwargs['timeout'])
            self.assertEqual(b'123', result.data)

    def test_download_stops_before_text_if_page_or_license_changes(self):
        bad = snapshots(page=PAGE.replace(b'1.1', b'2.0'))
        with patch.object(t, 'fetch', side_effect=lambda url, limit: next(s for s in bad.values() if s.requested_url == url)) as fetch:
            with self.assertRaises(t.SourceError):
                t.download_sources()
            self.assertNotIn(t.SOURCE_URL, [call.args[0] for call in fetch.call_args_list])

    def test_manifest_rejects_duplicate_keys_and_missing_fields(self):
        self.write()
        path = self.bundle / t.MANIFEST_FILE
        data = path.read_bytes().replace(b'"schema_version": 1', b'"schema_version": 1, "schema_version": 1')
        path.write_bytes(data)
        with self.assertRaisesRegex(t.SourceError, 'Duplicate'):
            t.verify_candidate(self.bundle, t.sha256(data))
        path.write_bytes(b'{}')
        with self.assertRaises(t.SourceError):
            t.verify_candidate(self.bundle, t.sha256(b'{}'))

    def test_storage_refuses_symlink_escape_from_ignored_project_directory(self):
        project = self.root / 'project'
        project.mkdir()
        (project / '.local').symlink_to(self.root, target_is_directory=True)
        with patch.object(t, 'PROJECT_ROOT', project), patch.object(t, 'CANDIDATE_ROOT', project / '.local' / 'quranic'):
            with self.assertRaises(t.SourceError):
                t.candidate_path(STAMP, '0' * 64)

    def test_cli_requires_terms_acknowledgement_and_does_not_download_on_failure(self):
        with patch.object(t, 'download_sources') as download, redirect_stderr(io.StringIO()):
            self.assertEqual(1, t.main(['acquire']))
            download.assert_not_called()

    def test_cli_acquisition_and_offline_verify_have_no_approval_or_text_output(self):
        project = self.root / 'project'
        project.mkdir()
        output = io.StringIO()
        with patch.object(t, 'PROJECT_ROOT', project), patch.object(t, 'CANDIDATE_ROOT', project / '.local' / 'quranic'), patch.object(t, 'download_sources', return_value=snapshots()), redirect_stdout(output):
            self.assertEqual(0, t.main(['acquire', '--accept-tanzil-terms']))
        receipt = json.loads(output.getvalue())
        self.assertEqual('NOT_APPROVED', receipt['gates']['app_import'])
        self.assertNotIn('SYNTHETIC NONRELIGIOUS', output.getvalue())
        with patch.object(t, 'fetch', side_effect=AssertionError('Network forbidden')), redirect_stdout(io.StringIO()):
            self.assertEqual(0, t.main(['verify', receipt['bundle'], '--expected-manifest-sha256', receipt['manifest_sha256']]))

    def test_network_failure_does_not_create_candidate_or_claim_success(self):
        with patch.object(t, 'download_sources', side_effect=URLError('OFFLINE')), redirect_stderr(io.StringIO()):
            self.assertEqual(1, t.main(['acquire', '--accept-tanzil-terms']))
        self.assertEqual([], list(self.root.iterdir()))


if __name__ == '__main__':
    unittest.main()
