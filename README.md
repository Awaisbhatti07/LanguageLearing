# LingoMate AI

Native Android language learning: English, Modern Standard Arabic and a separate verified Quranic Arabic track. English interface, Roman Urdu explanations, offline learning and local progress. This is an active development project, not a complete A0–B1 release.

Current development preview includes **74 English lessons/450 activities**: A0 has 11 lessons/66 activities, A1 has 18/108, A2 has 21/128 and B1 has 24/148. Each lesson includes reading, vocabulary, self-speaking and a listening question, using optional installed offline device voices. Listening keeps transcripts hidden until the learner chooses reading or saves an answer; reading alternatives stay separately recorded after reopening. Answers, resumable sessions and progress are stored locally through versioned content/database upgrades. The course lists A0 through B1 in order while allowing direct topic access.

The preview also includes **12 Fusha A0 lessons/72 activities** with marked Arabic, Roman Urdu teaching, greetings, reading and six practice types per lesson. Arabic study/input/transcript fields use RTL within the English interface. All material remains pending qualified review; the Fusha course is a foundation draft, not a complete A0–B1 release.

Latest checks passed **67 core + 61 app tests**, debug build and lint. The APK contains five source-identical packs: **86 lessons/522 activities/86 listening questions/six ungraded paragraphs**. Fusha progress remains separate, and adding it preserves English answers, active sessions, reading modes, writing and completed results. English A0 v4 also corrects lowercase-M grading while retaining older session results.

Approved checks on a fresh offline Android 36 emulator have exercised A0 answers/results, persisted feedback and reading alternatives, actual TTS completion, themes and settings. Enlarged-font/paragraph, interruption/fallback, settings persistence and direct B1 access checks also passed within that representative scope; exact observations and remaining limits are in [M4 acceptance](docs/M4_ACCEPTANCE.md). The Review singular-label fix also passed 56 fresh app tests, build/lint and a focused Android 36 check, preserving saved results. Physical audio quality and qualified English/Roman Urdu review remain pending; no completed/certified level is claimed. The Review correction, restored build setup and persistent verification evidence are published through bdbf183 on develop/lingomate; GitHub's exact hash was confirmed on 2026-10-26.

Focused Android 36 Fusha checks also passed for marked RTL text, 1.5× fonts, correct selected-answer feedback and feedback after restart; [evidence and limits](docs/evidence/m5-2026-10-26/README.md). Arabic voice quality/keyboard entry and qualified Fusha review remain unverified.

A2 includes two paragraph self-practice tasks with explicit local saving, self-review checklists and example responses. Writing receives no automatic grammar/quality score and does not change quiz points. A2 covers experiences/past events, future/modals, comparisons/conditionals, phrasal verbs and connected communication/roleplay.

B1 includes four more ungraded writing tasks. Topics cover narrative tenses, conditionals, passive/reported speech, relative/complex clauses, connected speech, stories/opinions and practical workplace/interview/presentation skills. All content awaits qualified review. Connected-speech material explains that device voices may not reproduce natural weak forms; no pronunciation grade is claimed. Full English/Fusha acceptance, later Fusha levels, Quranic lessons, complete revision and both online AI modes remain unfinished. See the progress files for exact evidence and remaining work.

Start with [project status](docs/PROJECT_STATUS.md), [task queue](docs/TASK_QUEUE.md) and [resume instructions](docs/RESUME_INSTRUCTIONS.md). The complete user specification is preserved in [product requirements](docs/PRODUCT_REQUIREMENTS.md).

## Development

- Open this folder in Android Studio. Use JDK 17+, Android SDK Platform 37.0 and Build Tools 36.0.0. The app targets API 37 and supports API 26+. SDK location belongs in ignored `local.properties` or `ANDROID_HOME`.
- Use the committed, checksum-pinned Gradle wrapper. Dependencies are pinned in `gradle/libs.versions.toml`.
- `app` contains Android UI/platform/persistence code; `core/learning` contains Android-independent learning logic.

```sh
./gradlew :core:learning:test
./gradlew :app:testDebugUnitTest :app:assembleDebug :app:lintDebug
```

Routine project unit tests, debug builds, lint and this project's isolated emulator have standing user approval, per `AGENTS.md`; do not repeat those permission prompts. The user also approved restoring the isolated build environment: active SDK `.local/android-sdk` and Gradle user home `.local/gradle-user-home` are ignored and leave the global SDK unchanged. Set `GRADLE_USER_HOME="$PWD/.local/gradle-user-home"` when using the restored cache. Actual executed checks and limits are in [test results](docs/TEST_RESULTS.md). The current debug APK at `app/build/outputs/apk/debug/app-debug.apk` contains English A0–B1, Fusha A0 and six saved paragraph self-practice tasks; it is installed on the isolated Android 36 test emulator; this does not establish physical-device/audio-quality acceptance.

No account, embedded API credentials, analytics or paid service is enabled. Normal verified pushes to `develop/lingomate` are authorized. Deployment, progress resets, paid APIs and main/force pushes require separate approval. Quranic material requires verified sources and licenses before inclusion.
