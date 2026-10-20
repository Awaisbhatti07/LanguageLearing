# Project status

Updated: 2026-10-20 (Asia/Karachi). Branch: `develop/lingomate`. The user has explicitly authorized pushing verified code to this development branch. First push is pending execution.

- **M1 and M2 complete within their recorded foundation/JVM acceptance scope.** Native Compose app, onboarding and four main tabs, working Settings/course information routes, dark/light/system themes, persisted goals/onboarding, Room content/progress foundation and explicit backup exclusions are implemented.
- Latest full foundation run: **11 tests passed**, 0 failures/errors/skips, debug APK and lint passed. Robolectric 4.17/Compose v2 tests simulate API 35; compile/target API 37, minimum 26. Lint retains one informational newer-Gradle suggestion; the documented compatible Gradle 9.6.0 baseline is intentional.
- Verified foundation APK: `app/build/outputs/apk/debug/app-debug.apk` (13,862,258 bytes). This is a foundation preview, not a complete language course or AI teacher.
- SDK 37.0 is installed in `/private/tmp/lingomate-android-sdk`; exact license acceptance and installation were explicitly approved by the user. Existing global SDK/projects unchanged; do not ask for the same approval again.
- Current task: **M3-01 IN_PROGRESS**, content models/parser/validation/grading. Core source is being prepared independently; these new files are **not yet compiled or tested** and are excluded from the verified foundation checkpoint.
- Next: finish core content/grade tests and a meaningful bundled starter pack; then M3-02 Room session/attempt migration, resumable lesson UI, local progress and offline TTS availability handling. Do not skip M3 to course expansion.
- Uncommitted M3 work: `core/learning/src/main/kotlin/com/lingomate/learning/LearningTrack.kt` serialization annotations and `core/learning/src/main/kotlin/com/lingomate/learning/content/`. Inspect Git status before resuming; preserve these files.
- The prior foundation commit attempt was not executed because automatic approval review hit a usage limit. Source and verification records survived. Reconciled those files and the on-disk test/lint reports before retrying the local commit; do not assume the interrupted commit exists.
- Full English A0–B1, Fusha, verified Quranic learning, complete practice/SRS/reminders, both AI modes, backup/import and final device/release checks remain unfinished. No fabricated Quranic content or mock AI is included.
- No emulator/device launch, paid API, deployment or remote push. Manual runtime/accessibility/Arabic device checks remain unverified.

See `TASK_QUEUE.md`, `TEST_RESULTS.md`, `KNOWN_ISSUES.md` and `RESUME_INSTRUCTIONS.md` for exact acceptance, evidence and next steps.
