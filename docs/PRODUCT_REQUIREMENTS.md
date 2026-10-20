# LINGOMATE AI — COMPLETE A-TO-Z ANDROID APP DEVELOPMENT

## ROLE AND PRIMARY OBJECTIVE

Act as a senior Android engineer, software architect, language-learning product designer, curriculum engineer, database engineer, QA engineer, and security reviewer.

Build a complete, polished, production-quality, Android-only language learning application named **LingoMate AI**.

This is a new standalone project intended for personal use initially, with the possibility of public release later.

**Your mission is to implement a real, fully functional application from A to Z, not merely design screens, create mockups, write specifications, or produce demonstration-only functionality.**

The application must teach:

1. English — absolute beginner to intermediate (A0–B1).
2. Arabic — Modern Standard Arabic (Fusha), A0–B1.
3. Quranic Arabic — frequently occurring Quranic vocabulary, meanings, grammar, word recognition, and contextual comprehension.

All learning explanations must be understandable in **Roman Urdu**.

The actual app interface should use simple, clear English.

The primary focus is excellent usability, correctness, educational quality, offline functionality, and safe data persistence.

---

# 1. NON-NEGOTIABLE REQUIREMENTS

- Android application only. Do not create iOS, Flutter, React Native, or web application projects.
- Use native Kotlin with Jetpack Compose and Material 3.
- Use current stable, mutually compatible Android libraries after verifying their versions.
- Target modern Android phones, including Redmi Note 12.
- Provide a simple and attractive interface suitable for nontechnical beginners.
- Default to dark theme, with an optional light theme.
- Support Android system font scaling and accessibility.
- Fully support properly rendered Arabic script, harakat, and right-to-left Arabic content.
- Use English UI labels and Roman Urdu learning explanations.
- Be offline-first.
- Store user progress safely on-device.
- Do not require login for personal use.
- Make paid AI functionality optional and clearly explain its configuration requirements.
- Never embed paid API secrets in an APK.
- Never claim incomplete or mocked functionality is operational.
- Never fabricate Quranic text, verses, references, translations, word frequencies, or linguistic annotations.
- Do not stop after a few screens. Continue systematically through all milestones, subject to available execution limits and genuine blockers.

Before starting, inspect the repository and environment. If it is a new empty project, initialize it. If LingoMate already exists, inspect and continue existing work rather than replacing it.

Never modify unrelated projects, files, repositories, user data, or system settings.

---

# 2. REQUIRED TECHNOLOGY

Use:

- Kotlin
- Jetpack Compose
- Material 3
- MVVM with clean, feature-oriented architecture
- AndroidX Navigation Compose
- Kotlin Coroutines and Flow
- Room database
- DataStore Preferences
- WorkManager for scheduled local revision reminders
- Retrofit/OkHttp only where networking is required
- Dependency injection using Hilt or a simpler justified alternative
- Android TextToSpeech, where suitable
- Android SpeechRecognizer where supported
- Replaceable speech-to-text, text-to-speech, and AI-provider interfaces
- JUnit and appropriate Android/Compose testing frameworks
- Gradle Kotlin DSL
- Git version control

Prefer maintainable implementation over unnecessary abstraction.

Use a repository structure similar to:

app/
core/
  designsystem/
  database/
  model/
  navigation/
  preferences/
  audio/
  common/
feature/
  onboarding/
  home/
  courses/
  lessons/
  grammar/
  vocabulary/
  speaking/
  listening/
  practice/
  quran/
  revision/
  progress/
  settings/
data/
  content/
  repositories/
docs/
tools/
backend/ (only if required for secure online AI integration)

Adjust structure where technically justified. Do not create unnecessary empty modules.

---

# 3. USER INTERFACE AND UX

Design a polished, extremely easy-to-use application.

UI principles:

- Clean and minimal.
- Consistent spacing and typography.
- Attractive but restrained colors.
- Large, readable lesson text.
- Clearly visible primary actions.
- Minimal unnecessary navigation.
- No cluttered dashboards.
- No excessive settings.
- No complicated learning workflow.
- Comfortable use on smaller Android phones.
- Accessible touch targets.
- Correct empty, loading, error, and offline states.
- Smooth navigation without excessive animations.
- Back navigation must behave correctly.
- Never display dead buttons.

Use a simple bottom navigation:

1. Home
2. Learn
3. Speak
4. Review

Provide Profile/Settings from the top-level menu.

On the Home screen, display three clear learning cards:

**Learn English**

**Learn Arabic**

**Understand Quranic Arabic**

Each card should show relevant progress and a prominent Continue button.

Also display a concise daily learning plan and revision reminders.

Use interactive exercises rather than long walls of text.

Support audio playback with appropriate controls.

Do not show decorative controls that have no functionality.

---

# 4. COMPLETE ENGLISH LANGUAGE COURSE

Build a structured A0–B1 learning experience.

### A0 — Foundation

- English alphabet and sounds.
- Pronunciation basics.
- Basic vocabulary.
- Greetings and introductions.
- Pronouns.
- Am/is/are.
- Articles.
- Singular and plural.
- Basic sentence construction.
- Numbers, time, dates, and everyday expressions.

### A1 — Beginner

- Present Simple.
- Present Continuous.
- Past Simple.
- Basic future expressions.
- Negatives and questions.
- Prepositions.
- Common verbs.
- Adjectives and adverbs.
- Daily routines.
- Family, shopping, travel, and workplace basics.
- Guided listening and speaking.

### A2 — Elementary

- Present Perfect.
- Past Continuous.
- Future structures.
- Modal verbs.
- Comparatives and superlatives.
- Basic conditional sentences.
- Common phrasal verbs.
- Sentence connectors.
- Everyday communication.
- Listening comprehension.
- Paragraph writing.
- Practical roleplay.

### B1 — Intermediate

- Tense accuracy.
- Conditionals.
- Passive voice.
- Reported speech.
- Relative clauses.
- Complex sentence construction.
- Connected speech.
- Storytelling.
- Giving opinions.
- Workplace conversations.
- Interviews.
- Presentations.
- Intermediate reading, listening, and writing.

Each lesson must include:

- Learning objective.
- Roman Urdu explanation.
- Clear grammar rules.
- Examples.
- Relevant vocabulary.
- Meaning of important words.
- Pronunciation/audio when available.
- Interactive practice.
- Listening or reading activity where appropriate.
- Speaking activity where appropriate.
- Assessment.
- Revision tracking.

Allow users to access grammar topics independently without completing the whole course first.

---

# 5. COMPLETE FUSHA ARABIC COURSE

Create a separate A0–B1 Arabic course.

### A0 — Arabic Foundation

- Arabic alphabet.
- Letter shapes and joining.
- Letter pronunciation.
- Fatha, Kasra, Damma.
- Sukoon, Shaddah, Tanween.
- Madd.
- Basic reading.
- Common greetings.
- Basic nouns and verbs.
- Introduction to simple Arabic sentences.

### A1 — Beginner

- Personal pronouns.
- Demonstrative pronouns.
- Masculine/feminine forms.
- Singular/plural.
- Nouns, verbs, and particles.
- Nominal and verbal sentences.
- Basic past and present verbs.
- Questions and negation.
- Common prepositions.
- Daily Arabic vocabulary.
- Guided conversations.

### A2 — Elementary

- Common conjugation patterns.
- Verb forms.
- Pronoun attachments.
- Idafah.
- Adjective agreement.
- Imperatives.
- Common sentence patterns.
- Vocabulary by roots.
- Reading and listening.
- Practical speaking.

### B1 — Intermediate

- Applied Nahw.
- Applied Sarf.
- Intermediate verb structures.
- Sentence connectors.
- Complex grammatical relationships.
- Reading passages.
- Listening comprehension.
- Descriptions and opinions.
- Intermediate Fusha conversations.
- Structured writing.

Important:

- Teach Arabic with proper diacritics at beginner levels.
- Gradually introduce reading without full diacritics where appropriate.
- Explain rules in Roman Urdu.
- Preserve correct Arabic spelling and grammar.
- Do not confuse Fusha and Gulf dialects.
- Design the system to support future Saudi/Gulf spoken Arabic as a separate learning track.

---

# 6. QURANIC ARABIC LEARNING — MAJOR FEATURE

This is a first-class learning module, not a small optional dictionary.

Its purpose is to help users understand Quranic Arabic through commonly occurring words, morphology, grammar, examples, and contextual understanding.

### 6.1 Quranic frequent words

Build a verified word-frequency learning system.

The learner should be able to study:

- Most frequently occurring Quranic words.
- Common particles and prepositions.
- Common nouns.
- Common verbs.
- Pronouns and attached pronouns.
- Verb forms.
- Root-based vocabulary families.
- Quranic grammatical structures.
- Words from familiar surahs.

Use a linguistically appropriate frequency method.

Distinguish:

- Exact surface forms.
- Lemmas.
- Roots.
- Morphological components.

Do not label a root frequency as an exact-word frequency.

Compute rankings from a verified, reproducible source and explain the ranking method.

### 6.2 Quranic vocabulary screen

For each verified entry display:

- Arabic word with correct harakat.
- Romanized pronunciation guidance.
- Roman Urdu meaning.
- Additional contextual meanings where appropriate.
- Root letters, if applicable.
- Part of speech.
- Relevant grammatical forms.
- Verified frequency count and counting methodology.
- Actual Quranic occurrence references.
- Short example from a verified ayah.
- Save to vocabulary.
- Mark as learned.
- Practice/revision option.

For example, entries may include words related to Rabb, knowledge, mercy, worship, guidance, and common Quranic particles.

Do not invent frequency values for these or any other examples.

### 6.3 Quranic comprehension learning

Provide:

- Word-by-word recognition exercises.
- Choose-the-correct-meaning activities.
- Match Arabic words with Roman Urdu meanings.
- Recognize common grammatical forms.
- Understand frequently occurring particles.
- Identify words within short Quranic passages.
- Short passage comprehension with sourced explanations.
- Revision quizzes.
- Progress by recognized vocabulary and comprehension.

Teach the difference between the literal meaning of an isolated word and its contextual meaning within an ayah.

Vocabulary knowledge alone must never be presented as equivalent to understanding an entire verse.

### 6.4 Surah vocabulary mode

Allow learners to select a supported surah and study its verified vocabulary.

Show:

- Surah and ayah reference.
- Original Arabic text.
- Verified vocabulary segments.
- Meanings.
- Related grammar concepts.
- Learned vs unfamiliar vocabulary.
- Reading/word recognition practice.

Begin with manageable, carefully verified selections and expand systematically.

### 6.5 Quranic source accuracy and licensing

Use trustworthy, legally usable sources.

Evaluate Tanzil for the original Quranic text and the Quranic Arabic Corpus for linguistic information, subject to their actual licensing restrictions.

- Preserve original Quran text verbatim.
- Include all required attribution and license notices.
- Never alter original ayat to simplify a lesson.
- Store word boundaries, morphology, translations, and educational annotations separately.
- Validate surah number, ayah number, source text, and word alignment.
- Never invent missing translations or source references.
- Check copyright and redistribution rights for any translation or recitation audio.
- If a reliable licensed resource cannot be acquired, clearly document the blocker and do not silently substitute AI-generated religious content.
- Keep scholarly/linguistic explanations distinct from definitive tafsir.
- Do not claim tajweed or recitation accuracy scoring without a genuinely validated specialist implementation.

Create automated checks for Quranic data integrity, reference consistency, frequency calculation, and import correctness.

Religious content correctness is a release-critical requirement.

---

# 7. VOCABULARY & DICTIONARY ENGINE

Create a complete searchable vocabulary system.

Support English, Fusha Arabic, and Quranic Arabic.

Required features:

- Search by word.
- Meanings in Roman Urdu.
- Pronunciation guidance.
- Audio where supported.
- Example sentences.
- Part of speech.
- Word forms.
- Synonyms/antonyms when applicable.
- Arabic root search.
- Save/unsave vocabulary.
- My Words collection.
- Learned/unlearned states.
- Spaced repetition.
- Search history.
- Vocabulary categories.
- Links to related grammar lessons.

Provide distinct dictionary senses for words with multiple meanings.

Ensure Arabic root-based and diacritic-insensitive search do not corrupt original Arabic display text.

---

# 8. COMPLETE GRAMMAR ENGINE

Build an independent grammar library.

Requirements:

- Search and filter grammar topics.
- Beginner-to-intermediate organization.
- Roman Urdu explanations.
- Correct examples.
- Mistake explanations.
- Practice questions.
- Related vocabulary.
- Track completed topics.
- Bookmark grammar lessons.
- Links from AI corrections to relevant grammar topics.

Avoid teaching grammar exclusively through memorization; combine rules with practical sentence-building activities.

---

# 9. INTERACTIVE LEARNING AND TEST ENGINE

Support:

- Single-choice quizzes.
- Multi-choice quizzes.
- Fill in the blanks.
- Sentence ordering.
- Match pairs.
- Word-to-meaning matching.
- Listening comprehension.
- Dictation.
- Translation exercises.
- Writing responses.
- Pronunciation repetition.
- Guided speaking.

Implement actual answer validation and feedback.

Store attempts and mastery results.

Do not give false precision for open-ended speaking and writing scores. Use transparent rubrics and flag uncertainty.

Every educational activity must provide a meaningful result and persist learner progress.

---

# 10. AI SPEAKING TEACHER

Implement TWO conversation modes.

### Mode A: Push-to-talk

The user presses the microphone button, speaks English or Arabic, and receives:

- Transcription.
- Spoken AI response.
- Written response.
- Grammar corrections.
- Relevant Roman Urdu explanations.
- Corrected sentences.
- Suggestions for improvement.
- Related lesson links.

Use supported Android speech facilities where practical, with a replaceable online recognition provider.

Handle denied permissions, speech service unavailability, network errors, cancellation, and retries.

### Mode B: Live conversation

Implement real-time spoken interaction through a supported AI voice provider.

Requirements:

- Natural conversational turn-taking.
- English and Fusha Arabic.
- Configurable speaking difficulty.
- Teacher voice playback.
- Pause/mute/end controls.
- Conversation transcript.
- Error recovery.
- User interruptions where supported.
- Session summary.
- Correction review.
- Session usage tracking.

Do not claim full-duplex live voice is implemented if only a push-to-talk prototype exists.

### AI teacher behavior

Provide three teaching modes:

1. Guided lessons.
2. Free conversation.
3. Scenario roleplay.

Examples:

- Self-introduction.
- Restaurant.
- Airport.
- Shopping.
- Workplace.
- Job interview.
- Daily routine.
- Family conversation.

The tutor should:

- Adapt to the learner's selected level.
- Ask appropriate questions.
- Encourage the student to speak.
- Explain errors in Roman Urdu.
- Avoid excessive interruptions.
- Keep relevant conversation context.
- Distinguish grammatical errors from acceptable accent variation.
- Summarize important mistakes afterward.
- Avoid inventing Quranic references or theological interpretations.

### Secure AI architecture

ChatGPT Plus is not an API credential.

Online paid voice functionality requires separately configured provider credentials and may incur charges.

Use secure backend-issued short-lived session credentials where supported.

Never put a private API key in source code, Git, APK resources, or application preferences.

Implement configurable session limits, provider error handling, usage logging, and a spending safeguard.

If no paid service is configured, all offline learning features must continue working. The app should clearly indicate that online AI conversation needs configuration.

Provide a fully documented secure setup process for activating both modes. Do not fake live conversations with hardcoded responses.

---

# 11. OFFLINE-FIRST FUNCTIONALITY

The following features must work without internet:

- Course navigation.
- Bundled lessons.
- Grammar explanations.
- Local dictionary.
- Quranic vocabulary packs.
- Supported Quranic reading content.
- Quizzes.
- Word meanings.
- Saved vocabulary.
- Lesson progress.
- Revision scheduling.
- Locally available lesson audio.
- Settings.
- Learning statistics.

Online functionality must be clearly distinguished and gracefully disabled when unavailable.

Add local export/import backup functionality with data validation and a documented recovery procedure.

Never delete progress during normal updates.

---

# 12. DATABASE AND CONTENT MODEL

Design a complete Room database with entities covering:

- Profile.
- Settings.
- Languages.
- Course levels.
- Units.
- Lessons.
- Grammar topics.
- Vocabulary entries.
- Vocabulary senses.
- Word forms.
- Examples.
- Exercises.
- Exercise attempts.
- Lesson progress.
- Skill mastery.
- Revision schedules.
- Vocabulary learning history.
- Voice sessions.
- Voice transcripts.
- Content packs.
- Quranic surahs.
- Quranic ayat.
- Quranic vocabulary.
- Quranic word occurrences.
- Quranic morphology.
- Quranic frequency statistics.
- Quranic learning progress.

Use stable identifiers, foreign keys, uniqueness rules and appropriate indexing.

Maintain separate learner-progress data from educational content packs.

Support versioned content imports.

Implement explicit migrations and migration tests.

Never use destructive fallback migration on real learner data.

---

# 13. CONTENT DEVELOPMENT REQUIREMENTS

Build a meaningful, structured curriculum—not hundreds of empty lesson records.

Use content packs with clear schema versions, validation rules, and source provenance.

Each lesson should contain:

- Language.
- Level.
- Learning objectives.
- Grammar explanations.
- Vocabulary.
- Example sentences.
- Roman Urdu meanings.
- Activities.
- Exercises.
- Correct answers.
- Explanations.
- Speaking prompts.
- Review metadata.

Develop comprehensive A0–B1 coverage for English and Fusha Arabic.

Track coverage against a curriculum checklist.

Do not mark a curriculum complete unless its planned skill coverage and content pass validation.

Prefer fewer thoroughly validated lessons over large quantities of duplicated or incorrect material.

Where qualified language or Quranic content review remains outstanding, mark the material clearly as pending review and document that limitation.

---

# 14. LEARNING PROGRESS

Implement:

- Daily study goals.
- Completed lessons.
- Progress per language.
- Vocabulary mastery.
- Grammar mastery.
- Quranic word learning progress.
- Review due items.
- Learning streaks, without punitive design.
- Weak-topic indicators.
- Assessment history.
- Speaking-session summaries.
- Personal learning recommendations based on actual stored progress.

Track English, Fusha Arabic, and Quranic vocabulary independently while allowing verified shared vocabulary relationships.

---

# 15. REMINDERS AND REVISION

Use a local spaced-repetition system.

Support:

- New words.
- Words needing practice.
- Mastered words.
- Due-for-review words.
- Revision history.
- Configurable reminders.
- Notification permission handling.
- Review progress preservation.

Use a documented scheduling algorithm and unit-test its behavior.

Avoid unnecessary notification spam.

---

# 16. PRIVACY, SECURITY, AND STABILITY

- Personal-use local mode requires no account.
- Minimize permissions.
- Request microphone permission only when voice features are opened.
- Do not request unnecessary contacts, location, or storage permissions.
- Voice recording persistence should be opt-in.
- Make saved transcript deletion possible.
- Secure personal data exports.
- Use Android-appropriate secure credential storage.
- Use HTTPS/TLS for online services.
- Protect backend usage against unauthorized requests.
- No hardcoded credentials.
- No secrets in logs.
- No telemetry by default without permission.
- No unaudited external analytics SDKs.
- Gracefully handle process recreation, app termination, audio interruptions, and low-memory conditions.

---

# 17. TESTING AND VERIFICATION

Create and execute meaningful tests for:

1. Database persistence.
2. Database migration safety.
3. Content imports.
4. Curriculum validation.
5. Exercise scoring.
6. Vocabulary searches.
7. Arabic Unicode handling.
8. Quranic reference integrity.
9. Quranic frequency calculations.
10. Spaced repetition scheduling.
11. User learning progress.
12. App navigation.
13. Theme behavior.
14. Offline functionality.
15. Microphone permission states.
16. Voice provider failure/recovery.
17. AI service usage restrictions.
18. Configuration and credential security.
19. Backup/export and restore.
20. Android device compatibility.

Perform static analysis and build verification.

Use emulator tests and a connected physical Android device where available and authorized.

Never report a test as passed if it was not executed.

Document unavailable testing environments and remaining risks.

---

# 18. IMPLEMENTATION MILESTONES

Work through these milestones in order.

### M1 — Project foundation

Create the Android project, architecture, design system, database foundation, navigation and settings.

### M2 — UI and basic workflows

Implement onboarding, Home, Learn, Speak, Review and all important supporting screen shells with functional navigation.

### M3 — Learning content engine

Implement content-pack parsing, course structure, reusable lessons, audio references and learner progress persistence.

### M4 — English course

Implement structured English grammar, vocabulary, practice, reading, listening and available learning content through B1.

### M5 — Arabic Fusha course

Implement Arabic reading, pronunciation, vocabulary, Nahw/Sarf, exercises, listening and available learning content through B1.

### M6 — Quranic Arabic

Implement verified Quranic vocabulary, frequency calculation, roots, meanings, references, surah-based learning and revision.

### M7 — Practice and progress

Complete quizzes, deterministic grading, revision scheduling, progress analytics and local reminders.

### M8 — Push-to-talk teacher

Implement microphone integration, speech recognition, secure AI interaction, responses and feedback.

### M9 — Live AI conversation

Implement secure real-time voice communication, session management and conversation review.

### M10 — Backup and reliability

Implement verified export/import, offline behavior, data migrations, recovery and security checks.

### M11 — Final UI refinement

Polish all screens, Android accessibility, Arabic typography, loading/error states and navigation consistency.

### M12 — Final validation and delivery

Complete available tests, audits, Android build, debug APK, installation instructions, verification report and remaining-blocker list.

A milestone is complete only when its deliverables and acceptance tests are met.

Do not skip unfinished milestones or misrepresent incomplete modules as finished.

---

# 19. CRITICAL: CODEX LIMIT RESET AND CONTINUATION SYSTEM

The work may require multiple Codex usage windows or sessions.

Build an interruption-resistant development workflow from the beginning.

### Required persistent files

Create and maintain:

- `AGENTS.md`
- `docs/MASTER_PLAN.md`
- `docs/PROJECT_STATUS.md`
- `docs/TASK_QUEUE.md`
- `docs/DECISIONS.md`
- `docs/TEST_RESULTS.md`
- `docs/KNOWN_ISSUES.md`
- `docs/CONTENT_SOURCES.md`
- `docs/RESUME_INSTRUCTIONS.md`

### AGENTS.md requirements

Document stable coding rules, architecture, test commands, data-safety restrictions, project scope, and resume behavior.

Do not put volatile progress details only in AGENTS.md. Keep the current work state in PROJECT_STATUS.md and TASK_QUEUE.md.

### Task tracking

Give each implementation task a unique ID.

Track:

- Milestone.
- Task ID.
- Status: TODO / IN_PROGRESS / BLOCKED / DONE.
- Files changed.
- Completed acceptance criteria.
- Remaining requirements.
- Test results.
- Dependencies.
- Known issues.
- Relevant Git commit.
- Next action.

Update this information after every meaningful, verified work unit.

### Git checkpointing

- Initialize Git if required.
- Use a dedicated development branch.
- Commit completed and verified logical changes.
- Write descriptive commit messages.
- Keep uncommitted work small.
- Never commit secrets, local credentials or confidential configuration.
- Never force-push or overwrite existing user history.
- Do not push to a remote repository without authorization.

Before interruption, when there is an opportunity to save progress:

1. Save completed source changes.
2. Record current unfinished work.
3. Record actual build and test status.
4. Update the task queue.
5. Update project status.
6. Create a safe Git checkpoint when appropriate.
7. Document any uncommitted changes.
8. Record the exact next task.

Do not falsely assume you can detect an imminent hard usage limit. Save state proactively throughout development.

### Automatic continuation during an active Codex session

While execution is available, continue from one completed task to the next without repeatedly asking for authorization for routine implementation details.

Use independent verification before marking tasks done.

Do not abandon the project after completing a single milestone.

### Continuation after usage reset

When the user restarts Codex or provides a continuation request:

1. Find the existing LingoMate project.
2. Read AGENTS.md.
3. Read MASTER_PLAN.md and PROJECT_STATUS.md.
4. Read TASK_QUEUE.md and RESUME_INSTRUCTIONS.md.
5. Inspect Git status and recent commits.
6. Verify the existing implementation.
7. Identify the earliest incomplete or blocked task.
8. Reconcile any incomplete changes.
9. Continue from the saved checkpoint.
10. Update all progress files as work proceeds.

Never recreate an existing feature solely because a new chat started.

Never reset the database, project, user data, or Git history to simplify continuation.

A new Codex session cannot be assumed to start itself automatically after usage limits reset. The repository must be ready for explicit resumption with a short user command.

---

# 20. COST AND RESOURCE CONTROL

Build offline functionality first.

Minimize cloud dependencies.

Do not assume ChatGPT Plus grants free OpenAI API access.

Keep AI service providers interchangeable.

Keep session duration and usage limits configurable.

Avoid unnecessarily downloading large language models or datasets.

Do not purchase cloud services, deploy production infrastructure, activate paid APIs, or incur charges without authorization.

Document the costs and setup requirements of any online feature.

---

# 21. FINAL COMPLETION CRITERIA

The finished application must:

- Install and launch on Android.
- Provide a clean, complete beginner-friendly UI.
- Support English and Arabic Fusha.
- Teach grammar and vocabulary with Roman Urdu explanations.
- Include a functional Quranic Arabic learning module.
- Support real exercises and revision.
- Save progress reliably.
- Work offline for core learning features.
- Have functional audio/listening features where supported.
- Provide actual push-to-talk behavior when configured.
- Provide actual real-time voice behavior when configured.
- Handle missing paid AI credentials clearly.
- Include validated learning content and accurate religious references.
- Pass available automated checks and builds.
- Include useful documentation.
- Produce a debug APK.
- Explain how to configure optional online voice services.
- Preserve reliable Git development checkpoints.
- Be resumable from the last documented incomplete task.

Features requiring unavailable external services, licensed content, credentials, or manual review must be marked BLOCKED or PENDING VERIFICATION, not DONE.

Do not give a misleading 100% completion report.

---

# 22. START EXECUTION

Begin by:

1. Inspecting the working directory and environment.
2. Confirming you are creating or continuing the correct standalone LingoMate project.
3. Creating the master plan and persistent progress/checkpoint files.
4. Defining the initial architecture and database schema.
5. Beginning milestone M1.
6. Implementing and testing in logical increments.
7. Recording progress continuously.
8. Proceeding to subsequent tasks and milestones as execution capacity allows.

Do not stop merely to provide recommendations or repeat the project description.

Implement the application systematically and verify the actual results.

When work cannot continue, leave a clear persistent checkpoint and an exact resume instruction.

The objective is a genuinely functional Android application with robust continuation across multiple Codex sessions, not a collection of mockups or unfinished feature demonstrations.