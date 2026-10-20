# Resume LingoMate AI safely

1. Read `AGENTS.md`, `MASTER_PLAN.md`, `PROJECT_STATUS.md`, `TASK_QUEUE.md`, `TEST_RESULTS.md`, `KNOWN_ISSUES.md` and relevant decisions/sources. Inspect `git status --short --branch` and recent commits. Preserve existing code, progress, user changes and remote history.
2. **M1/M2 foundation acceptance passed**: 11 JVM tests, APK build and lint. Do not re-scaffold those features. Device/runtime appearance remains unverified; the app is not a complete course/AI release.
3. **Exact next task: verify M4-01L listening after explicit authorization.** A local WIP implements pack v3 (11 lessons/66 activities), `listening_choice`/mode-bearing answers, request-specific offline playback completion, hidden transcripts and durable reading alternatives. Room v3/migration 2→3 persists exposure before text is revealed; reopening or replay must not turn reading into listening credit. Regression tests are written but unrun. Inspect the local WIP and Git diff; do not implement this slice again. After authorization, run the relevant core/app unit tests, inspect the generated schema `3.json` and actual 1→3/2→3 migration results; run debug APK/lint only with build/verification authorization. Fix actual failures, record evidence, then commit/push verified changes. Last verified/published source is d9b6800 (pack/database v2; 30 core/26 distinct app tests across full/focused runs). The old APK does not include this new code. Keep language review/device audio pending and M4 incomplete; A1 authoring follows this slice.
4. Active SDK: `/private/tmp/lingomate-android-sdk`; Gradle cache: `/private/tmp/lingomate-gradle`. Ignored `local.properties` points to that SDK. **User already approved** the exact SDK license hash below and isolated setup on 2026-10-20; do not request the same consent again.
5. **Apply the user's latest development modes: implementation only unless this new scope has explicit unit-testing/build authorization.** Earlier verification authorization/results do not automatically extend to this continuation. Do not execute tests, compilers, builds, lint, launches or automated validation while approval is absent. Read/edit/diff and local WIP checkpoint commits are allowed. The user's authorization for normal pushes of verified code to `origin develop/lingomate` persists; do not ask again for that same push scope, but do not publish this unverified WIP. Main updates, force pushes, device/emulator launches, paid APIs, deployment and destructive actions remain separate.
6. After meaningful work units update persistent status/queue/test/issues and relevant decisions/sources; commit verified logical changes. Document any WIP and exact next action before interruption. Keep source/static, JVM, APK/lint and device/backend evidence separate.
7. GitHub publication succeeded after the user switched authentication. Source checkpoint `d9b6800` and all preceding commits are on `origin/develop/lingomate`; `git ls-remote` confirmed the remote hash. The branch now tracks its upstream. The prior HTTP 403 blocker is resolved; do not ask for the same access correction again. Inspect Git status before further work and use normal pushes within the existing authorization; never force push or change accounts automatically. Main remains unchanged.

## Commands
If the temporary SDK is still present, do not reinstall it. If it was cleaned, recreate using the already approved hash:
```sh
python3 tools/setup_android_sdk.py --install --accept-license-hash 9002c006f4b8d9a16e715a9fa4df30ddb8abf9d9
```
Set `ANDROID_HOME` to the existing SDK containing Build Tools 36.0.0, or pass `--existing-sdk`. The helper preserves official package metadata, validates the live license hash, stable channel/origin/checksum, and leaves the existing SDK unchanged. A different license hash would need a new human decision.

Only after explicit authorization for the current listening scope, run relevant checks and record actual outcomes:
```sh
GRADLE_USER_HOME=/private/tmp/lingomate-gradle ./gradlew --no-daemon :core:learning:test
GRADLE_USER_HOME=/private/tmp/lingomate-gradle ./gradlew --no-daemon :app:testDebugUnitTest :app:assembleDebug :app:lintDebug
```
The first Robolectric runtime download can take several minutes; do not mistake it for an app/test hang or start duplicate Gradle runs. No test has passed unless its result was actually inspected.

Suggested resumption: “Continue LingoMate AI from the saved checkpoint. Read the progress files and inspect the local listening WIP. Implement safely; ask before running tests/builds under my latest development modes. Local checkpoints and normal pushes of verified code to origin develop/lingomate remain authorized; no paid calls, deployment, force pushes or main-branch changes.”
