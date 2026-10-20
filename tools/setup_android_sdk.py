#!/usr/bin/env python3
"""Install the required stable platform in an isolated SDK; never auto-accept terms.

Inspection only: python3 tools/setup_android_sdk.py
Install with existing acceptance: python3 tools/setup_android_sdk.py --install
New acceptance requires an explicit --accept-license-hash matching the reviewed terms.
"""

import argparse
import hashlib
import os
from pathlib import Path
import shutil
import tempfile
import urllib.parse
import urllib.request
import xml.etree.ElementTree as ET
import zipfile


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--existing-sdk", default=os.environ.get("ANDROID_HOME"))
    parser.add_argument("--sdk", default="/private/tmp/lingomate-android-sdk")
    parser.add_argument("--install", action="store_true")
    parser.add_argument("--accept-license-hash")
    args = parser.parse_args()
    if not args.existing_sdk:
        parser.error("Set ANDROID_HOME or pass --existing-sdk; no SDK path is assumed.")
    existing = Path(args.existing_sdk).resolve()
    sdk = Path(args.sdk).resolve()
    if sdk == existing or sdk.is_relative_to(existing):
        parser.error("The isolated SDK must be outside the existing SDK.")

    base = "https://dl.google.com/android/repository/"
    with urllib.request.urlopen(base + "repository2-3.xml", timeout=30) as response:
        root = ET.fromstring(response.read())
    package = next(item for item in root.findall("{*}remotePackage")
                   if item.attrib["path"] == "platforms;android-37.0")
    if package.find("{*}channelRef").attrib["ref"] != "channel-0":
        raise SystemExit("Required platform is not on the stable channel.")
    license_id = package.find("{*}uses-license").attrib["ref"]
    terms = next(item for item in root.findall("{*}license") if item.attrib["id"] == license_id).text.strip()
    license_hash = hashlib.sha1(terms.encode()).hexdigest()
    accepted = set()
    for license_file in [existing / "licenses" / license_id, sdk / "licenses" / license_id]:
        if license_file.is_file():
            accepted.update(license_file.read_text().splitlines())
    terms_path = Path(tempfile.gettempdir()) / "lingomate-sdk-license.txt"
    terms_path.write_text(terms + "\n")
    print(f"Platform: platforms;android-37.0\nLicense hash: {license_hash}\nTerms: {terms_path}", flush=True)
    if not args.install:
        print(f"Already accepted: {license_hash in accepted}. Inspection only; no installation performed.")
        return
    if license_hash not in accepted and args.accept_license_hash != license_hash:
        raise SystemExit("License not accepted. Obtain human consent after reviewing the terms; then pass the exact --accept-license-hash.")
    if not (existing / "build-tools" / "36.0.0").is_dir():
        raise SystemExit("Existing SDK needs Build Tools 36.0.0; this tool installs only the platform.")

    complete = package.find("./{*}archives/{*}archive/{*}complete")
    archive_url = urllib.parse.urljoin(base, complete.find("{*}url").text)
    if urllib.parse.urlparse(archive_url).hostname != "dl.google.com" or not archive_url.startswith("https://"):
        raise SystemExit("Unexpected SDK download origin.")
    checksum = complete.find("{*}checksum")
    target = sdk / "platforms" / "android-37.0"
    if not target.exists():
        with tempfile.TemporaryDirectory(prefix="lingomate-sdk-") as temporary:
            archive_path = Path(temporary) / "platform.zip"
            print(f"Downloading {archive_url}", flush=True)
            with urllib.request.urlopen(archive_url, timeout=120) as response, archive_path.open("wb") as output:
                shutil.copyfileobj(response, output)
            if hashlib.new(checksum.attrib.get("type", "sha1"), archive_path.read_bytes()).hexdigest() != checksum.text:
                raise SystemExit("SDK checksum mismatch; nothing installed.")
            unpacked = Path(temporary) / "unpacked"
            unpacked.mkdir()
            with zipfile.ZipFile(archive_path) as archive:
                if any(not (unpacked / entry.filename).resolve().is_relative_to(unpacked.resolve()) for entry in archive.infolist()):
                    raise SystemExit("Unsafe SDK archive path.")
                archive.extractall(unpacked)
            platform = next(path.parent for path in unpacked.glob("*/android.jar"))
            target.parent.mkdir(parents=True, exist_ok=True)
            shutil.move(str(platform), target)

    (sdk / "licenses").mkdir(parents=True, exist_ok=True)
    accepted.add(license_hash)
    (sdk / "licenses" / license_id).write_text("\n".join(sorted(accepted - {""})) + "\n")
    for child in ["build-tools", "platform-tools"]:
        destination = sdk / child
        if not destination.exists():
            destination.symlink_to(existing / child, target_is_directory=True)
    for platform in ["android-35", "android-36"]:
        destination = sdk / "platforms" / platform
        if not destination.exists() and (existing / "platforms" / platform).exists():
            destination.symlink_to(existing / "platforms" / platform, target_is_directory=True)
    project = Path(__file__).resolve().parents[1]
    (project / "local.properties").write_text(f"sdk.dir={sdk}\n")
    print(f"Isolated SDK ready: {sdk}. Existing SDK unchanged.", flush=True)


if __name__ == "__main__":
    main()
