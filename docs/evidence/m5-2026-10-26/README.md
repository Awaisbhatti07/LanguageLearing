# Fusha A0 focused Android 36 evidence — 2026-10-26

APK: **13,974,280 bytes**, SHA-256 `873a2693b1e688ad51e8311ca652828aaa4f402a4dcf12fc68f42fcfbdb8f8d4`. Five source-identical content packs: 86 lessons/522 activities/86 listening questions/six ungraded paragraphs. Fresh **67 core + 61 app tests** (zero failures/errors/skips), debug build and lint passed in 42s. JVM tests include all twelve Arabic lessons and a complete six-activity Compose flow; these are separate from the limited emulator observations below.

The retained project-only LingoMate_M4 Android 36 Google Play ARM64 instance was targeted as emulator-5580 on ADB 5039, 1080×2400/480 dpi. Installed with `-r`, preserving synthetic English progress; Review still showed the existing 6/6 result. No database edits, data clearing or reseeding.

- [Course](fusha-course.png): new Fusha course and vowel lesson route, pending-review label.
- [Vowel examples](vowel-examples.png): original Arabic short-vowel sequence, marked word and separate Roman Urdu explanation at normal font scale.
- [1.5× font](vowels-1_5x.png): same example with larger text, visible vowel marks and usable scrolling.
- [Feedback](vowel-feedback.png): selecting kasra produces Correct, the original marked answer and Roman Urdu explanation.
- [After restart](feedback-after-restart.png): force-stop/relaunch, reopen the active Fusha lesson, saved feedback remains.

All PNGs are original and visually inspected; corresponding XML files preserve the UI hierarchy. The first helper attempt stopped before navigation because its coordinate guard excluded the bottom tab; the corrected helper passed without app changes. Font scale restored and read back as 1.0, then only this isolated emulator was stopped. Synthetic data retained.

No actual Arabic speech-quality/voice-availability acceptance, Arabic keyboard-entry runtime check, physical device, API 26/37 runtime, complete accessibility audit or all-lesson emulator pass is claimed. Arabic input, reading-only listening credit and all six activity types passed JVM/Compose tests; the device check above exercised study, one selected answer and persisted feedback. Qualified English/Fusha review and physical/audio assessments remain open release gates.
