# LingoMate AI

Native Android language learning: English, Modern Standard Arabic and a separate verified Quranic Arabic track. English interface, Roman Urdu explanations, offline learning and local progress. This is an active development project, not a complete A0–B1 release.

Current development preview includes **50 English lessons/302 activities**: A0 has 11 lessons/66 activities, A1 has 18/108 and A2 has 21/128. Each lesson includes reading, vocabulary, self-speaking and a listening question, using optional installed offline device voices. Listening keeps transcripts hidden until the learner chooses reading or saves an answer; reading alternatives stay separately recorded after reopening. Answers, resumable sessions and progress are stored locally through versioned content/database upgrades. The course lists A0, A1 and A2 in order while allowing direct topic access.

The latest authorized run passed **53 core/51 app unit tests (104 total)**, debug build and lint for A2 and earlier-level regressions. All three packs were inspected in the APK. Physical-device audio/appearance and qualified English review remain pending; no completed/certified level is claimed.

A2 includes two paragraph self-practice tasks with explicit local saving, self-review checklists and example responses. Writing receives no automatic grammar/quality score and does not change quiz points. A2 covers experiences/past events, future/modals, comparisons/conditionals, phrasal verbs and connected communication/roleplay. Content remains pending review. B1, Fusha/Quranic lessons, complete revision and both online AI modes remain unfinished. See the progress files for exact evidence and remaining work.

The local B1 source draft adds **24 lessons/148 activities**, including 24 listening questions and four ungraded writing tasks. Topics include narrative tenses, conditionals, passive/reported speech, relative/complex clauses, connected speech, stories/opinions and practical workplace/interview/presentation skills. **B1 tests/build/lint have not run; the current APK and 104-test report cover only A0–A2.** All B1 content awaits qualified review. Its connected-speech material explains that device voices may not reproduce natural weak forms; no pronunciation grade is claimed.

Start with [project status](docs/PROJECT_STATUS.md), [task queue](docs/TASK_QUEUE.md) and [resume instructions](docs/RESUME_INSTRUCTIONS.md). The complete user specification is preserved in [product requirements](docs/PRODUCT_REQUIREMENTS.md).

## Development

- Open this folder in Android Studio. Use JDK 17+, Android SDK Platform 37.0 and Build Tools 36.0.0. The app targets API 37 and supports API 26+. SDK location belongs in ignored `local.properties` or `ANDROID_HOME`.
- Use the committed, checksum-pinned Gradle wrapper. Dependencies are pinned in `gradle/libs.versions.toml`.
- `app` contains Android UI/platform/persistence code; `core/learning` contains Android-independent learning logic.

```sh
./gradlew :core:learning:test
./gradlew :app:testDebugUnitTest :app:assembleDebug :app:lintDebug
```

Run verification commands only after explicit authorization for the current scope, per `AGENTS.md`. Actual executed checks and limits are in [test results](docs/TEST_RESULTS.md). The current debug APK at `app/build/outputs/apk/debug/app-debug.apk` contains A0/A1/A2 and saved paragraph self-practice; it has not been installed or inspected on a device.

No account, embedded API credentials, analytics or paid service is enabled. Do not push, deploy, reset progress or activate paid APIs without authorization. Quranic material requires verified sources and licenses before inclusion.
