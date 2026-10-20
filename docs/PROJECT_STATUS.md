# Project status

Updated: 2026-10-20 (Asia/Karachi). Branch: `develop/lingomate`. Pushes of verified code to this branch are authorized, but the first push failed HTTP 403: authenticated account `aslamm14` lacks write access to `Awaisbhatti07/LanguageLearing`. Human access correction is pending; no remote source update has occurred.

- **M1 and M2 complete within their recorded foundation/JVM acceptance scope.** Native Compose app, onboarding and four main tabs, working Settings/course information routes, dark/light/system themes, persisted goals/onboarding, Room content/progress foundation and explicit backup exclusions are implemented.
- Latest verified foundation source commit: **304f1d9**. Its full run passed **11 tests**, 0 failures/errors/skips, debug APK and lint. Robolectric 4.17/Compose v2 tests simulate API 35; compile/target API 37, minimum 26. Lint retains one informational newer-Gradle suggestion; the compatible Gradle 9.6.0 baseline is intentional.
- Verified foundation APK: `app/build/outputs/apk/debug/app-debug.apk` (13,862,258 bytes). This is a foundation preview, not a complete language course or AI teacher.
- SDK 37.0 is installed in `/private/tmp/lingomate-android-sdk`; exact license acceptance and installation were explicitly approved by the user. Existing global SDK/projects unchanged; do not ask for the same approval again.
- **M3-01 completed**: content models/parser/validation, five deterministic graders and original three-lesson/15-activity English starter pack. Core compile and all 29 tests passed; language review remains pending.
- Current task: **M3-02 IN_PROGRESS**. Next implement atomic imports, explicit Room v1→v2 session/attempt migration, resumable snapshot-based lesson UI, idempotent completion, real progress and offline TTS handling; then focused persistence/migration/UI tests and build/lint. Do not skip M3 to course expansion.
- The prior foundation commit attempt was not executed because automatic approval review hit a usage limit. Source and verification records survived. Reconciled those files and the on-disk test/lint reports before retrying the local commit; do not assume the interrupted commit exists.
- Full English A0–B1, Fusha, verified Quranic learning, complete practice/SRS/reminders, both AI modes, backup/import and final device/release checks remain unfinished. No fabricated Quranic content or mock AI is included.
- No emulator/device launch, paid API, deployment or remote push. Manual runtime/accessibility/Arabic device checks remain unverified.

See `TASK_QUEUE.md`, `TEST_RESULTS.md`, `KNOWN_ISSUES.md` and `RESUME_INSTRUCTIONS.md` for exact acceptance, evidence and next steps.
