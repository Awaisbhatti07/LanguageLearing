# Resume LingoMate AI safely

1. Read `AGENTS.md`, `MASTER_PLAN.md`, `PROJECT_STATUS.md`, `TASK_QUEUE.md`, `TEST_RESULTS.md`, `KNOWN_ISSUES.md` and relevant decisions/sources. Inspect `git status --short --branch` and recent commits. Preserve existing code, progress, user changes and remote history.
2. **M1/M2 foundation acceptance passed**: 11 JVM tests, APK build and lint. Do not re-scaffold those features. Device/runtime appearance remains unverified; the app is not a complete course/AI release.
3. **Exact next task: verify the local M4-02 A1/catalog WIP after explicit authorization.** The draft is already authored: separate `english-a1-beginner` v1 pack, 18 lessons/108 activities in four units; Roman Urdu teaching, examples/vocabulary/reading/self-speaking and all six activity types. Do not duplicate it. DAO catalog ordering now keeps A0→A1→A2→B1 before pack/position. New `EnglishA1ContentTest`/`EnglishLevelsTest` and expanded `LessonFlowTest` are written but unrun. Inspect local Git/diffs; get authorization before core/app unit tests or debug build/lint. Then check actual reports, two-pack APK contents and A0 data-retention/order/resume evidence; fix failures and commit/push verified changes. No schema migration/dependency is added. A0 pack v3 and Room v3 schema are unchanged; last verified listening checkpoint f31126f/80e26e1/1e7a622 passed 77 tests/APK/lint, and that existing APK has no A1. Keep qualified language review/device audio and M4 acceptance open.
4. Active SDK: `/private/tmp/lingomate-android-sdk`; Gradle cache: `/private/tmp/lingomate-gradle`. Ignored `local.properties` points to that SDK. **User already approved** the exact SDK license hash below and isolated setup on 2026-10-20; do not request the same consent again.
5. **Apply the user's latest development modes.** The current A1/catalog continuation is implementation only; do not carry the completed listening-scope test/build authorization into this new work. Read/edit/diff and local WIP checkpoints are allowed. Authorization for normal pushes of verified code to `origin develop/lingomate` persists; do not ask again for that same push scope, but keep unverified A1 work local. Main updates, force pushes, device/emulator launches, paid APIs, deployment and destructive actions remain separate.
6. After meaningful work units update persistent status/queue/test/issues and relevant decisions/sources; commit verified logical changes. Document any WIP and exact next action before interruption. Keep source/static, JVM, APK/lint and device/backend evidence separate.
7. GitHub publication succeeded after the user switched authentication. Verified listening source `f31126f`, generated schema/results checkpoint `80e26e1` and all preceding commits are on `origin/develop/lingomate`; `git ls-remote` confirmed 80e26e1. The branch tracks its upstream. The prior HTTP 403 blocker is resolved; do not ask for the same access correction again. Inspect Git status before further work and use normal pushes within existing authorization; never force push or change accounts automatically. Main remains unchanged.

## Commands
If the temporary SDK is still present, do not reinstall it. If it was cleaned, recreate using the already approved hash:
```sh
python3 tools/setup_android_sdk.py --install --accept-license-hash 9002c006f4b8d9a16e715a9fa4df30ddb8abf9d9
```
Set `ANDROID_HOME` to the existing SDK containing Build Tools 36.0.0, or pass `--existing-sdk`. The helper preserves official package metadata, validates the live license hash, stable channel/origin/checksum, and leaves the existing SDK unchanged. A different license hash would need a new human decision.

The following commands passed for the earlier listening scope. The new A1/catalog scope requires explicit authorization before running them; do not treat old reports as a pass for new source:
```sh
GRADLE_USER_HOME=/private/tmp/lingomate-gradle ./gradlew --no-daemon :core:learning:test
GRADLE_USER_HOME=/private/tmp/lingomate-gradle ./gradlew --no-daemon :app:testDebugUnitTest :app:assembleDebug :app:lintDebug
```
The first Robolectric runtime download can take several minutes; do not mistake it for an app/test hang or start duplicate Gradle runs. No test has passed unless its result was actually inspected.

Suggested resumption: “Continue LingoMate AI from the local A1/catalog checkpoint. Read the progress files and reconcile Git state; do not reauthor the 18-lesson draft. Preserve A0 progress and its verified listening behavior. Current mode is implementation only; ask before A1 unit tests/build/lint. Local checkpoints and normal verified pushes to origin develop/lingomate remain authorized; no paid calls, deployment, force pushes or main-branch changes.”
