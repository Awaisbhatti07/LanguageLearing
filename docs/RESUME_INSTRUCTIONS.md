# Resume LingoMate AI safely

1. Read `AGENTS.md`, `MASTER_PLAN.md`, `PROJECT_STATUS.md`, `TASK_QUEUE.md`, `TEST_RESULTS.md`, `KNOWN_ISSUES.md` and relevant decisions/sources. Inspect `git status --short --branch` and recent commits. Preserve existing code, progress, user changes and remote history.
2. **M1/M2 foundation acceptance passed**: 11 JVM tests, APK build and lint. Do not re-scaffold those features. Device/runtime appearance remains unverified; the app is not a complete course/AI release.
3. **M4-01L is verified; do not implement or test it again without a new reason.** Source f31126f and its following verification checkpoint contain pack v3 (11 lessons/66 activities), typed listening answers, request-specific playback completion, hidden transcripts and durable reading alternatives. The authorized combined command passed in 55s: 37 core/40 app tests, APK and lint. Generated Room schema 3.json adds only the presentation table; historical v1/v2 migrations, answers/progress and actual pack upgrades pass. Inspect Git local/upstream state to confirm publication; normal verified pushes remain authorized. Next work is M4-01's qualified content/device-audio acceptance and subsequent A1 authoring within M4, preserving open review gates. The complete course remains unfinished; fake-player/JVM checks do not prove physical audio quality.
4. Active SDK: `/private/tmp/lingomate-android-sdk`; Gradle cache: `/private/tmp/lingomate-gradle`. Ignored `local.properties` points to that SDK. **User already approved** the exact SDK license hash below and isolated setup on 2026-10-20; do not request the same consent again.
5. **Apply the user's latest development modes.** Current listening-scope unit tests, schema generation, debug build and lint are explicitly authorized, including fixes/reruns necessary for this change; unrelated future scopes still default to implementation only. Read/edit/diff and local checkpoints are allowed. Authorization for normal pushes of verified code to `origin develop/lingomate` persists; do not ask again for that same push scope. Main updates, force pushes, device/emulator launches, paid APIs, deployment and destructive actions remain separate.
6. After meaningful work units update persistent status/queue/test/issues and relevant decisions/sources; commit verified logical changes. Document any WIP and exact next action before interruption. Keep source/static, JVM, APK/lint and device/backend evidence separate.
7. GitHub publication succeeded after the user switched authentication. Source checkpoint `d9b6800` and all preceding commits are on `origin/develop/lingomate`; `git ls-remote` confirmed the remote hash. The branch now tracks its upstream. The prior HTTP 403 blocker is resolved; do not ask for the same access correction again. Inspect Git status before further work and use normal pushes within the existing authorization; never force push or change accounts automatically. Main remains unchanged.

## Commands
If the temporary SDK is still present, do not reinstall it. If it was cleaned, recreate using the already approved hash:
```sh
python3 tools/setup_android_sdk.py --install --accept-license-hash 9002c006f4b8d9a16e715a9fa4df30ddb8abf9d9
```
Set `ANDROID_HOME` to the existing SDK containing Build Tools 36.0.0, or pass `--existing-sdk`. The helper preserves official package metadata, validates the live license hash, stable channel/origin/checksum, and leaves the existing SDK unchanged. A different license hash would need a new human decision.

The following listening-scope checks were explicitly authorized and have passed; use them again only for a relevant new change/failure/concern. New scopes require their own authorization:
```sh
GRADLE_USER_HOME=/private/tmp/lingomate-gradle ./gradlew --no-daemon :core:learning:test
GRADLE_USER_HOME=/private/tmp/lingomate-gradle ./gradlew --no-daemon :app:testDebugUnitTest :app:assembleDebug :app:lintDebug
```
The first Robolectric runtime download can take several minutes; do not mistake it for an app/test hang or start duplicate Gradle runs. No test has passed unless its result was actually inspected.

Suggested resumption: “Continue LingoMate AI from the saved verified listening checkpoint. Read the progress files and reconcile Git state; preserve the passing code and learner data. Continue the next incomplete M4 work. New tasks default to implementation only; ask before their tests/builds. Local checkpoints and normal verified pushes to origin develop/lingomate remain authorized; no paid calls, deployment, force pushes or main-branch changes.”
