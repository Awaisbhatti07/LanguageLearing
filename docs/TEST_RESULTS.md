# Test and verification record

## 2026-10-20 initial inspection
- Workspace initially empty, no Git repository, no existing source or verification artifacts.
- User confirmed fresh initialization and supplied remote. `git ls-remote` and clone succeeded with authorized network access; remote contains README and initial commit `c06f3ab`.
- Java 17, Gradle 9.5.0 and Android SDK platforms 35/36 are present (file/tool-path inspection only).
- Current application checks: **not run yet**. No emulator, device, paid API or deployment used.

Record commands, exact outcomes, counts and remaining limits below after each work unit. Do not treat source inspection as runtime verification.

## Foundation attempt 1 — SDK gate failed; no application test pass
- `git diff --cached --check` passed for planning checkpoint `92f1041`.
- Stable version metadata read directly from Google Maven/Maven Central. Official wrapper JAR checksum verified and distribution SHA-256 pinned.
- `GRADLE_USER_HOME=/private/tmp/lingomate-gradle ./gradlew --version`: **passed**, Gradle 9.6.0 on Azul Java 17.0.18, macOS aarch64.
- `./gradlew --no-daemon :app:testDebugUnitTest :app:assembleDebug :app:lintDebug` using the same isolated cache: **failed at SDK metadata gate** (details below). Added persistence, preference/theme, onboarding and navigation checks. No application test pass claimed.
- Added settings read/write failure-recovery tests. `git diff --check` passed for tracked source/document changes; this is whitespace hygiene only, not compilation.
- First foundation run **failed** after 9m58s at `:app:checkDebugAarMetadata`: 14 current stable AndroidX artifacts require compile SDK 37; local SDK has 35/36. Core Kotlin compiled and Room KSP/schema export ran, but app tests/APK/lint did not complete. Fix: compile SDK 37 with an isolated official SDK platform. Target SDK remains 36; minimum remains 26.
- Official SDK metadata identifies stable `platforms;android-37.0`. The attempted license-gated installer stopped **before download/install** because the exact required license hash was not already accepted. Public terms saved at `/private/tmp/lingomate-sdk-license.txt`; human decision pending.
- Room-generated v1 schema contains `content_packs`, `lessons`, `lesson_progress`; schema file is preserved in `app/schemas/`. This proves processor/schema generation, not database runtime behavior.
- Static parsing passed for 10 Android XML files and the TOML version catalog. Eleven JVM test methods are written; none reached execution. No APK produced; no emulator/device run, paid API, deployment or remote push.
- `tools/setup_android_sdk.py` preserves the isolated setup procedure. Default invocation inspects only. Installation rejects a missing accepted license unless the caller supplies the exact reviewed hash after human consent; existing global SDK is not modified.
- SDK helper passed Python AST syntax parsing only. `git diff --cached --check` passed for the complete staged checkpoint; source saved as `b4968ba`. These checks do not replace the pending application tests/build/lint.

## Foundation attempt 2 — SDK consent received and installation completed
- User explicitly approved the exact SDK license and isolated installation. `python3 tools/setup_android_sdk.py --install --accept-license-hash 9002c006f4b8d9a16e715a9fa4df30ddb8abf9d9` **completed**: official stable `platform-37.0_r02.zip`, archive checksum verified, installed at `/private/tmp/lingomate-android-sdk`; existing global SDK unchanged.
- Ignored `local.properties` now points to the isolated SDK. Acceptance is recorded only in that SDK's own license directory.
- Attempt 2 failed after 10 seconds before compilation: SDK archive lacked installed `package.xml`, so the loader could not recognize `android-37.0`. Fixed the installer to preserve the official remote package's type/revision/license metadata as a local package manifest; platform/source properties and jars were not altered. The repair completed without another archive download.
- Attempt 3 ran `GRADLE_USER_HOME=/private/tmp/lingomate-gradle ./gradlew --no-daemon :app:testDebugUnitTest :app:assembleDebug :app:lintDebug` successfully. No emulator/device or paid API was involved.
- `:app:checkDebugAarMetadata` and `:app:compileDebugKotlin` passed after the SDK metadata repair. A focused test-worker stack inspection during the initial wait confirmed Robolectric runtime downloading through Maven, rather than a blocked application test.
- Attempt 3 **BUILD SUCCESSFUL in 10m53s**: all **11 tests passed**, 0 failures/errors/skips (AppViewModel 2, Room persistence 2, Compose navigation/onboarding 3, preferences/theme 4). `:app:assembleDebug` and `:app:lintDebug` passed; APK 13,386,082 bytes. These are JVM/Robolectric API 35 checks, not emulator/physical-device evidence.
- Lint reported no errors and four findings: older target API, newer compatible-tooling candidates (Gradle 9.8.2/Robolectric 4.17), missing explicit data-extraction rules. Privacy rules, target API 37, current Robolectric and Compose v2 test rule are now being refined; a new focused full foundation run is required before marking milestone gates complete.

## Foundation attempt 4 — privacy/target/test-framework refinement
- Added explicit backup exclusions for cloud and device transfer, plus legacy backup rules. Raised the fresh app's target to API 37 (minimum remains 26), selected verified stable Robolectric 4.17, and replaced the deprecated Compose test rule import with v2.
- Same foundation command is running. Result pending. Previous 11-test/APK/lint pass applies to source checkpoint `b4c13de`, not automatically to these new changes.
- Attempt 4 **BUILD SUCCESSFUL in 4m21s**: 11 tests passed, 0 failures/errors/skips; debug APK 13,862,258 bytes; lint passed with only `AndroidGradlePluginVersion` (Gradle 9.8.2 available). Backup-rule/target/Robolectric/deprecated-rule findings are resolved. Gradle 9.6.0 remains the documented compatible pin. Tests simulate API 35; no emulator/device or real API 37 execution is claimed.
- New M3 core model/parser/grader source was prepared while the final check ran but was not part of the compiled foundation dependency snapshot. It is excluded from the foundation Git checkpoint and requires its own compile/unit validation before acceptance.

## M3-01 content engine
- `GRADLE_USER_HOME=/private/tmp/lingomate-gradle ./gradlew --no-daemon :core:learning:test`: **BUILD SUCCESSFUL in 17s**, 29 tests passed, 0 failures/errors/skips. Tested the real bundled three-lesson/15-activity pack, strict schema/IDs/provenance/Unicode/depth/size guards, unsourced Quran rejection, all five graders, invalid answer shapes, Arabic harakat preservation, repeated ordering tokens and feedback. Structural validation is not qualified language review.
- Created verified foundation commit `304f1d9`. Authorized `git push -u origin develop/lingomate` failed HTTP 403: `aslamm14` lacks write permission to `Awaisbhatti07/LanguageLearing`. No code was pushed; user access correction is pending. Local development continues.

## M3-02 persistence and UI work in progress
- First focused command: `:app:testDebugUnitTest --tests com.lingomate.ai.LearningRepositoryTest --tests com.lingomate.ai.DatabaseMigrationTest`: 12 tests executed; **11 repository tests passed, 1 migration fixture test failed** (`JSONObject` optional `indices` key missing in exported v1 schema). Corrected fixture reader to handle absent empty indices; production migration unchanged.
- Full `:app:testDebugUnitTest :app:assembleDebug :app:lintDebug` running against the connected content/session/lesson flow. Two additional Compose end-to-end tests now exercise all five real activity inputs, persisted feedback/resume and completed progress; these require execution after compilation.
- Full app command **BUILD SUCCESSFUL in 45s** after correcting the fixture reader: compilation, existing/repository/migration tests, APK and lint passed. Lint has only the informational newer-Gradle suggestion.
- With the two additional UI tests compiled, `:app:testDebugUnitTest` **BUILD SUCCESSFUL in 19s**, **25 tests passed**, 0 failures/errors/skips. Both Compose lesson flows passed: all five activity types→saved feedback→results/progress, and leaving→resuming the persisted wrong answer/feedback. Core remains 29 passing tests. No emulator/physical device/audio or backend claim is made.

## M4-01 A0 expansion in progress
- Authored pack v2 with 11 lessons/55 activities; preserved v1's existing IDs. Added a required-A0-skill-tag coverage check and retained exact source/structural/grade tests. Adjusted importer tests to the expanded count/version and UI tests to explicitly open the greetings lesson after alphabet-first ordering.
- Running `:core:learning:test :app:testDebugUnitTest :app:assembleDebug :app:lintDebug` with the isolated cache/SDK. Results pending; authorship is not qualified language review.
- Expanded-pack command **BUILD SUCCESSFUL in 30s**: **30 core tests and 25 app tests passed**, 0 failures/errors/skips; APK and lint passed (one informational Gradle-version suggestion). Inspected the APK ZIP directly: content pack version **2**, **11 lessons**, title `English A0 foundations` is actually bundled; this is artifact inspection, not device installation.
- Focused regression fixture contains the exact historical v1 starter pack from commit `76b24a1`. The upgrade test installs v1, saves an active answer and completed assessment, imports current v2, then verifies snapshots/answers/progress survive with all 11 lessons available. Repository-only command **BUILD SUCCESSFUL in 17s**, **12 repository tests passed**, including the new actual v1→v2 regression. Across the full and focused runs, all **30 core and 26 distinct app tests** passed; the final full app run had 25 tests before this one additional regression was added.

## GitHub publication — authentication corrected
- The user reported switching authentication and explicitly requested retrying the already authorized push. `git push -u origin develop/lingomate` **succeeded**, created the remote development branch and configured its upstream.
- `git ls-remote --heads origin refs/heads/develop/lingomate` returned **d9b6800e988cc3951efd02ebe86a0c6237fcf0b5**, matching the local verified source checkpoint. Main was not changed; no force push was used. The previous HTTP 403 access blocker is resolved.
- Refreshed status, queue, master plan, known issues and resume instructions. No source changes, new tests, builds or runtime checks were performed for this documentation/publication follow-up; prior application evidence remains as recorded above.
