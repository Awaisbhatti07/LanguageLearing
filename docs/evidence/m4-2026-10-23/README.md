# M4 representative Android runtime evidence

Recorded 2026-10-23 on a fresh isolated Android 36 Google Play ARM64 revision 7 emulator: 1080×2400, 480 dpi (360×800 dp). See `environment.json`. All learner text is synthetic. No user AVD/account/physical device was operated on. Original PNGs are unedited.

APK SHA-256: `001e1054ee323339766c9af546fc44c01e371e2aabedcbf7c33c01ea28dde308` (13,969,405 bytes). Source fixes are in 4672698, 488446d and 52d6f68. Relevant app tests/debug build/lint passed before interruption; this resumption reused that exact artifact without rebuilding. Core coverage is unchanged UP-TO-DATE evidence, not a fresh test run.

| Evidence | Observation |
| --- | --- |
| [Paragraph keyboard](paragraph-keyboard.png) | Exact paced 110-character input and on-screen keyboard |
| [Paragraph saved](paragraph-saved.png) | Save from the bottom returns to ungraded feedback at the top |
| [TTS completed](tts-completed.png) | Real installed offline engine completed playback and unlocked choices |
| [Reading after replay](reading-after-replay.png) | Replay retains reading mode |
| [Voice unavailable](tts-unavailable.png) | Disabling the speech service only on the isolated AVD produces a visible error; service was re-enabled afterward |
| [A2 result](a2-results.png) | Seven activities, six scored questions, paragraph remains ungraded |
| [Reading totals](reading-totals.png) | Listening 0, reading alternative 1 |
| [B1 direct access](b1-direct-topic.png) | Reported-question study opened before earlier levels were completed |
| [System dark](system-dark.png) / [System light](system-light.png) | App and system-bar contrast follow the device when System is selected |
| [Home at 1.5×](home-font150.png) | Correct singular goal/progress and readable bottom navigation |
| [Later writing at 1.5×](later-paragraph-font150.png) / [UI hierarchy](later-paragraph-font150.xml) | After process restart/new run, the original 110-character paragraph is present in the study review; scroll to read the full long card |
| [First activity at 1.5×](writing-step-font150.png) | Starting from the long study bottom returns to the first activity heading |

Stop/background gating and other exact observations are recorded in `../../TEST_RESULTS.md` and `../../M4_ACCEPTANCE.md`. Screenshots establish only the displayed representative UI. They do not establish audible quality, every lesson/device configuration, API 26/37 runtime behavior or qualified English/Roman Urdu review. Earlier 2026-10-22 observations remain historical; their temporary screenshot files were cleared between sessions.
