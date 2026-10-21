# Resume LingoMate AI safely

1. Read `AGENTS.md`, `MASTER_PLAN.md`, `PROJECT_STATUS.md`, `TASK_QUEUE.md`, `TEST_RESULTS.md`, `KNOWN_ISSUES.md` and relevant decisions/sources. Inspect `git status --short --branch` and recent commits. Preserve existing code, progress, user changes and remote history.
2. **M1/M2 foundation acceptance passed**: 11 JVM tests, APK build and lint. Do not re-scaffold those features. Device/runtime appearance remains unverified; the app is not a complete course/AI release.
3. **A1/catalog implementation is verified; do not reauthor it or repeat passing checks without a new reason.** Source ba16b96 and its following verification checkpoint contain the 18-lesson/108-activity A1 pack, explicit level ordering, coverage/key tests, A0-data preservation and direct A1 access/resume. The approved combined command passed in 54s: **43 core/44 app tests, 0 failures/errors/skips, APK and lint**. Inspected APK contains A0 v3 + A1 v1, 29 lessons/174 activities/29 listening questions. A0 assets/IDs and Room v3 schemas are unchanged; no migration/dependency was added. Inspect Git local/upstream state for publication, then continue pending qualified-review/device-audio acceptance and subsequent M4-03 A2 authoring within M4. Keep the review dependencies/M4 acceptance open; JVM/fake audio is not physical-device validation. New development scopes default to implementation only.
4. Active SDK: `/private/tmp/lingomate-android-sdk`; Gradle cache: `/private/tmp/lingomate-gradle`. Ignored `local.properties` points to that SDK. **User already approved** the exact SDK license hash below and isolated setup on 2026-10-20; do not request the same consent again.
5. **Apply the user's latest development modes.** A1/catalog unit tests, debug build and lint for ba16b96 were explicitly approved and completed; new scopes default to implementation only. Do not ask again for the completed scope or rerun its checks without a relevant change/failure/concern. Local checkpoints and normal pushes of verified code to `origin develop/lingomate` remain authorized. Main updates, force pushes, device/emulator launches, paid APIs, deployment and destructive actions remain separate.
6. After meaningful work units update persistent status/queue/test/issues and relevant decisions/sources; commit verified logical changes. Document any WIP and exact next action before interruption. Keep source/static, JVM, APK/lint and device/backend evidence separate.
7. GitHub publication succeeded after the user switched authentication. Verified listening source `f31126f`, generated schema/results checkpoint `80e26e1` and all preceding commits are on `origin/develop/lingomate`; `git ls-remote` confirmed 80e26e1. The branch tracks its upstream. The prior HTTP 403 blocker is resolved; do not ask for the same access correction again. Inspect Git status before further work and use normal pushes within existing authorization; never force push or change accounts automatically. Main remains unchanged.

## Commands
If the temporary SDK is still present, do not reinstall it. If it was cleaned, recreate using the already approved hash:
```sh
python3 tools/setup_android_sdk.py --install --accept-license-hash 9002c006f4b8d9a16e715a9fa4df30ddb8abf9d9
```
Set `ANDROID_HOME` to the existing SDK containing Build Tools 36.0.0, or pass `--existing-sdk`. The helper preserves official package metadata, validates the live license hash, stable channel/origin/checksum, and leaves the existing SDK unchanged. A different license hash would need a new human decision.

The following commands passed for the listening and subsequent A1/catalog scopes, each after explicit authorization. Use them again only for an authorized relevant change/failure/concern; old reports are not a pass for future source:
```sh
GRADLE_USER_HOME=/private/tmp/lingomate-gradle ./gradlew --no-daemon :core:learning:test
GRADLE_USER_HOME=/private/tmp/lingomate-gradle ./gradlew --no-daemon :app:testDebugUnitTest :app:assembleDebug :app:lintDebug
```
The first Robolectric runtime download can take several minutes; do not mistake it for an app/test hang or start duplicate Gradle runs. No test has passed unless its result was actually inspected.

Suggested resumption: “Continue LingoMate AI from the verified A1/catalog checkpoint. Read the progress files and reconcile Git state; preserve the 18-lesson A1 pack, A0 progress and listening behavior. Continue the next incomplete English/M4 work while retaining language-review/device-audio gates. New tasks default to implementation only; ask before their tests/builds. Local checkpoints and normal verified pushes to origin develop/lingomate remain authorized; no paid calls, deployment, force pushes or main-branch changes.”
