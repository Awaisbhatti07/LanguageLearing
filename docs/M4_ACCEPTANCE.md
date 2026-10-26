# English M4 acceptance checkpoint

Updated: 2026-10-26 (Asia/Karachi). Representative automated/runtime checks passed as recorded below; full M4 and qualified curriculum acceptance remain incomplete.

2026-10-26 continuation: UI-01 Review singular-label source is now corrected, with manual source/diff inspection only. The user has now explicitly approved restoring SDK 37/cache and downloading missing pinned build dependencies. Setup and current checks are in progress; inspect results before acceptance. The recorded APK/reports/screenshots below remain evidence for the preceding published source. Keep this new correction local WIP pending verification.

## Source and automated evidence

- 4672698 resets scroll only when a lesson session/step changes; saving/errors/playback retain position. Its long-writing Compose regression passed. 488446d fixes system-bar contrast when app/device themes differ. 52d6f68 fixes singular Home goal/progress labels. No content IDs/versions, grading/storage/schema, dependencies or learner records changed.
- Initial combined core/app/build/lint command passed in 51s: **56 app tests**, 0 failures/errors/skips; unchanged **60 core tests were UP-TO-DATE**, not re-executed. Theme-fix app tests/build/lint passed in 53s; Home-fix app tests/build/lint passed in 56s, again 56 tests/0 failures/errors/skips. Lint contains only the existing newer-Gradle suggestion.
- Verified APK: **13,969,405 bytes**, SHA-256 `001e1054ee323339766c9af546fc44c01e371e2aabedcbf7c33c01ea28dde308`. All four packaged assets match source: 74 lessons/450 activities/74 listening/six paragraphs, all `PENDING_REVIEW`. The 2026-10-23 resumption inspected/reused this artifact and existing reports; it did not rerun tests/build or install SDK/voice components.

## Runtime evidence

User approved option 1 on 2026-10-22 and standing routine project checks (“please don't ask everytime”). Existing Android 36 Google Play ARM64 revision 7 image, 1080×2400/480 dpi (360×800 dp), isolated serial **emulator-5580**, ADB **5039**, no snapshots. Previous temporary AVD/evidence were cleared between sessions; resumption uses fresh synthetic data at `/private/tmp/lingomate-m4-xzzt60ol`. Existing user AVDs/accounts/snapshots/physical devices remain untouched. Wi-Fi/data both read 0 before the speech checks. The installed speech service was temporarily disabled only on this throwaway instance and re-enabled afterward.

[Persistent screenshot evidence](evidence/m4-2026-10-23/README.md) contains original PNGs and environment metadata. Earlier 2026-10-22 observations are retained in TEST_RESULTS.md; their temporary screenshots no longer exist.

| Scenario | Actual observation | Limits / remaining work |
| --- | --- | --- |
| First launch/course | Onboarding/settings and active lesson survived the earlier APK update. A0→A1→A2 traversal observed; B1 reported-question topic opened directly with A0/A1/A2 incomplete. UI shows 74 lessons/pending review and no certification claim | Representative access; not every lesson was exercised |
| Long transitions | A0 study/activity/feedback/results, A2 study→first activity, save-from-bottom→feedback and next-question headings began at the top. A2 first/next activities also observed at 1.5× fonts | New long-writing Compose regression additionally passed |
| Scored practice | A0 all six types, deliberately wrong answer, restart/resume, expected 5/6 result. Fresh A2 seven-activity run finished **6/6 scored**; paragraph excluded. Review shows one completed run; Home one distinct lesson/1 of 74 | App grading/UI evidence, not expert validation of answer keys/proficiency |
| Paragraph practice | Paced 110-character synthetic entry matched displayed field exactly, optional first checklist item saved, ungraded feedback and original text. Earlier saved feedback survived restart at 1.5× fonts | After process restart/new run, original text is present in the 1.5× study review (saved hierarchy/screenshot). Earlier rapid ADB injection delivered only a prefix; pacing resolved that helper issue without source changes |
| Reading alternative | Hidden transcript/disabled options initially; full actual TTS completion unlocks options. Reading persists through restart in the A0 run and replay in fresh A2; feedback/results listening 0/reading 1 | No reading answer counted as listening |
| Device TTS | Real offline engine: stop before completion and background/return kept answers locked; full completion unlocked them. Reading replay retained mode. Disabled speech service produced visible offline-voice error, reading submission still worked; service re-enabled | Back/stale-callback edge cases additionally have JVM/fake-player coverage. Audible/physical output quality was not assessed |
| Appearance/accessibility | Dark/light plus System following device dark/light all show readable matching status/navigation icons. Theme/goal persisted after force-stop. Home/Settings/Review, keyboard, long forms and controls usable at 1.5× with scrolling; goal chips wrap | Minimum API 26, target API 37, physical/Redmi Note 12 behavior and full accessibility audit not established |

An emulator establishes representative runtime/UI and engine-callback evidence. No pronunciation, volume, intelligibility, natural connected-speech or audio-quality assessment is claimed. The minor Review singular-label source fix is tracked as verification-pending in KNOWN_ISSUES.md; recorded screenshots/artifact still contain the earlier copy. Functionality/counts are unaffected.

Final font scale restored to 1.0 and the speech service confirmed enabled, then only emulator-5580 was stopped. Temporary synthetic data is retained; persistent screenshots are committed with the checkpoint.

The initial normal push failed HTTP 403. After the user switched authentication and requested retry, normal publication succeeded through d0c2ed2; remote hash `d0c2ed20ec611f198aa2e897dea766f52f25b1cb` was confirmed. All five saved M4 commits/fixes/evidence are published. No automatic account/remote/main/force-push change, paid service or deployment was performed.

## Qualified curriculum review — still requires a reviewer

Use `CURRICULUM_COVERAGE.md` for the 74 stable lesson IDs and `CONTENT_SOURCES.md` for authorship/rights. A qualified English/Roman Urdu reviewer must examine the original versioned JSON, not just the topic list or test pass: grammar/usage and answer keys, explanation/meaning accuracy, progression/level suitability, vocabulary/pronunciation guidance, reading/listening scripts and distractors, inclusive practical scenarios and honest self-review/proficiency limits.

Record the reviewer identity/qualification, date, exact pack ID/version/commit, findings/corrections and approval scope before changing any provenance to `REVIEWED`. Developer source inspection, generated material and JVM tests do not substitute for that approval. Content corrections require a higher pack version and stable existing IDs; existing immutable sessions/answers must remain tied to their original content. Never rewrite learner records to match a new answer key.

M4-01 through M4-04 remain `IN_PROGRESS` until their required evidence exists. Do not silently advance into M5 or label the English course complete. Outstanding independent grammar library, SRS, assessed writing/speaking and later features remain in their original milestones.
