#!/usr/bin/env python3
"""Acquire a Tanzil-only source candidate or check its pinned bytes offline.

This tool never imports app content or grants religious/linguistic approval.
Only Python's standard library is used. Run --help for explicit commands.
"""

import argparse
from dataclasses import dataclass
from datetime import datetime, timezone
import hashlib
from html.parser import HTMLParser
import json
from pathlib import Path
import re
import struct
import sys
from urllib.error import URLError
from urllib.parse import urlencode, urlsplit
from urllib.request import HTTPRedirectHandler, Request, build_opener
import xml.etree.ElementTree as ET


PROJECT_ROOT = Path(__file__).resolve().parents[1]
CANDIDATE_ROOT = PROJECT_ROOT / ".local" / "quranic" / "tanzil"
VERSION = "1.1"
DOWNLOAD_PAGE = "https://tanzil.net/download/"
LICENSE_PAGE = "https://tanzil.net/docs/text_license"
OPTIONS = {"quranType": "uthmani", "outType": "xml", "marks": "true",
           "sajdah": "true", "tatweel": "true", "agree": "true"}
SOURCE_URL = "https://tanzil.net/pub/download/index.php?" + urlencode(OPTIONS)
SOURCE_FILE = "tanzil-uthmani.xml"
NOTICE_FILE = "NOTICE.txt"
PAGE_NOTICE_FILE = "LICENSE_PAGE_NOTICE.txt"
MANIFEST_FILE = "manifest.json"
URLS = {"download.html": DOWNLOAD_PAGE, "text-license.html": LICENSE_PAGE,
        SOURCE_FILE: SOURCE_URL}
HTML_LIMIT = 256 * 1024
XML_LIMIT = 16 * 1024 * 1024
JSON_LIMIT = 128 * 1024
MAX_CHAPTERS = 1000  # Resource limits, not an asserted religious reference inventory.
MAX_RECORDS = 100000
GATES = {"app_import": "NOT_APPROVED", "qualified_review": "PENDING",
         "independent_reference_inventory": "PENDING",
         "word_alignment_meanings_morphology": "NOT_INCLUDED"}
NOTICE_ANCHORS = ("Tanzil Quran Text", "Copyright", "Creative Commons Attribution 3.0",
                  "TERMS OF USE:", "CHANGING IT IS NOT ALLOWED", "tanzil.net")


class SourceError(ValueError):
    """The candidate does not satisfy this tool's source-only contract."""


def require(condition, message):
    if not condition:
        raise SourceError(message)


def sha256(data):
    return hashlib.sha256(data).hexdigest()


def official_url(url):
    require(isinstance(url, str) and len(url) <= 4096
            and not any(ord(c) < 32 or ord(c) == 127 for c in url), "Invalid source URL")
    parts = urlsplit(url)
    require(parts.scheme == "https" and parts.netloc == "tanzil.net" and not parts.fragment,
            "Only canonical HTTPS Tanzil URLs are allowed")
    return url


class TanzilRedirects(HTTPRedirectHandler):
    def redirect_request(self, req, fp, code, msg, headers, newurl):
        # Validate BEFORE urllib issues the redirected request, including HTTPS downgrades.
        official_url(newurl)
        return super().redirect_request(req, fp, code, msg, headers, newurl)


@dataclass(frozen=True)
class Snapshot:
    data: bytes
    requested_url: str
    final_url: str
    content_type: str

    def receipt(self):
        return {"requested_url": self.requested_url, "final_url": self.final_url,
                "content_type": self.content_type, "http_status": 200}


def read_response(response, limit):
    require(response.status == 200, "Source response must be HTTP 200")
    official_url(response.geturl())
    require(response.headers.get("Content-Encoding", "identity").lower() == "identity",
            "Encoded source responses are not accepted")
    length = response.headers.get("Content-Length")
    if length is not None:
        require(re.fullmatch(r"[0-9]+", length) is not None and len(length) <= 12,
                "Invalid Content-Length")
        require(int(length) <= limit, "Source exceeds size limit")
    data = response.read(limit + 1)
    require(0 < len(data) <= limit, "Empty or oversized source response")
    if length is not None:
        require(len(data) == int(length), "Truncated source response")
    return data


def fetch(url, limit):
    official_url(url)
    request = Request(url, headers={"User-Agent": "LingoMate-source-tooling/1.0",
                                    "Accept-Encoding": "identity"})
    with build_opener(TanzilRedirects()).open(request, timeout=30) as response:
        data = read_response(response, limit)
        return Snapshot(data, url, response.geturl(), response.headers.get("Content-Type", ""))


class PageParser(HTMLParser):
    def __init__(self):
        super().__init__(convert_charrefs=True)
        self.text = []
        self.pre_blocks = []
        self._pre = None
        self.form_actions = []
        self.inputs = {}
        self.selects = {}
        self._select = None

    def handle_starttag(self, tag, attrs):
        attrs = dict(attrs)
        if tag == "pre":
            require(self._pre is None, "Nested notice markup")
            self._pre = []
        elif tag == "form":
            self.form_actions.append(attrs.get("action"))
        elif tag == "input" and "name" in attrs:
            self.inputs[attrs["name"]] = attrs.get("value")
        elif tag == "select":
            self._select = attrs.get("name")
            self.selects[self._select] = []
        elif tag == "option" and self._select is not None:
            self.selects[self._select].append(attrs.get("value"))

    def handle_endtag(self, tag):
        if tag == "pre" and self._pre is not None:
            self.pre_blocks.append("".join(self._pre))
            self._pre = None
        elif tag == "select":
            self._select = None

    def handle_data(self, data):
        self.text.append(data)
        if self._pre is not None:
            self._pre.append(data)


def utf8(data, limit):
    require(isinstance(data, bytes) and 0 < len(data) <= limit, "Invalid source byte size")
    try:
        text = data.decode("utf-8", errors="strict")
    except UnicodeDecodeError as error:
        raise SourceError("Source is not strict UTF-8") from error
    require("\x00" not in text and "\ufffd" not in text, "Invalid source encoding characters")
    return text


def parse_page(data):
    page = PageParser()
    page.feed(utf8(data, HTML_LIMIT))
    page.close()
    require(page._pre is None, "Unclosed notice markup")
    return page


def check_download_page(data):
    page = parse_page(data)
    versions = re.findall(r"Version\s+([0-9]+(?:\.[0-9]+)+)", "".join(page.text))
    require(versions == [VERSION], "Official download page release changed or is missing")
    require("/pub/download/index.php" in page.form_actions, "Official download form changed")
    require("uthmani" in page.selects.get("quranType", [])
            and "xml" in page.selects.get("outType", []), "Required source options are missing")
    require(all(page.inputs.get(name) == "true" for name in
                ("marks", "sajdah", "tatweel", "agree")), "Required source form fields changed")


def notice_from_page(data):
    blocks = [text for text in parse_page(data).pre_blocks if "Tanzil Quran Text" in text]
    require(len(blocks) == 1 and all(anchor in blocks[0] for anchor in NOTICE_ANCHORS),
            "The full expected Tanzil notice could not be identified")
    # Preserve the complete decoded <pre> contents, including whitespace; no rewording.
    return blocks[0].encode("utf-8")


def check_embedded_notice(text, notice):
    blocks = [m.group(1) for m in re.finditer(r"<!--(.*?)-->", text, re.DOTALL)
              if "Tanzil Quran Text" in m.group(1)]
    require(len(blocks) == 1, "Missing or ambiguous embedded Tanzil notice")
    require(re.search(r"Version\s+1\.1\b", blocks[0]) is not None,
            "Embedded source release does not match the selected release")
    require(all(anchor in blocks[0] for anchor in NOTICE_ANCHORS),
            "Embedded source notice is incomplete")
    # The live XML and page use different copyright years and descriptive prose.
    # Preserve both complete notices; compare their entire usage-terms sequence.
    expected = notice.decode("utf-8").split("TERMS OF USE:", 1)[1]
    supplied = blocks[0].split("TERMS OF USE:", 1)[1]
    lines = supplied.splitlines()
    # The official XML prefixes every comment line with '#', including border lines.
    # Remove that decoration only for comparison; NOTICE.txt retains it byte-for-byte.
    if all(line.lstrip().startswith("#") for line in lines if line.strip()):
        lines = [line.lstrip()[1:].strip() for line in lines if line.strip()]
        supplied = " ".join(line for line in lines if not re.fullmatch(r"={3,}", line))
    require(" ".join(expected.split()) == " ".join(supplied.split()),
            "Embedded terms differ from the captured official notice; review required")
    return blocks[0].encode("utf-8")


def index(value, expected):
    require(isinstance(value, str) and len(value) <= 8 and re.fullmatch(r"[1-9][0-9]*", value)
            is not None and int(value) == expected, "Missing, duplicate or unordered reference")


def whitespace(value):
    return value is None or not value.strip()


def inspect_xml(data, notice):
    text = utf8(data, XML_LIMIT)
    require(not re.search(r"<!\s*(DOCTYPE|ENTITY)\b", text, re.IGNORECASE),
            "DTD and entity declarations are forbidden")
    require(not re.search(r"<\?(?!xml\s)", text, re.IGNORECASE),
            "Processing instructions are forbidden")
    declaration = re.match(r"\ufeff?<\?xml\s+[^?]*\?>", text)
    require(declaration is not None and re.search(r'encoding=[\"\']utf-8[\"\']',
            declaration.group(), re.IGNORECASE) is not None, "Explicit UTF-8 XML is required")
    # XML normalizes literal attribute tabs/newlines. Reject them rather than silently
    # fingerprinting changed text; numeric character references retain their XML meaning.
    for value in re.finditer(r'''\b(?:text|name|bismillah)\s*=\s*(['"])(.*?)\1''', text, re.DOTALL):
        require(not any(c in value.group(2) for c in "\r\n\t"),
                "Source text fields would undergo attribute-whitespace normalization")
    check_embedded_notice(text, notice)
    try:
        root = ET.fromstring(text)
    except ET.ParseError as error:
        raise SourceError("Malformed source XML") from error
    require(root.tag == "quran" and not root.attrib and whitespace(root.text),
            "Unexpected source XML root")
    require(0 < len(root) <= MAX_CHAPTERS, "Empty source XML or chapter limit exceeded")
    digest = hashlib.sha256()
    counts = []
    openings = 0
    record_count = 0

    def field(label, value):
        require(isinstance(value, str) and value.strip() and len(value) <= 64000,
                "Empty or oversized source field")
        for part in (label, value):
            encoded = part.encode("utf-8")
            digest.update(struct.pack("!I", len(encoded)))
            digest.update(encoded)

    for chapter_number, chapter in enumerate(root, 1):
        require(chapter.tag == "sura" and set(chapter.attrib) == {"index", "name"}
                and whitespace(chapter.text) and whitespace(chapter.tail), "Unexpected chapter structure")
        index(chapter.get("index"), chapter_number)
        field(f"chapter:{chapter_number}:name", chapter.get("name"))
        record_count += len(chapter)
        require(len(chapter) > 0 and record_count <= MAX_RECORDS,
                "Empty chapter or source record limit exceeded")
        for ayah_number, ayah in enumerate(chapter, 1):
            require(ayah.tag == "aya" and set(ayah.attrib) in
                    ({"index", "text"}, {"index", "text", "bismillah"})
                    and not len(ayah) and whitespace(ayah.text) and whitespace(ayah.tail),
                    "Unexpected ayah structure")
            index(ayah.get("index"), ayah_number)
            field(f"ayah:{chapter_number}:{ayah_number}:text", ayah.get("text"))
            if "bismillah" in ayah.attrib:
                require(ayah_number == 1, "Chapter-opening field must be attached to its first ayah")
                field(f"ayah:{chapter_number}:{ayah_number}:bismillah", ayah.get("bismillah"))
                openings += 1
        counts.append(len(chapter))
    return {"chapter_count_observed": len(counts), "ayah_count_observed": sum(counts),
            "ayah_counts_by_chapter_observed": counts, "separate_opening_fields_observed": openings,
            "decoded_fields_sha256": digest.hexdigest(),
            "decoded_fields_digest_method": "utf8-length-prefixed-fields-v1"}


def validate_snapshots(snapshots):
    require(set(snapshots) == set(URLS), "Unexpected source snapshot set")
    for name, snapshot in snapshots.items():
        require(snapshot.requested_url == URLS[name], "Requested source URL/options changed")
        official_url(snapshot.final_url)
    check_download_page(snapshots["download.html"].data)
    notice = notice_from_page(snapshots["text-license.html"].data)
    checks = inspect_xml(snapshots[SOURCE_FILE].data, notice)
    embedded = check_embedded_notice(utf8(snapshots[SOURCE_FILE].data, XML_LIMIT), notice)
    return {NOTICE_FILE: embedded, PAGE_NOTICE_FILE: notice}, checks


def download_sources():
    snapshots = {name: fetch(URLS[name], HTML_LIMIT) for name in ("download.html", "text-license.html")}
    # Check release/options/terms before accepting a potentially different text download.
    check_download_page(snapshots["download.html"].data)
    notice_from_page(snapshots["text-license.html"].data)
    snapshots[SOURCE_FILE] = fetch(SOURCE_URL, XML_LIMIT)
    return snapshots


def write_candidate(directory, snapshots, acquired_at):
    require(re.fullmatch(r"\d{4}-\d\d-\d\dT\d\d:\d\d:\d\dZ", acquired_at) is not None,
            "UTC acquisition timestamp required")
    require(set(snapshots) == set(URLS), "Unexpected source snapshot set")
    # mkdir is exclusive: an existing directory/file/symlink is never replaced.
    directory.mkdir(parents=True, exist_ok=False, mode=0o700)
    try:
        for name, snapshot in snapshots.items():
            with (directory / name).open("xb") as output:
                output.write(snapshot.data)
        notices, checks = validate_snapshots(snapshots)
        for name, notice in notices.items():
            with (directory / name).open("xb") as output:
                output.write(notice)
        files = {name: {"bytes": len(data), "sha256": sha256(data)} for name, data in
                 [(name, value.data) for name, value in snapshots.items()] + list(notices.items())}
        manifest = {"schema_version": 1, "source": "Tanzil Project", "source_version": VERSION,
                    "selection": OPTIONS, "acquired_at_utc": acquired_at,
                    "requests": {name: value.receipt() for name, value in snapshots.items()},
                    "files": files, "checks": checks, "gates": GATES}
        data = (json.dumps(manifest, ensure_ascii=False, indent=2, sort_keys=True) + "\n").encode("utf-8")
        # Written last: without this marker the directory is an incomplete candidate.
        with (directory / MANIFEST_FILE).open("xb") as output:
            output.write(data)
        return sha256(data), checks
    except (SourceError, OSError):
        # Preserve acquired bytes for diagnosis; never delete or bless a partial candidate.
        print(f"Incomplete candidate retained at {directory}", file=sys.stderr)
        raise


def read_file(path, limit):
    require(not path.is_symlink() and path.is_file(), "Expected a regular, nonsymlink candidate file")
    with path.open("rb") as source:
        data = source.read(limit + 1)
    require(0 < len(data) <= limit, "Candidate file size is invalid")
    return data


def unique_object(pairs):
    result = {}
    for key, value in pairs:
        require(key not in result, "Duplicate manifest key")
        result[key] = value
    return result


def verify_candidate(directory, expected_manifest_sha256):
    require(re.fullmatch(r"[0-9a-f]{64}", expected_manifest_sha256 or "") is not None,
            "An externally retained lowercase SHA-256 manifest pin is required")
    require(not directory.is_symlink() and directory.is_dir(), "Expected a candidate directory")
    data = read_file(directory / MANIFEST_FILE, JSON_LIMIT)
    require(sha256(data) == expected_manifest_sha256, "Manifest does not match the supplied pin")
    try:
        manifest = json.loads(utf8(data, JSON_LIMIT), object_pairs_hook=unique_object,
                              parse_constant=lambda _: (_ for _ in ()).throw(SourceError("Invalid JSON constant")))
    except (json.JSONDecodeError, RecursionError) as error:
        raise SourceError("Invalid candidate manifest") from error
    require(isinstance(manifest, dict) and set(manifest) ==
            {"schema_version", "source", "source_version", "selection", "acquired_at_utc",
             "requests", "files", "checks", "gates"}, "Unexpected manifest schema")
    require(type(manifest["schema_version"]) is int and manifest["schema_version"] == 1
            and manifest["source"] == "Tanzil Project" and manifest["source_version"] == VERSION
            and manifest["selection"] == OPTIONS and manifest["gates"] == GATES,
            "Unsupported source selection or changed approval gates")
    require(isinstance(manifest["acquired_at_utc"], str) and re.fullmatch(
            r"\d{4}-\d\d-\d\dT\d\d:\d\d:\d\dZ", manifest["acquired_at_utc"]) is not None,
            "Invalid acquisition timestamp")
    require(isinstance(manifest["files"], dict) and set(manifest["files"]) == set(URLS) | {NOTICE_FILE, PAGE_NOTICE_FILE}
            and isinstance(manifest["requests"], dict) and set(manifest["requests"]) == set(URLS),
            "Unexpected candidate file/receipt set")
    snapshots = {}
    stored = {}
    for name in sorted(set(URLS) | {NOTICE_FILE, PAGE_NOTICE_FILE}):
        limit = XML_LIMIT if name == SOURCE_FILE else HTML_LIMIT
        stored[name] = read_file(directory / name, limit)
        info = manifest["files"][name]
        require(isinstance(info, dict) and set(info) == {"bytes", "sha256"}
                and type(info["bytes"]) is int and info["bytes"] == len(stored[name])
                and info["sha256"] == sha256(stored[name]), "Candidate bytes do not match their receipt")
        if name in URLS:
            request = manifest["requests"][name]
            require(isinstance(request, dict) and set(request) ==
                    {"requested_url", "final_url", "content_type", "http_status"}
                    and type(request["http_status"]) is int and request["http_status"] == 200
                    and isinstance(request["content_type"], str), "Invalid HTTP receipt")
            snapshots[name] = Snapshot(stored[name], request["requested_url"],
                                       request["final_url"], request["content_type"])
    notices, checks = validate_snapshots(snapshots)
    require(all(stored[name] == notice for name, notice in notices.items()),
            "Saved notice differs from captured official notice")
    require(json.dumps(manifest["checks"], sort_keys=True) == json.dumps(checks, sort_keys=True),
            "Structural checks differ from their receipt")
    return checks


def candidate_path(timestamp, source_sha256):
    # CLI acquisitions are confined to ignored project-local storage, even with symlinked parents.
    base = CANDIDATE_ROOT.resolve()
    require(base.is_relative_to(PROJECT_ROOT) and base.is_relative_to(PROJECT_ROOT / ".local"),
            "Candidate storage must remain under the project's ignored .local directory")
    return base / (timestamp.replace("-", "").replace(":", "") + "-" + source_sha256[:16])


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    commands = parser.add_subparsers(dest="command", required=True)
    acquire = commands.add_parser("acquire", help="Download only the selected official Tanzil candidate")
    acquire.add_argument("--accept-tanzil-terms", action="store_true",
                         help="Explicitly acknowledge the evaluated verbatim-use terms")
    verify = commands.add_parser("verify", help="Read-only offline check against a separately retained pin")
    verify.add_argument("bundle", type=Path)
    verify.add_argument("--expected-manifest-sha256", required=True)
    args = parser.parse_args(argv)
    try:
        if args.command == "acquire":
            require(args.accept_tanzil_terms, f"Review {LICENSE_PAGE} and explicitly pass --accept-tanzil-terms")
            snapshots = download_sources()
            timestamp = datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ")
            directory = candidate_path(timestamp, sha256(snapshots[SOURCE_FILE].data))
            pin, checks = write_candidate(directory, snapshots, timestamp)
        else:
            directory = args.bundle
            pin = args.expected_manifest_sha256
            checks = verify_candidate(directory, pin)
        print(json.dumps({"bundle": str(directory), "manifest_sha256": pin,
                          "checks": checks, "gates": GATES}, indent=2, sort_keys=True))
        return 0
    except (SourceError, OSError, URLError) as error:
        print(f"Tanzil source operation failed: {error}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
