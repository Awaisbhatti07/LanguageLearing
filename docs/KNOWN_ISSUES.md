# Known issues and blockers

| ID | State | Issue | Impact / next action |
| --- | --- | --- | --- |
| ENV-01 | Open | Sandbox DNS/network restrictions require approved network execution for Git/dependency downloads | Use scoped network access; preserve exact failures if downloads are unavailable |
| GIT-01 | Resolved | Initial push failed HTTP 403; user switched authentication and the retry succeeded on 2026-10-20 | Source checkpoint d9b6800 and all preceding commits are published on origin/develop/lingomate; remote hash confirmed with git ls-remote. Normal development-branch pushes are authorized; no force push or main-branch change |
| VERIFY-01 | Resolved for M4-01L automated scope | Authorized listening command passed in 55s: 37 core/40 app tests, generated schema, APK and lint | Schema v3 adds only presentation records; v1/v2 migrations and old pack upgrades passed. Physical audio/qualified language review remain CONTENT-01/DEVICE-01; new development scopes need their own test/build authorization |
| CONTENT-01 | Pending | No qualified language review yet | Mark authored learning material pending review; do not claim certified A0–B1 coverage |
| QURAN-01 | Pending M6 | Quranic datasets, redistribution rights and linguistic/religious review not acquired | Do not fabricate or bundle religious material; evaluate sources before import |
| VOICE-01 | Pending M8/M9 | No paid voice credentials/backend configured | Offline learning independent; no fake AI or embedded secret |
| DEVICE-01 | Pending authorization | No device/emulator verification performed | Request scope before launches; compilation is not device evidence |
| TOOLING-01 | Informational | Lint suggests Gradle 9.8.2 | Keep the AGP 9.4 documented Gradle 9.6.0 baseline; this is not a build or lint error |
| BUILD-01 | Resolved | Stable AndroidX requires compile SDK 37; initial SDK 36 rejected 14 AARs | User approved the license; official stable platform/package metadata installed in the isolated SDK. Full foundation run passed 11 tests, APK and lint |
