# Resume LingoMate AI safely

1. Read `AGENTS.md`, `MASTER_PLAN.md`, `PROJECT_STATUS.md`, `TASK_QUEUE.md`, `TEST_RESULTS.md`, `KNOWN_ISSUES.md` and relevant decisions/sources. Inspect `git status --short --branch` and recent commits. Preserve existing code, progress, user changes and remote history.
2. **M1/M2 foundation acceptance passed**: 11 JVM tests, APK build and lint. Do not re-scaffold those features. Device/runtime appearance remains unverified; the app is not a complete course/AI release.
3. **Exact next task: M4-01 remaining assessment work.** A0 v2 now has 11 lessons/55 activities; all requested A0 topic areas are authored. Full run passed 30 core/25 app tests, APK and lint; a focused 12-test repository run additionally passed the new historical v1→v2 pack-upgrade regression (26 distinct app tests total). Add a real listening activity with honest unavailable-voice/fallback behavior and persisted activity mode, with focused model/grade/UI/persistence tests. Keep qualified language review/device audio verification pending; do not mark A0 or M4 complete. Then continue A1 authoring within M4; preserve stable IDs, snapshots and already passing code.
4. Active SDK: `/private/tmp/lingomate-android-sdk`; Gradle cache: `/private/tmp/lingomate-gradle`. Ignored `local.properties` points to that SDK. **User already approved** the exact SDK license hash below and isolated setup on 2026-10-20; do not request the same consent again.
5. Tests, static analysis, builds and local commits are authorized for this project. On 2026-10-20 the user additionally authorized normal pushes of verified code to `origin develop/lingomate`; do not ask again for this same push scope. Main-branch updates, force pushes, device/emulator launches, paid APIs, deployment and destructive actions remain separate. Do not enable a paid provider or fabricate religious content.
6. After meaningful work units update persistent status/queue/test/issues and relevant decisions/sources; commit verified logical changes. Document any WIP and exact next action before interruption. Keep source/static, JVM, APK/lint and device/backend evidence separate.
7. Push authorization exists, but GitHub rejected the first push (HTTP 403, authenticated `aslamm14` lacks write access). The human was asked to grant/accept collaborator access or authenticate an authorized account. Wait for that access correction before retrying; continue local work meanwhile. Never force push or change accounts automatically.

## Commands
If the temporary SDK is still present, do not reinstall it. If it was cleaned, recreate using the already approved hash:
```sh
python3 tools/setup_android_sdk.py --install --accept-license-hash 9002c006f4b8d9a16e715a9fa4df30ddb8abf9d9
```
Set `ANDROID_HOME` to the existing SDK containing Build Tools 36.0.0, or pass `--existing-sdk`. The helper preserves official package metadata, validates the live license hash, stable channel/origin/checksum, and leaves the existing SDK unchanged. A different license hash would need a new human decision.

For current source changes, run only relevant checks and record actual outcomes:
```sh
GRADLE_USER_HOME=/private/tmp/lingomate-gradle ./gradlew --no-daemon :core:learning:test
GRADLE_USER_HOME=/private/tmp/lingomate-gradle ./gradlew --no-daemon :app:testDebugUnitTest :app:assembleDebug :app:lintDebug
```
The first Robolectric runtime download can take several minutes; do not mistake it for an app/test hang or start duplicate Gradle runs. No test has passed unless its result was actually inspected.

Suggested resumption: “Continue LingoMate AI from the saved checkpoint. Read the progress files, reconcile Git state and continue the earliest unfinished task. Tests/builds/local commits authorized; no paid calls, deployment or external push.”
