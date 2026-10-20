# LingoMate AI master plan

Source requirements: `PRODUCT_REQUIREMENTS.md` (user-provided, preserved verbatim).
Started: 2026-10-20, Asia/Karachi. Remote: https://github.com/Awaisbhatti07/LanguageLearing.git. Normal pushes to `develop/lingomate` are authorized; verified source through `d9b6800` is published on that branch. Main remains unchanged.

| Milestone | Deliverables and acceptance gate |
| --- | --- |
| M1 Foundation | Reproducible Kotlin/Compose build, design system, safe Room foundation, DataStore settings, navigation; compilation and focused persistence/configuration tests. |
| M2 Workflows | Onboarding, Home/Learn/Speak/Review, Settings, correct back stack and truthful unavailable states; navigation/theme/offline checks. |
| M3 Content engine | Versioned validated packs, course/lesson rendering, deterministic exercises, atomic progress persistence, replaceable audio; import/grading/persistence tests. |
| M4 English A0–B1 | Curriculum coverage checklist, substantial lessons, grammar/vocabulary, reading/listening/speaking and assessments; validation and language-review limitations recorded. |
| M5 Fusha A0–B1 | Separate structured curriculum, harakat, reading, Nahw/Sarf, practice and audio; content and RTL validation. |
| M6 Quranic Arabic | Licensed verified sources, reproducible frequency methodology, word/ayah alignment, vocabulary/surah practice and progress; religious integrity tests and review. |
| M7 Practice/progress | Remaining exercise types, SRS, dictionary and grammar library, analytics, local reminders and permissions; deterministic and persistence tests. |
| M8 Push-to-talk | Replaceable Android speech/TTS, permission and failure recovery, secure optional provider integration; no secrets in APK, no fake AI. |
| M9 Live conversation | Actual supported realtime provider, short-lived credentials, controls, interruptions, limits, transcript review; blocked until secure provider setup is available. |
| M10 Reliability | Validated export/import, explicit migrations, recovery, offline/process lifecycle and security checks; rollback and migration tests. |
| M11 UI refinement | Small phones, dark/light, font scaling, accessibility, Arabic typography, errors and navigation; authorized device checks. |
| M12 Delivery | Available tests/lint/build, debug APK, installation/provider setup and honest verification report; open blockers explicitly listed. |

No milestone is complete until its acceptance evidence exists. Later milestone planning may occur early, but implementation must not bypass unfinished predecessors. Safe persistence and deterministic grading are foundational to M3; full revision/reminder UX remains M7.

2026-10-20 checkpoint: M1 and M2 foundation acceptance passed (11 JVM/Robolectric tests, debug APK and lint). Device/runtime appearance remains separate M11/M12 evidence. M3 is now in progress; M4–M12 remain incomplete.

2026-10-20 M3 checkpoint: 29 core and 25 app tests pass, including pack validation/grading, safe imports, explicit v1→v2 migration, concurrency/idempotence and two complete Compose lesson flows. APK/lint pass. Offline device TTS is implemented with honest failure states; physical audio/runtime quality is not verified. M4 A0 expansion is next; the full course remains unfinished.

2026-10-20 M4 work: A0 pack v2 now contains 11 lessons/55 activities and passes topic-tag/structural checks, compatibility flows and the actual v1→v2 update regression. Across full/focused runs, 30 core/26 distinct app tests pass with APK/lint. M4 remains incomplete: qualified language review, dedicated listening assessment/audio verification and A1–B1 content remain outstanding. GitHub publication succeeded after the user switched authentication; remote development-branch source hash `d9b6800` was confirmed.

2026-10-20 listening continuation (M4-01L): source pack v3 appends one listening assessment per A0 lesson (66 total activities). Hidden transcripts, completed-playback gating, offline error/reading alternatives and irreversible per-session reading exposure are implemented, with an additive database v3 migration and regression tests. The user's latest instructions restrict this continuation to implementation; no tests/build/lint or schema generation has run. Preserve as local WIP until explicitly authorized verification succeeds. M4 and its review/device acceptance remain incomplete; last verified source/APK remains d9b6800.
