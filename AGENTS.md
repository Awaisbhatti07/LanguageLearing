# LingoMate AI engineering instructions

## Scope and safety
- Native Android only: Kotlin, Jetpack Compose, Material 3. English UI; Roman Urdu teaching explanations; correct Arabic and RTL rendering.
- Read `docs/MASTER_PLAN.md`, `PROJECT_STATUS.md`, `TASK_QUEUE.md`, `TEST_RESULTS.md`, and `RESUME_INSTRUCTIONS.md` before continuing. Preserve existing work and Git history.
- Work milestones in order. Record incomplete acceptance criteria; never label placeholders, unreviewed content, or unexecuted checks complete. On 2026-10-26 the user delegated the M4/M5 sequencing choice: continue Fusha A0 implementation while keeping M4 qualified language review and physical audio as pending release gates. This narrow implementation-order exception does not mark M4 complete or authorize physical-device operations.
- On 2026-10-22 the user approved option 1 (unit tests, debug build/lint and a fresh isolated Android 36 emulator) and said “please don't ask everytime.” This is standing approval for routine project unit tests, debug builds, lint and checks on this project's fresh isolated emulator; do not repeat those permission questions unless the user narrows/revokes the scope. Use existing SDK/system images, isolate emulator data under `/private/tmp`, and target only that instance. Preserve existing AVDs/accounts/snapshots and physical devices; no SDK or voice downloads are included. Earlier results remain historical evidence, not a pass for changed source.
- On 2026-10-26 the user separately approved restoring the isolated Android SDK 37/Gradle environment and downloading missing pinned build dependencies. Use ignored project-local `.local/android-sdk` and `.local/gradle-user-home` so temporary cleanup does not remove them. Reuse the already accepted exact SDK license hash; preserve the global SDK and dependency pins. Do not repeat this restoration approval; other SDK/voice downloads remain outside this exception.
- Paid APIs, deployment, destructive operations, physical-device/user-AVD operations, SDK/voice downloads and pushes outside the separately authorized normal development-branch scope still require specific authorization. Read-only inspection, source editing and local checkpoint commits remain allowed. Record actual verification evidence and limitations; qualified curriculum review cannot be inferred from automation.
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
- Use `develop/lingomate`. Commit completed verified changes; checkpoint incomplete work only with an explicit WIP description and accurate verification state. The user explicitly authorized pushing verified code to the development branch on 2026-10-20. Normal pushes to `origin develop/lingomate` are now allowed; no force push, main-branch update, deployment or paid API use is authorized.
- Do not install SDKs or dependencies outside authorized scope. Use an isolated Gradle user home if the execution sandbox cannot write the default cache.

## Routine verification commands (standing approval recorded above)
```sh
./gradlew :core:learning:test
./gradlew :app:testDebugUnitTest :app:assembleDebug :app:lintDebug
# Only target this project's isolated emulator; never connected user devices:
./gradlew :app:connectedDebugAndroidTest
```
Source inspection, compilation, JVM tests, instrumentation and real-device behavior are separate evidence. Never infer a device pass from an APK build.
