# Review wording verification — 2026-10-26

Source correction: e93dc0e. APK SHA-256 `b8582389c27e01e4c765281a8b4fc83bdcb23a06e236c4a96f9a80bc8f3eea2b`, 13,969,405 bytes. App unit tests/debug build/lint passed; 56 tests, zero failures/errors/skips.

[Original screenshot](review-singular.png) and [UI hierarchy](review-singular.xml) show the exact **1 completed practice run** label and retained **Best practice result: 6/6** after `install -r` and process restart. The data is synthetic from the prior project-only AVD; no database seeding, clear-data, uninstall or user-device operation was used. The screenshot was visually inspected and is unedited.

Environment: existing LingoMate_M4 Android 36 Google Play ARM64 AVD, emulator-5580/ADB 5039, 1080×2400/480 dpi, default font scale. Only this instance was started/stopped; its synthetic data was retained. Plural selection for counts other than one was source-inspected; this focused runtime check exercised the singular case. No full lesson/audio/theme regression was repeated, and no physical/audio-quality or qualified language-review acceptance is implied.
