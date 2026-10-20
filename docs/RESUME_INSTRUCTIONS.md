# Resume LingoMate AI safely

1. Work in this repository; read `AGENTS.md`, `MASTER_PLAN.md`, `PROJECT_STATUS.md`, `TASK_QUEUE.md`, `TEST_RESULTS.md`, `KNOWN_ISSUES.md` and relevant decisions/sources.
2. Inspect `git status --short --branch` and `git log -8 --oneline`. Preserve user changes, remote history and learner data. Do not restart or re-scaffold existing code.
3. Match actual source/check outputs to documented task state. If a milestone lacks acceptance evidence, keep it incomplete. Reconcile WIP first.
4. Continue the earliest incomplete task. Current starting point: **M1-02 IN_PROGRESS**. The user explicitly approved SDK 37.0 license acceptance and isolated installation on 2026-10-20. Do not ask again. Finish installation in `/private/tmp/lingomate-android-sdk`, point ignored `local.properties` there, rerun foundation checks, then finish M1-03 and M2-01. Do not silently downgrade libraries or skip milestone gates.
5. Use the explicit scope authorized in the current session. The initial request authorizes project implementation/testing/builds/local commits; device launches, paid services, deployment, destructive operations and Git pushes are separate.
6. Use `./gradlew` with JDK 17+ and the configured Android SDK. Keep machine paths in ignored `local.properties`; isolated Gradle cache is permitted where needed. Record actual failures, never invent a pass.
7. After every meaningful work unit update status/queue/test record/issues and commit verified logical changes. Before interruption document exact next task, any uncommitted files, commands/results and blockers. A checkpoint commit may be WIP only when explicitly labelled and limitations recorded.

## Authorized SDK setup step
The human approved this exact license hash on 2026-10-20:
```sh
python3 tools/setup_android_sdk.py --install --accept-license-hash 9002c006f4b8d9a16e715a9fa4df30ddb8abf9d9
GRADLE_USER_HOME=/private/tmp/lingomate-gradle ./gradlew --no-daemon :app:testDebugUnitTest :app:assembleDebug :app:lintDebug
```
`ANDROID_HOME` must identify the existing SDK containing Build Tools 36.0.0 (alternatively pass `--existing-sdk`). The installer checks the live license hash, stable channel, official origin and archive checksum, writes only the isolated SDK and ignored project `local.properties`, and fails if the reviewed hash has changed. A different license hash would need a new decision; this exact hash is already authorized.

Suggested resumption: “Continue LingoMate AI from the saved checkpoint. Read the progress files, reconcile Git state, and continue the earliest unfinished task. Tests/builds and local commits authorized; no paid calls, deployment or external push.”
