# Resume LingoMate AI safely

1. Read `AGENTS.md`, `MASTER_PLAN.md`, `PROJECT_STATUS.md`, `TASK_QUEUE.md`, `TEST_RESULTS.md`, `KNOWN_ISSUES.md` and relevant decisions/sources. Inspect `git status --short --branch` and recent commits. Preserve existing code, progress, user changes and remote history.
2. **M1/M2 foundation acceptance passed**: 11 JVM tests, APK build and lint. Do not re-scaffold those features. Device/runtime appearance remains unverified; the app is not a complete course/AI release.
3. **Exact next task: M3-01.** Reconcile the new core content/parser/grader files and LearningTrack serialization annotations left in the working tree. They were not compiled/tested by the foundation run. Write meaningful core tests, create/validate the starter English pack, then proceed to M3-02 for safe content/session/attempt persistence and lesson UI. Respect the ordered task queue.
4. Active SDK: `/private/tmp/lingomate-android-sdk`; Gradle cache: `/private/tmp/lingomate-gradle`. Ignored `local.properties` points to that SDK. **User already approved** the exact SDK license hash below and isolated setup on 2026-10-20; do not request the same consent again.
5. Tests, static analysis, builds and local commits are authorized for this project. On 2026-10-20 the user additionally authorized normal pushes of verified code to `origin develop/lingomate`; do not ask again for this same push scope. Main-branch updates, force pushes, device/emulator launches, paid APIs, deployment and destructive actions remain separate. Do not enable a paid provider or fabricate religious content.
6. After meaningful work units update persistent status/queue/test/issues and relevant decisions/sources; commit verified logical changes. Document any WIP and exact next action before interruption. Keep source/static, JVM, APK/lint and device/backend evidence separate.

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
