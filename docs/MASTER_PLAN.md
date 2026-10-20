# LingoMate AI master plan

Source requirements: `PRODUCT_REQUIREMENTS.md` (user-provided, preserved verbatim).
Started: 2026-10-20, Asia/Karachi. Remote: https://github.com/Awaisbhatti07/LanguageLearing.git. All checkpoints remain local until a push is authorized.

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
