# English M4 acceptance checkpoint

Updated: 2026-10-22 (Asia/Karachi). This records pending work, not a device or qualified-review pass.

## Evidence and current source

- Published B1 source c39c0db/results ab008a2: 60 core/55 app tests, debug APK/lint passed. Four packs contain 74 lessons/450 activities/74 listening questions/six ungraded writing tasks. All provenance remains `PENDING_REVIEW`.
- New source fix: each lesson step gets its own scroll state. Study → first activity, submission → feedback, Next → next activity and feedback → results start at the top. Saving/error/playback/reading-mode changes within the same step retain its position. Other screens keep their existing scroll behavior. A Compose regression source covers the long writing flow; no new checks have run.
- No content IDs, answer/scoring/storage code, schema, dependencies or learner records changed. The existing APK does not contain this scroll fix until a newly authorized build succeeds.

## Proposed execution scope — requires explicit approval

1. Run core/app unit tests, build a debug APK and run lint for the scroll fix. Inspect fresh reports and the actual artifact; use the existing SDK/cache. Do not install SDK components or dependencies beyond this scope.
2. If separately authorized, create a fresh isolated emulator data directory under `/private/tmp` using the already installed Android 36 Google Play ARM64 phone image (`~/Library/Android/sdk/system-images/android-36/google_apis_playstore/arm64-v8a`). Start with a 1080×2400 display at 480 dpi (360×800 dp), then inspect enlarged fonts at 1.5× and both app themes. Keep existing AVD data, accounts, snapshots, physical devices and unrelated projects untouched. Install the resulting debug APK only on that isolated instance. Never use clear-data/uninstall/wipe commands against a user's existing installation.
3. Record the emulator API/image, dimensions, density, theme and font scale. A pass on one installed image is limited to that image; it does not prove minimum API 26, target API 37, Redmi Note 12 or physical audio behavior.
4. Exercise the scenarios below. Save observations/screenshots and exact failures. Fix only relevant issues, then request any new scope required. No paid APIs, online AI, deployment or external messaging.

| Scenario | Acceptance to observe | Current result |
| --- | --- | --- |
| First launch/course | Onboarding/settings persist, A0→A1→A2→B1 order, direct grammar access, no complete-course/certification claim | Not run on device |
| Long lesson transitions | Scroll to Start practice; first activity starts at top. Save a paragraph from the bottom; feedback heading visible. Next question/results start at top | New regression source only; unexecuted |
| Scored practice | All six scored input types, correct/wrong feedback, resume after navigation and process termination, completion once | JVM evidence only |
| Paragraph practice | Explicit save, learner checks/original text, no automatic grade, six scored questions in a seven-activity run, saved text readable on next run | JVM evidence only |
| Reading alternative | Transcript hidden first, durable reading choice before reveal, remains reading after resume/replay, no audio credit | JVM/fake-player evidence only |
| Device TTS | Full-playback gate; stop/back/background behavior; missing voice/error recovery. Use only installed offline voices; absence is recorded, not fixed by unapproved downloads | Not run with actual TTS |
| Small screen/accessibility | Dark/light, enlarged fonts, keyboard, scrolling/touch targets and Back; no clipped questions/actions or unreadable saved writing | Not run on device |

An emulator can establish runtime/UI evidence and actual engine callbacks if a suitable offline voice is installed. Without listening to captured/physical output, do not claim pronunciation, volume, intelligibility, natural connected speech or audio quality was assessed.

## Qualified curriculum review — still requires a reviewer

Use `CURRICULUM_COVERAGE.md` for the 74 stable lesson IDs and `CONTENT_SOURCES.md` for authorship/rights. A qualified English/Roman Urdu reviewer must examine the original versioned JSON, not just the topic list or test pass: grammar/usage and answer keys, explanation/meaning accuracy, progression/level suitability, vocabulary/pronunciation guidance, reading/listening scripts and distractors, inclusive practical scenarios and honest self-review/proficiency limits.

Record the reviewer identity/qualification, date, exact pack ID/version/commit, findings/corrections and approval scope before changing any provenance to `REVIEWED`. Developer source inspection, generated material and JVM tests do not substitute for that approval. Content corrections require a higher pack version and stable existing IDs; existing immutable sessions/answers must remain tied to their original content. Never rewrite learner records to match a new answer key.

M4-01 through M4-04 remain `IN_PROGRESS` until their required evidence exists. Do not silently advance into M5 or label the English course complete. Outstanding independent grammar library, SRS, assessed writing/speaking and later features remain in their original milestones.
