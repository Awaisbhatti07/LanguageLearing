# LingoMate AI

Native Android language learning: English, Modern Standard Arabic and a separate verified Quranic Arabic track. English interface, Roman Urdu explanations, offline learning and local progress. This is an active development project, not a complete A0–B1 release.

Current development preview includes **74 English lessons/450 activities**: A0 has 11 lessons/66 activities, A1 has 18/108, A2 has 21/128 and B1 has 24/148. Each lesson includes reading, vocabulary, self-speaking and a listening question, using optional installed offline device voices. Listening keeps transcripts hidden until the learner chooses reading or saves an answer; reading alternatives stay separately recorded after reopening. Answers, resumable sessions and progress are stored locally through versioned content/database upgrades. The course lists A0 through B1 in order while allowing direct topic access.

The latest lesson-flow checks passed **56 app tests**, debug build and lint; unchanged core coverage remains **60 passing UP-TO-DATE tests**. Every new lesson step starts at the top. Android 36 runtime inspection also found and fixed status-bar contrast when the app theme differs from the device theme. The corrected APK retains all four source-identical packs.

Approved checks on a fresh offline Android 36 emulator have exercised A0 answers/results, persisted feedback and reading alternatives, actual TTS completion, themes and settings. Enlarged-font/paragraph, interruption/fallback, settings persistence and direct B1 access checks also passed within that representative scope; exact observations and remaining limits are in [M4 acceptance](docs/M4_ACCEPTANCE.md). The Review singular-label source correction is prepared locally but awaits fresh verification; the earlier APK/reports do not include it. Physical audio quality and qualified English/Roman Urdu review remain pending; no completed/certified level is claimed. The user restored GitHub authentication on 2026-10-23; all five saved M4 commits through d0c2ed2 are published on develop/lingomate, including the fixes and persistent acceptance evidence.

A2 includes two paragraph self-practice tasks with explicit local saving, self-review checklists and example responses. Writing receives no automatic grammar/quality score and does not change quiz points. A2 covers experiences/past events, future/modals, comparisons/conditionals, phrasal verbs and connected communication/roleplay.

B1 includes four more ungraded writing tasks. Topics cover narrative tenses, conditionals, passive/reported speech, relative/complex clauses, connected speech, stories/opinions and practical workplace/interview/presentation skills. All content awaits qualified review. Connected-speech material explains that device voices may not reproduce natural weak forms; no pronunciation grade is claimed. Full English acceptance, Fusha/Quranic lessons, complete revision and both online AI modes remain unfinished. See the progress files for exact evidence and remaining work.

Start with [project status](docs/PROJECT_STATUS.md), [task queue](docs/TASK_QUEUE.md) and [resume instructions](docs/RESUME_INSTRUCTIONS.md). The complete user specification is preserved in [product requirements](docs/PRODUCT_REQUIREMENTS.md).

## Development

- Open this folder in Android Studio. Use JDK 17+, Android SDK Platform 37.0 and Build Tools 36.0.0. The app targets API 37 and supports API 26+. SDK location belongs in ignored `local.properties` or `ANDROID_HOME`.
- Use the committed, checksum-pinned Gradle wrapper. Dependencies are pinned in `gradle/libs.versions.toml`.
- `app` contains Android UI/platform/persistence code; `core/learning` contains Android-independent learning logic.

```sh
./gradlew :core:learning:test
./gradlew :app:testDebugUnitTest :app:assembleDebug :app:lintDebug
```

Routine project unit tests, debug builds, lint and this project's isolated emulator have standing user approval, per `AGENTS.md`; do not repeat those permission prompts. Actual executed checks and limits are in [test results](docs/TEST_RESULTS.md). The current debug APK at `app/build/outputs/apk/debug/app-debug.apk` contains A0–B1 and six saved paragraph self-practice tasks; it is installed on the isolated Android 36 test emulator; this does not establish physical-device/audio-quality acceptance.

No account, embedded API credentials, analytics or paid service is enabled. Normal verified pushes to `develop/lingomate` are authorized. Deployment, progress resets, paid APIs and main/force pushes require separate approval. Quranic material requires verified sources and licenses before inclusion.
