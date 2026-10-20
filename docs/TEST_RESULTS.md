# Test and verification record

## 2026-10-20 initial inspection
- Workspace initially empty, no Git repository, no existing source or verification artifacts.
- User confirmed fresh initialization and supplied remote. `git ls-remote` and clone succeeded with authorized network access; remote contains README and initial commit `c06f3ab`.
- Java 17, Gradle 9.5.0 and Android SDK platforms 35/36 are present (file/tool-path inspection only).
- Current application checks: **not run yet**. No emulator, device, paid API or deployment used.

Record commands, exact outcomes, counts and remaining limits below after each work unit. Do not treat source inspection as runtime verification.

## Foundation attempt 1 — SDK gate failed; no application test pass
- `git diff --cached --check` passed for planning checkpoint `92f1041`.
- Stable version metadata read directly from Google Maven/Maven Central. Official wrapper JAR checksum verified and distribution SHA-256 pinned.
- `GRADLE_USER_HOME=/private/tmp/lingomate-gradle ./gradlew --version`: **passed**, Gradle 9.6.0 on Azul Java 17.0.18, macOS aarch64.
- `./gradlew --no-daemon :app:testDebugUnitTest :app:assembleDebug :app:lintDebug` using the same isolated cache: **failed at SDK metadata gate** (details below). Added persistence, preference/theme, onboarding and navigation checks. No application test pass claimed.
- Added settings read/write failure-recovery tests. `git diff --check` passed for tracked source/document changes; this is whitespace hygiene only, not compilation.
- First foundation run **failed** after 9m58s at `:app:checkDebugAarMetadata`: 14 current stable AndroidX artifacts require compile SDK 37; local SDK has 35/36. Core Kotlin compiled and Room KSP/schema export ran, but app tests/APK/lint did not complete. Fix: compile SDK 37 with an isolated official SDK platform. Target SDK remains 36; minimum remains 26.
- Official SDK metadata identifies stable `platforms;android-37.0`. The attempted license-gated installer stopped **before download/install** because the exact required license hash was not already accepted. Public terms saved at `/private/tmp/lingomate-sdk-license.txt`; human decision pending.
- Room-generated v1 schema contains `content_packs`, `lessons`, `lesson_progress`; schema file is preserved in `app/schemas/`. This proves processor/schema generation, not database runtime behavior.
- Static parsing passed for 10 Android XML files and the TOML version catalog. Eleven JVM test methods are written; none reached execution. No APK produced; no emulator/device run, paid API, deployment or remote push.
- `tools/setup_android_sdk.py` preserves the isolated setup procedure. Default invocation inspects only. Installation rejects a missing accepted license unless the caller supplies the exact reviewed hash after human consent; existing global SDK is not modified.
- SDK helper passed Python AST syntax parsing only. `git diff --cached --check` passed for the complete staged checkpoint; source saved as `b4968ba`. These checks do not replace the pending application tests/build/lint.

## Foundation attempt 2 — SDK consent received and installation completed
- User explicitly approved the exact SDK license and isolated installation. `python3 tools/setup_android_sdk.py --install --accept-license-hash 9002c006f4b8d9a16e715a9fa4df30ddb8abf9d9` **completed**: official stable `platform-37.0_r02.zip`, archive checksum verified, installed at `/private/tmp/lingomate-android-sdk`; existing global SDK unchanged.
- Ignored `local.properties` now points to the isolated SDK. Acceptance is recorded only in that SDK's own license directory.
- Attempt 2 failed after 10 seconds before compilation: SDK archive lacked installed `package.xml`, so the loader could not recognize `android-37.0`. Fixed the installer to preserve the official remote package's type/revision/license metadata as a local package manifest; platform/source properties and jars were not altered. The repair completed without another archive download.
- Attempt 3 re-runs `GRADLE_USER_HOME=/private/tmp/lingomate-gradle ./gradlew --no-daemon :app:testDebugUnitTest :app:assembleDebug :app:lintDebug`; result pending. No emulator/device or paid API is involved.
- During attempt 3, `:app:checkDebugAarMetadata` and `:app:compileDebugKotlin` **passed** after the SDK metadata repair. This verifies the SDK provisioning fix; the broader run remains pending and no test/APK/lint success is inferred.
