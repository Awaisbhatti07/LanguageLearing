# Resume LingoMate AI safely

1. Work in this repository; read `AGENTS.md`, `MASTER_PLAN.md`, `PROJECT_STATUS.md`, `TASK_QUEUE.md`, `TEST_RESULTS.md`, `KNOWN_ISSUES.md` and relevant decisions/sources.
2. Inspect `git status --short --branch` and `git log -8 --oneline`. Preserve user changes, remote history and learner data. Do not restart or re-scaffold existing code.
3. Match actual source/check outputs to documented task state. If a milestone lacks acceptance evidence, keep it incomplete. Reconcile WIP first.
4. Continue the earliest incomplete task. Current starting point: **M1-02** (build configuration), then **M1-03**. Future checkpoints must replace this pointer.
5. Use the explicit scope authorized in the current session. The initial request authorizes project implementation/testing/builds/local commits; device launches, paid services, deployment, destructive operations and Git pushes are separate.
6. Use `./gradlew` with JDK 17+ and the configured Android SDK. Keep machine paths in ignored `local.properties`; isolated Gradle cache is permitted where needed. Record actual failures, never invent a pass.
7. After every meaningful work unit update status/queue/test record/issues and commit verified logical changes. Before interruption document exact next task, any uncommitted files, commands/results and blockers. A checkpoint commit may be WIP only when explicitly labelled and limitations recorded.

Suggested resumption: “Continue LingoMate AI from the saved checkpoint. Read the progress files, reconcile Git state, and continue the earliest unfinished task. Tests/builds and local commits authorized; no paid calls, deployment or external push.”
