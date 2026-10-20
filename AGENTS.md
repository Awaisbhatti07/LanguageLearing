# LingoMate AI engineering instructions

## Scope and safety
- Native Android only: Kotlin, Jetpack Compose, Material 3. English UI; Roman Urdu teaching explanations; correct Arabic and RTL rendering.
- Read `docs/MASTER_PLAN.md`, `PROJECT_STATUS.md`, `TASK_QUEUE.md`, `TEST_RESULTS.md`, and `RESUME_INSTRUCTIONS.md` before continuing. Preserve existing work and Git history.
- Work milestones in order. Record incomplete acceptance criteria; never label placeholders, unreviewed content, or unexecuted checks complete.
- Default is implementation only. Tests, builds, launches, and verification need explicit user authorization. The initial development request authorizes implementation, tests, static analysis, builds, and local checkpoint commits for this project. Device/emulator launches, paid API use, deployment, destructive operations, and external Git pushes require separate authorization.
- No review agents or subagents unless explicitly requested. No unrelated changes or unnecessary dependencies.
- Never embed API secrets, log credentials/transcripts, use destructive database fallback, reset user progress, or commit local SDK/signing configuration.

## Architecture
- `app`: Android application, feature-oriented packages, lifecycle-aware ViewModels, Room persistence, DataStore settings, platform audio adapters, manual application-scoped dependency injection.
- `core/learning`: Android-independent curriculum models, validation, grading, Unicode search and review scheduling. Keep these deterministic and unit-testable.
- Bundled versioned content lives in `app/src/main/assets/content`. Keep learner records separate from replaceable content. Stable IDs must survive content updates.
- Database schema changes require explicit migrations, exported Room schemas and migration tests. Content imports validate before changing stored content; no delete-and-reseed learner tables.
- Quranic original text, annotations, morphology and frequencies must remain separate. No religious text, references, counts or translations without verified provenance and redistribution rights.
- Online AI is optional; offline learning must never depend on it. No simulated tutor responses marketed as AI.

## Working style
- Inspect relevant files and diffs, keep changes narrow, handle errors and process recreation, honor accessibility/font scaling. Prefer simple maintainable code.
- Each logical work unit updates status, task queue, test results and issues. Update decisions/sources when they change. Record actual commands and results, including environment blockers.
- Use local `develop/lingomate` branch. Commit completed verified changes; checkpoint incomplete work only with an explicit WIP description and accurate verification state. Do not push.
- Do not install SDKs or dependencies outside authorized scope. Use an isolated Gradle user home if the execution sandbox cannot write the default cache.

## Verification commands (when authorized)
```sh
./gradlew :core:learning:test
./gradlew :app:testDebugUnitTest :app:assembleDebug :app:lintDebug
# Only after device/emulator authorization:
./gradlew :app:connectedDebugAndroidTest
```
Source inspection, compilation, JVM tests, instrumentation and real-device behavior are separate evidence. Never infer a device pass from an APK build.
