# Task queue

Status values: TODO / IN_PROGRESS / BLOCKED / DONE. DONE requires recorded acceptance evidence. See milestone gates in `MASTER_PLAN.md`.

| ID | Milestone | Status | Task | Dependencies | Acceptance / remaining | Files | Test evidence / commit | Next action |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| M1-01 | M1 | DONE | Repository, instructions, requirements and checkpoint system | None | Remote history preserved; required planning files and verbatim product specification captured | AGENTS.md, docs/*, .gitignore | Planning checkpoint 92f1041; initial remote c06f3ab | Continue M1-02 |
| M1-02 | M1 | BLOCKED | Compatible Gradle/Kotlin/Compose project | M1-01; SDK license approval | Stable pins/wrapper installed; compile SDK corrected to 37; isolated stable 37.0 platform needs a license whose hash is not locally accepted | Gradle files, app/, core/learning/, tools/ | WIP checkpoint b4968ba: wrapper/core Kotlin/Room KSP passed; first build failed 14 AAR checks; app tests/lint not reached | Await pending license decision; install isolated SDK and rerun |
| M1-03 | M1 | IN_PROGRESS | Safe Room and DataStore foundation, design system | M1-02 acceptance pending | Content/progress separation, non-destructive Room builder, persistent dark/light/onboarding/goals implemented; schema v1 exported; tests pending | app/core/, app/schemas/, app/src/test/ | WIP b4968ba; persistence and failure recovery tests written, not executed | Resolve M1-02 and run tests |
| M2-01 | M2 | IN_PROGRESS | Onboarding and functional main navigation | M1 acceptance pending | Home/Learn/Speak/Review/Settings and course information routes implemented; all acceptance checks pending | app/feature/, app/core/navigation/ | WIP b4968ba; navigation/onboarding tests written, not executed | Finish M1 gate, then verify M2 |
| M3-01 | M3 | TODO | Pack models/parser/validation | M2 | Stable IDs, schema/version/provenance validation, fail closed, tests | core/learning/, assets/content/ | Not run | Define bounded versioned format |
| M3-02 | M3 | TODO | Lessons, assessments, progress and audio | M3-01 | Real scoring, Room transactions, retry/process recreation, offline lessons, TTS availability | app/feature/lessons/, app/data/ | Not run | Build learning flow |
| M4-01 | M4 | TODO | English A0 foundation content | M3 | Complete planned A0 skill checklist with examples, vocabulary and assessments; language review status | assets/content/, docs/CURRICULUM_COVERAGE.md | Not run | Author and validate A0 pack |
| M4-02 | M4 | TODO | English A1 content | M4-01 | All specified A1 topics and meaningful activities; reviewed coverage | Content and grammar library | Not run | Expand A1 |
| M4-03 | M4 | TODO | English A2 content | M4-02 | All specified A2 topics and meaningful activities; reviewed coverage | Content and grammar library | Not run | Expand A2 |
| M4-04 | M4 | TODO | English B1 content | M4-03 | All specified B1 topics, communication practice, review | Content and grammar library | Not run | Expand B1 |
| M5-01 | M5 | TODO | Fusha A0 foundation | M4 | Alphabet/joining/harakat/reading, sourced language review, RTL | Arabic content | Not run | Follow curriculum |
| M5-02 | M5 | TODO | Fusha A1–B1 | M5-01 | Full requested curriculum and practical exercises; language review | Arabic content | Not run | Expand in level order |
| M6-01 | M6 | TODO | Quranic source/license evaluation and import | M5 | Verified legal source, immutable Arabic, separate annotations, references, frequencies and integrity tests | tools/, content/, CONTENT_SOURCES.md | Not run | Verify sources before any religious content |
| M6-02 | M6 | TODO | Quranic vocabulary/surah comprehension | M6-01 | Verified vocabulary, root/form counts, surah practice and separate progress | feature/quran/ | Not run | Implement only verified data |
| M7-01 | M7 | TODO | Full practice, dictionary, grammar, SRS and analytics | M6 | Exercise coverage, search/Arabic normalization, independent library, SRS history and progress tests | Features, Room, core/learning | Not run | Complete practice requirements |
| M7-02 | M7 | TODO | WorkManager local reminders | M7-01 | Opt-in, notifications permission, due checks, no spam | feature/revision/ | Not run | Add reminders |
| M8-01 | M8 | TODO | Push-to-talk teacher | M7 | Real Android speech, secure optional AI provider, permission/error/cost safeguards | core/audio/, feature/speaking/ | Not run | Offline speech first; no paid usage |
| M9-01 | M9 | TODO | Live provider integration | M8 | Real realtime sessions, backend-issued credentials, controls/limits and review | Optional backend and voice adapter | No provider configured | Prepare secure integration; do not incur charges |
| M10-01 | M10 | TODO | Backup, migrations, recovery, lifecycle and security | M9 | Validated import/export and rollback, migration and reliability tests | data/, tools/, docs/ | Not run | Preserve learner data |
| M11-01 | M11 | TODO | UI/accessibility/Arabic refinement | M10 | Authorized small-device, font-scale, navigation and RTL checks | UI | Not run | Verify actual devices when authorized |
| M12-01 | M12 | TODO | Final audits, APK and delivery | M11 | Test/build report, installation instructions, honest blockers | docs/, debug APK | Not run | Complete available validation |
