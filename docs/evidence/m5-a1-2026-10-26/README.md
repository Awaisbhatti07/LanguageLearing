# Fusha A1 focused Android 36 evidence — 2026-10-26

APK **13,974,363 bytes**, SHA-256 `da9c11578e668e963a55265b26c766b1743b59b3deeac4ceb7c3678a3d180dad`. All six packaged content assets match source bytes: 104 lessons/630 activities/104 listening questions/six ungraded paragraphs. Fusha A1 v1 adds 18 lessons/108 activities; the five earlier packs are unchanged.

Current-source automated evidence: **74 core + 65 app = 139 passing tests**, zero failures/errors/skips, across the recorded runs. App debug build/lint passed with the full suite in 1m34s using one fresh JVM per test class. A final normal app-task rerun without temporary diagnostic scripts passed in 1m16s, again 65 tests/zero failures/errors/skips. Earlier shared-worker stalls were deliberately terminated and are recorded as failed/interrupted attempts in TEST_RESULTS.md/ENV-02; class isolation mitigated the observed stalls, while the underlying JVM/framework cause is unknown. No tests are disabled and no production Kotlin/JDK/dependency pins changed.

The retained project-only LingoMate_M4 Android 36 Google Play ARM64 instance was targeted as emulator-5580 on ADB 5039, 1080×2400/480 dpi. APK installed with `-r`; no reset, database seeding, uninstall or user-device operation. Existing English Review 6/6 and original Fusha A0 vowel-answer feedback were confirmed after installation.

- [Course boundary](a1-course.png): Fusha A0 precedes A1, with pending language-review labels.
- [Pronoun examples](pronoun-examples.png): marked original Arabic with separate Roman Urdu meaning, at normal font scale.
- [1.5× fonts](pronouns-1_5x.png): marked text, wrapping and scrolling remain usable.
- [Answer feedback](pronoun-feedback.png): feminine-listener pronoun selected, correct result and original marked answer.
- [After process restart](feedback-after-restart.png): saved A1 answer feedback restored after force-stop/relaunch/reopen.

Original PNGs were visually inspected; corresponding XMLs preserve UI text/bounds. Font scale restored/read back as 1.0, then only this emulator was stopped; synthetic English/A0/A1 data remains.

The device scope is representative study/one selected answer/resume. All eighteen A1 lesson completions, exact previous learning preservation, typed person-ending rejection and reading-only listening credit are JVM/Compose/repository evidence. No actual Arabic keyboard-entry, voice availability/pronunciation/waqf/volume-quality acceptance, physical device, API 26/37 runtime, all-lesson emulator or full accessibility pass is claimed. Qualified English/Fusha review and physical audio remain open release gates.
