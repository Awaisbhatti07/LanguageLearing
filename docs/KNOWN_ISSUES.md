# Known issues and blockers

| ID | State | Issue | Impact / next action |
| --- | --- | --- | --- |
| ENV-01 | Open | Sandbox DNS/network restrictions require approved network execution for Git/dependency downloads | Use scoped network access; preserve exact failures if downloads are unavailable |
| GIT-01 | Resolved | Initial push failed HTTP 403; user switched authentication and authorized development-branch pushes now succeed | Verified listening source f31126f and schema/results checkpoint 80e26e1 are published on origin/develop/lingomate; remote hash confirmed. Normal verified pushes remain authorized; no force push or main-branch change |
| VERIFY-01 | Resolved for M4-01L automated scope | Authorized listening command passed in 55s: 37 core/40 app tests, generated schema, APK and lint | Schema v3 adds only presentation records; v1/v2 migrations and old pack upgrades passed. Physical audio/qualified language review remain CONTENT-01/DEVICE-01; new development scopes need their own test/build authorization |
| VERIFY-02 | Pending new-scope authorization | A1 draft (18 lessons/108 activities) and catalog ordering are implemented, but new/modified tests and app checks are unrun | Keep local WIP; obtain explicit core/app unit-test and debug build/lint authorization, inspect both packs and A0 data-retention/order/resume results. Existing APK/77-test pass cover A0/listening only; no verified A1 publication yet |
| CONTENT-01 | Pending | No qualified language review yet | Mark authored learning material pending review; do not claim certified A0–B1 coverage |
| QURAN-01 | Pending M6 | Quranic datasets, redistribution rights and linguistic/religious review not acquired | Do not fabricate or bundle religious material; evaluate sources before import |
| VOICE-01 | Pending M8/M9 | No paid voice credentials/backend configured | Offline learning independent; no fake AI or embedded secret |
| DEVICE-01 | Pending authorization | No device/emulator verification performed | Request scope before launches; compilation is not device evidence |
| TOOLING-01 | Informational | Lint suggests Gradle 9.8.2 | Keep the AGP 9.4 documented Gradle 9.6.0 baseline; this is not a build or lint error |
| BUILD-01 | Resolved | Stable AndroidX requires compile SDK 37; initial SDK 36 rejected 14 AARs | User approved the license; official stable platform/package metadata installed in the isolated SDK. Full foundation run passed 11 tests, APK and lint |
