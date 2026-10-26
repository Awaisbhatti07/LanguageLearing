# LingoMate AI

Native Android language learning: English, Modern Standard Arabic and a separate verified Quranic Arabic track. English interface, Roman Urdu explanations, offline learning and local progress. This is an active development project, not a complete A0–B1 release.

Current development preview includes **74 English lessons/450 activities**: A0 has 11 lessons/66 activities, A1 has 18/108, A2 has 21/128 and B1 has 24/148. Each lesson includes reading, vocabulary, self-speaking and a listening question, using optional installed offline device voices. Listening keeps transcripts hidden until the learner chooses reading or saves an answer; reading alternatives stay separately recorded after reopening. Answers, resumable sessions and progress are stored locally through versioned content/database upgrades. The course lists A0 through B1 in order while allowing direct topic access.

The preview also includes **72 Fusha lessons/438 activities**: A0 has 12 lessons/72 activities, A1 has 18/108, A2 has 20/122 and B1 has 22/136. Marked Arabic, Roman Urdu teaching, grammar, everyday vocabulary, readings and self-speaking are paired with six scored practice types per lesson. A2/B1 add six saved, ungraded Arabic writing tasks with RTL input and model feedback. B1 readings gradually omit vowel marks while marked teaching examples remain available. Arabic study/input/transcript fields use RTL within the English interface. All material remains pending qualified review; the authored A0–B1 Fusha course remains a development draft.

Latest checks passed **83 core + 71 app tests**, debug build and lint. The APK contains eight source-identical packs: **146 lessons/888 activities/146 listening questions/12 ungraded paragraphs**. Fusha progress remains separate, and adding it preserves English answers, active sessions, reading modes, writing and completed results. English A0 v4 also corrects lowercase-M grading while retaining older session results.

Approved checks on a fresh offline Android 36 emulator have exercised A0 answers/results, persisted feedback and reading alternatives, actual TTS completion, themes and settings. Enlarged-font/paragraph, interruption/fallback, settings persistence and direct B1 access checks also passed within that representative scope; exact observations and remaining limits are in [M4 acceptance](docs/M4_ACCEPTANCE.md). The Review singular-label fix also passed 56 fresh app tests, build/lint and a focused Android 36 check, preserving saved results. Physical audio quality and qualified English/Roman Urdu review remain pending; no completed/certified level is claimed. Verified A2/B1 implementation/evidence is saved locally in 03e9641. Its normal development push was rejected with HTTP 403 for `aslamm14`; repository writer access must be restored. All previous checkpoints remain preserved.

Focused Android 36 Fusha checks also passed for marked RTL text, 1.5× fonts, correct selected-answer feedback and feedback after restart; [evidence and limits](docs/evidence/m5-2026-10-26/README.md). Arabic voice quality/keyboard entry and qualified Fusha review remain unverified.

Fusha A1 adds 18 lessons on pronouns, gender/plurals, word classes, sentence patterns, basic verbs, questions/negation/prepositions and everyday conversations. Focused Android 36 checks passed for the A0/A1 boundary, marked text/1.5× fonts, answer feedback and restart, preserving earlier English/A0 data; [A1 evidence and limits](docs/evidence/m5-a1-2026-10-26/README.md).

Fusha A2 covers verb conjugation/forms, attached pronouns, idafah/agreement, commands and roots. B1 adds applied Nahw/Sarf, verb moods/voice, connectors, comprehension, descriptions/opinions and structured messages. New regression checks preserve earlier learning and all six original Arabic writing responses through retries, content updates and database reopen. Focused Android 36 paragraph/model RTL, 1.5× fonts and saved feedback after restart passed; [upper-level evidence and limits](docs/evidence/m5-upper-2026-10-26/README.md).

English A2 includes two paragraph self-practice tasks with explicit local saving, self-review checklists and example responses. Writing receives no automatic grammar/quality score and does not change quiz points. English A2 covers experiences/past events, future/modals, comparisons/conditionals, phrasal verbs and connected communication/roleplay.

English B1 includes four more ungraded writing tasks. Topics cover narrative tenses, conditionals, passive/reported speech, relative/complex clauses, connected speech, stories/opinions and practical workplace/interview/presentation skills. All content awaits qualified review. Connected-speech material explains that device voices may not reproduce natural weak forms; no pronunciation grade is claimed. Full English/Fusha acceptance, Quranic lessons, complete revision and both online AI modes remain unfinished. See the progress files for exact evidence and remaining work.

Start with [project status](docs/PROJECT_STATUS.md), [task queue](docs/TASK_QUEUE.md) and [resume instructions](docs/RESUME_INSTRUCTIONS.md). The complete user specification is preserved in [product requirements](docs/PRODUCT_REQUIREMENTS.md).

## Development

- Open this folder in Android Studio. Use JDK 17+, Android SDK Platform 37.0 and Build Tools 36.0.0. The app targets API 37 and supports API 26+. SDK location belongs in ignored `local.properties` or `ANDROID_HOME`.
- Use the committed, checksum-pinned Gradle wrapper. Dependencies are pinned in `gradle/libs.versions.toml`.
- App unit test classes use fresh JVM workers (`forkEvery = 1`) and, on ARM64 macOS, `-XX:TieredStopAtLevel=1` after repeated Robolectric worker stalls. All tests remain enabled; the ordinary task without diagnostics passed. This host-test mitigation leaves Android runtime unchanged; the underlying framework/JVM cause remains unconfirmed (ENV-02).
- `app` contains Android UI/platform/persistence code; `core/learning` contains Android-independent learning logic.

```sh
./gradlew :core:learning:test
./gradlew :app:testDebugUnitTest :app:assembleDebug :app:lintDebug
```

Routine project unit tests, debug builds, lint and this project's isolated emulator have standing user approval, per `AGENTS.md`; do not repeat those permission prompts. The user also approved restoring the isolated build environment: active SDK `.local/android-sdk` and Gradle user home `.local/gradle-user-home` are ignored and leave the global SDK unchanged. Set `GRADLE_USER_HOME="$PWD/.local/gradle-user-home"` when using the restored cache. Actual executed checks and limits are in [test results](docs/TEST_RESULTS.md). The current debug APK at `app/build/outputs/apk/debug/app-debug.apk` contains English/Fusha A0–B1 and 12 saved paragraph self-practice tasks; it is installed on the isolated Android 36 test emulator; this does not establish physical-device/audio-quality acceptance.

No account, embedded API credentials, analytics or paid service is enabled. Normal verified pushes to `develop/lingomate` are authorized. Deployment, progress resets, paid APIs and main/force pushes require separate approval. Quranic material requires verified sources and licenses before inclusion.
