# Fusha A2/B1 focused Android 36 evidence — 2026-10-26

Current batch: Fusha A2 v1 (20 lessons/122 activities) and B1 v1 (22/136), with six additional saved, ungraded writing tasks. All content remains PENDING_REVIEW.

APK: `app/build/outputs/apk/debug/app-debug.apk`, 14,024,269 bytes; SHA-256 `c80399a98d563535ec8ce6e39e7091e21d29a5d3ffecb2909c402003372e478a`. All eight packaged content assets match source. Fresh 83 core/71 app tests, debug build and lint passed; commands, host-JVM mitigation and interrupted attempts are recorded in `docs/TEST_RESULTS.md`.

Only the project-isolated LingoMate_M4 AVD was used: emulator-5580 through ADB server 5039, Android 36 ARM64, 1080×2400 at 480 dpi (360×800 dp). APK install used `-r`, with synthetic learner records retained and Wi-Fi/mobile data disabled. No clear-data, user AVD, physical-device, voice/SDK download or paid API operation.

Observed and visually inspected original captures:

- [Writing input route](arabic-writing-input.png), [hierarchy](arabic-writing-input.xml): A2 practical-message lesson, activity 1 of 7, English prompt and explicit self-practice/save instructions.
- [Saved original/checklist](saved-paragraph.png), [hierarchy](saved-paragraph.xml): explicitly saved the 84-character synthetic original `My synthetic practice message. Please confirm the room before I send the invitation.` and one criterion. No automatic grade or quiz points are claimed.
- [Arabic model at 1.0×](arabic-model.png), [hierarchy](arabic-model.xml): marked Arabic paragraph with separate English heading and Roman Urdu meaning.
- [Arabic model at 1.5×, complete frame](arabic-model-1_5x-full.png), [hierarchy](arabic-model-1_5x-full.xml): the full Arabic model remains readable and wraps inside the card. [Earlier scrolled frame](arabic-model-1_5x.png), [hierarchy](arabic-model-1_5x.xml) retains the lower portion/meaning before repositioning; it is not evidence of the whole paragraph alone.
- [Saved feedback after process restart](writing-after-restart.png), [hierarchy](writing-after-restart.xml): force-stop/relaunch, course resume and exact synthetic original/checklist retained.

Font scale was restored and read back as 1.0, then only emulator-5580 was stopped. Synthetic progress remains in the temporary project AVD; these original PNG/XML captures persist in Git. Helpers remained temporary and no screenshot was edited.

Limits: ADB injected ASCII text; actual Arabic original-text entry/RTL is covered by the fresh Compose/repository tests, not a device-keyboard check. No Arabic voice availability, pronunciation/waqf, physical audio, API 26/37 runtime or full accessibility audit was performed. All 42 upper-level lesson completions, six-question scoring, reading-only credit, old-progress preservation and six Arabic writing responses across retry/update/database reopen are JVM evidence. Qualified Arabic/Roman Urdu/CEFR review and physical/audible audio remain pending M5 release gates; earlier broader English/A0/A1 runtime checks were not repeated.
