# LingoMate AI

Native Android language learning: English, Modern Standard Arabic and a separate verified Quranic Arabic track. English interface, Roman Urdu explanations, offline learning and local progress. This is an active development project, not a complete A0–B1 release.

Current development preview includes 11 English A0 lessons and 66 deterministic practice activities, including 11 listening questions, reading, vocabulary and optional installed offline device-TTS examples. Listening keeps transcripts hidden until the learner chooses reading or saves an answer; reading alternatives remain separately recorded after reopening. Answers, resumable practice sessions and progress are stored locally through versioned content/database upgrades.

The latest authorized run passed 37 core/40 app unit tests, debug build and lint. Physical-device audio/appearance and qualified English review remain pending. This is a partial course: English A1–B1, Fusha/Quranic lessons, complete revision and both online AI modes remain unfinished. See the progress files for exact verification evidence and remaining work.

Local implementation draft: a separate A1 pack adds 18 lessons/108 activities with Roman Urdu teaching, reading/listening and guided self-speaking, plus catalog ordering by level. This new source and its regression tests have not been run or built under the current implementation-only scope. The verified APK still contains A0 only; A1 is not yet published as verified or marked complete.

Start with [project status](docs/PROJECT_STATUS.md), [task queue](docs/TASK_QUEUE.md) and [resume instructions](docs/RESUME_INSTRUCTIONS.md). The complete user specification is preserved in [product requirements](docs/PRODUCT_REQUIREMENTS.md).

## Development

- Open this folder in Android Studio. Use JDK 17+, Android SDK Platform 37.0 and Build Tools 36.0.0. The app targets API 37 and supports API 26+. SDK location belongs in ignored `local.properties` or `ANDROID_HOME`.
- Use the committed, checksum-pinned Gradle wrapper. Dependencies are pinned in `gradle/libs.versions.toml`.
- `app` contains Android UI/platform/persistence code; `core/learning` contains Android-independent learning logic.

```sh
./gradlew :core:learning:test
./gradlew :app:testDebugUnitTest :app:assembleDebug :app:lintDebug
```

Actual executed checks and limits are in [test results](docs/TEST_RESULTS.md). A debug build, once successful, is at `app/build/outputs/apk/debug/app-debug.apk`.

No account, embedded API credentials, analytics or paid service is enabled. Do not push, deploy, reset progress or activate paid APIs without authorization. Quranic material requires verified sources and licenses before inclusion.
