# Curriculum coverage

Coverage is based on the user's specification; an authored lesson must have objectives, Roman Urdu explanations, rules, examples, vocabulary meanings, practice/assessment and revision metadata. Language review is separate from automated structural validation. No CEFR certification is claimed.

## English
| Level | Planned skill coverage | Content state | Review state |
| --- | --- | --- | --- |
| A0 | Alphabet/sounds; pronunciation; basic vocabulary; greetings/introductions; pronouns; am/is/are; articles; singular/plural; sentence construction; numbers; time/dates/everyday expressions | Pack v4: 11 lessons/66 activities, including one listening question per lesson. Full run passed 37 core/40 app tests, migrations, APK/lint | Pending qualified language review and physical-device audio; authored coverage is not certified mastery |
| A1 | Present Simple/Continuous; Past Simple; basic future; questions/negatives; prepositions; common verbs; adjectives/adverbs; routines; family/shopping/travel/workplace; guided listening/speaking | Pack v1: 18 lessons/108 activities in four units, all six types, reading/self-speaking. Approved full run passed 43 core/44 app tests, APK/lint; two-pack ordering/A0 preservation and A1 access/resume passed | Qualified language review and device audio pending; automated coverage is not completed/certified A1 proficiency |
| A2 | Present Perfect; Past Continuous; future structures; modals; comparisons; basic conditionals; phrasal verbs; connectors; everyday communication; listening; paragraph writing; roleplay | Pack v1: 21 lessons/128 activities in four units; 126 scored activities including 21 listening questions, plus two saved paragraph/checklist activities. Approved full run passed 53 core/51 app tests, APK/lint; three-pack ordering, old-data preservation and paragraph save/resume passed | PENDING_REVIEW; qualified review and device audio pending; self-writing/roleplay have no automatic quality score |
| B1 | Tense accuracy; conditionals; passive; reported speech; relatives; complex sentences; connected speech; stories/opinions; workplace/interviews/presentations; intermediate reading/listening/writing | Pack v1: 24 lessons/148 activities in four units, including 24 listening questions/four ungraded saved writing tasks. Approved scope passed 60 core/55 app tests, APK/lint; four-level ordering/old data, all writing tasks and direct B1 resume passed | PENDING_REVIEW; qualified review/device audio pending; automated coverage does not complete B1/M4 acceptance |

## Fusha Arabic
| Level | Planned skill coverage | Content state | Review state |
| --- | --- | --- | --- |
| A0 | Alphabet; joining/shapes; sounds; fatha/kasra/damma; sukoon/shaddah/tanween; madd; reading; greetings; basic nouns/verbs/sentences | Draft v1: 12 lessons/72 activities, including 12 listening questions; 67 core/61 app tests and APK/lint passed. Focused Android 36 study/RTL/1.5×/selected-answer/feedback-restart passed; exact scope under evidence/m5-2026-10-26 | PENDING_REVIEW; qualified Fusha/phonetic and physical-audio acceptance pending |
| A1 | Personal/demonstrative pronouns; gender; singular/plural; word classes; nominal/verbal sentences; past/present; questions/negation; prepositions; daily vocabulary/conversation | Draft v1: 18 lessons/108 activities in four units; 18 listening questions. 74 core/65 app tests and build/lint passed; focused Android 36 A1 boundary/RTL/1.5×/feedback-restart/earlier-progress check passed | PENDING_REVIEW; qualified Fusha/phonetic and physical-audio acceptance pending |
| A2 | Conjugation; verb forms; attached pronouns; idafah; adjective agreement; imperatives; sentence patterns; roots; reading/listening/speaking | Draft v1: 20 lessons/122 activities, 20 listening questions and two ungraded Arabic writing tasks; shared 83-core/71-app batch and APK/lint passed; focused Android 36 paragraph/model RTL/1.5×/restart passed | PENDING_REVIEW; qualified Arabic/physical audio pending |
| B1 | Applied Nahw/Sarf; intermediate verbs; connectors/relationships; passages; listening; descriptions/opinions; Fusha conversation; writing | Draft v1: 22 lessons/136 activities, 22 listening questions and four ungraded Arabic writing tasks; shared 83-core/71-app batch and APK/lint passed; Arabic B1 save/resume/RTL passed Compose | PENDING_REVIEW; qualified Arabic/physical audio pending |

## Quranic Arabic
No Quranic material is approved or bundled yet. Source/license acquisition, exact text/reference/word alignment, frequency method and qualified linguistic/religious review are prerequisites. Vocabulary recognition must never be labelled full ayah comprehension.

## Exercise coverage
Verified within recorded automated scope: single/multi-choice, fill blanks, ordering and matching with persisted grading, reading activities, offline device-TTS examples, self-practice speaking prompts and dedicated listening questions with hidden transcripts and durable, separately labelled reading alternatives. Listening JVM tests use fake playback. Recorded Android 36 emulator checks additionally exercised real installed offline TTS completion and durable reading separation; audible/physical audio quality remains unassessed. See M4_ACCEPTANCE.md for the exact representative runtime scope. Still planned: dictation, writing/translation rubrics, assessed speaking and full revision scheduling. Neither reading alternatives nor self-practice prompts establish listening/speaking proficiency.

A2 adds paragraph submission with learner-controlled self-review, an example response and locally saved original writing. This seventh activity is ungraded and excluded from quiz points/denominator; it is not an assessed writing rubric. Practical roleplays specify two roles and a goal, remain self-practice and use no recording or automatic speaking assessment. Paragraph content/validation/persistence/scoring and Compose save/resume passed JVM checks; representative 360×800 dp Android 36 appearance at 1.5× fonts, explicit saving and restart/resume were observed. The approved representative runtime scenarios are recorded in M4_ACCEPTANCE.md, with persistent screenshots and exact limits. Qualified writing-content review and physical audio remain pending.

English A0 lesson mapping: `en-a0-alphabet`, `en-a0-sounds`, `en-a0-greetings`, `en-a0-pronouns-be`, `en-a0-articles`, `en-a0-everyday-words`, `en-a0-plurals`, `en-a0-sentence-building`, `en-a0-numbers`, `en-a0-time-dates`, `en-a0-polite-expressions`. Numbers cover 0–20; time/dates include weekdays, month names, 3:00/3:30 and AM/PM. This remains a foundation, not unlimited vocabulary or audio assessment coverage.

## English A1 draft mapping

Each lesson includes original Roman Urdu teaching, examples, vocabulary, reading, self-speaking and six exercises including a listening question. Topic/key/structural and integration checks passed in the authorized run; this mapping still does not establish expert review or proficiency. Speaking prompts are self-practice with no recording/automatic score; direct grammar access without completing A0 first is covered by the passing navigation test.

| Stable lesson ID | Main teaching/context |
| --- | --- |
| en-a1-routines | Present Simple affirmative, common routine verbs |
| en-a1-do-does | Present Simple negatives and yes-no questions |
| en-a1-question-words | Who/what/where/when/why/how; be and action questions |
| en-a1-present-continuous | Am/is/are + -ing, negatives and questions |
| en-a1-present-contrast | Routine versus action now; basic state-verb distinction |
| en-a1-past-be | Was/were, past states, negatives/questions |
| en-a1-past-regular | Regular past spelling, did/did not + base verb |
| en-a1-past-irregular | Went/had/ate/bought/saw, past questions/negatives |
| en-a1-future-plans | Going-to intentions and simple will decisions/promises |
| en-a1-place-prepositions | In/on/under/next to/between, locating objects |
| en-a1-time-prepositions | At clock times, on days, in months, common exceptions |
| en-a1-adjectives-adverbs | Descriptions and manner, good/well and fast |
| en-a1-frequency | Frequency words/position, daily routines, how often |
| en-a1-family | Family, possessives, have/has and questions |
| en-a1-shopping | Polite requests, prices/counts, receipt/change |
| en-a1-travel | Station/travel questions, simple directions |
| en-a1-workplace | Workday verbs, requests, routine versus now |
| en-a1-guided-conversation | Integrated routine/past/plan dialogue and clarification |

## English A2 draft mapping

Each of the 21 lessons includes original Roman Urdu teaching, four spoken examples, five vocabulary entries, reading, two speaking prompts covering paired roles/role swaps and six scored activities including listening. Connectors and paragraph-writing lessons additionally have an ungraded paragraph activity (128 total activities). The standalone paragraph lesson starts with writing. Content/key/structural and integration checks passed in the authorized run and A2 is in the current APK. This mapping and test success do not establish qualified review or certified proficiency.

| Stable lesson ID | Main teaching/context |
| --- | --- |
| en-a2-perfect-experiences | Have/has + participle, ever/never, life experiences |
| en-a2-perfect-recent | Just/already/yet, recent results and negatives/questions |
| en-a2-perfect-duration | Continuing states, for/since, how long |
| en-a2-perfect-past | Experience versus finished past time, past follow-up questions |
| en-a2-past-continuous | Was/were + -ing, past action in progress |
| en-a2-past-interruptions | Background/shorter events, when/while |
| en-a2-future-arrangements | Present Continuous arrangements, going-to intentions, timetable present |
| en-a2-future-possibility | Will predictions, may/might possibilities |
| en-a2-can-could | Current/past general ability, polite requests |
| en-a2-should-advice | Everyday advice and should not |
| en-a2-obligation | Must/have to; prohibited versus optional |
| en-a2-comparatives | -er/more, than, better/worse, as ... as |
| en-a2-superlatives | The -est/most, best/worst, one of + plural |
| en-a2-zero-conditional | Present + present, repeated routines/results |
| en-a2-first-conditional | If + present, will + base verb, alternative plans |
| en-a2-phrasal-verbs | Get up, look for, put away, turn off, pick up; pronoun position |
| en-a2-connectors | And/but/because/so, first/then/finally; saved connected account |
| en-a2-everyday-messages | Polite changes to arrangements, time/place and confirmation |
| en-a2-listening-detail | Corrected time/room details and clarification questions |
| en-a2-paragraph-writing | Topic/support/sequence/closing; saved paragraph and self-review |
| en-a2-practical-roleplay | Explain problem, offer alternative, agree and confirm details |

## English B1 draft mapping

Each lesson has original Roman Urdu explanations/rules, four examples with English speech references, five vocabulary entries, a developed reading with Roman Urdu meaning, two role/role-swap prompts, review tags and six scored activities including one listening question. Four lessons additionally save an ungraded writing response/checklist (148 activities total). Content/key/structural and integration checks passed within the approved scope; the inspected APK contains 74 lessons/450 activities/74 listening/six paragraphs across English A0–B1. This mapping and automated success do not establish qualified review or certified proficiency.

| Stable lesson ID | Main teaching/context |
| --- | --- |
| en-b1-tense-accuracy | Routine/current/finished-past meanings; auxiliary agreement and base forms |
| en-b1-perfect-past | Experience versus dated detail; been/gone with return context |
| en-b1-narrative-tenses | Past Continuous background and Past Simple main events |
| en-b1-past-perfect | Had + participle, earlier event and main timeline |
| en-b1-future-review | Immediate decision, prior intention, confirmed arrangement, timetable |
| en-b1-used-to | Old habits/states, did ... use to, distinction from be used to |
| en-b1-conditionals-review | Zero/first conditional, unless meaning and possible plans |
| en-b1-second-conditional | Imagined present/future, if + past / would + base, advice |
| en-b1-passive-present | Am/is/are + participle, processes, optional agent |
| en-b1-passive-past | Was/were + participle, irregular forms, completed-event reporting |
| en-b1-reported-statements | Contextual backshift/pronouns/time, said versus told + person |
| en-b1-reported-questions | Statement word order, if/whether, contextual backshift |
| en-b1-defining-relatives | Who/which/that/where, identifying information, no repeated subject |
| en-b1-nondefining-relatives | Extra information, who/which and paired commas, no that |
| en-b1-complex-sentences | Because/although/so that, reason/concession/purpose |
| en-b1-connected-speech | Linking, weak forms, stress/meaning groups, ordinary spelling and TTS limits |
| en-b1-storytelling | Setting/problem/response/outcome; saved narrative and self-review |
| en-b1-opinions | Opinion/reason/example/alternative/conclusion; saved opinion and self-review |
| en-b1-workplace | Clarify task/version/dependency, owner/deadline, summarise agreement |
| en-b1-interviews | Relevant situation/action/result, clarification and truthful limits |
| en-b1-presentations | Purpose/signposts/examples/conclusion; saved proposal script and self-review |
| en-b1-reading-purpose | Developed notice, purpose/main idea/detail/evidence/not stated |
| en-b1-listening-inference | Wording-supported intention, cautious interest versus confirmed promise |
| en-b1-writing-messages | Context/reason/alternative/pending detail/next step; saved message first |

Connected-speech practice introduces awareness and clearly states that installed voices may not demonstrate natural reductions; its listening question checks the script’s meaning. Writing, roleplay and presentation practice do not record speech or automatically assess grammar/argument/pronunciation quality. Longer reading and listening scripts, explanations and approximate pronunciation guides still need qualified review and real-device audio checks. Full independent grammar library, assessed writing/speaking, dictation, translation rubrics and SRS remain M7/later work.

## Fusha A0 draft mapping

Each lesson has original Roman Urdu teaching, at least three Arabic examples/vocabulary entries, marked reading, two self-speaking prompts and six scored activities (single/multiple choice, marked text, ordering, matching and listening). Typed prompts explicitly request the shown marks; missing/different harakat are not silently normalized away. This is a foundation draft, not certified proficiency or a full pronunciation/conjugation course. No Quranic dataset is included.

| Stable lesson ID | Main teaching/context |
| --- | --- |
| ar-a0-alphabet | All 28 basic letters, names versus sounds, right-to-left letter lists |
| ar-a0-shapes | Dot families and contextual baa shapes |
| ar-a0-joining | Connections, six basic right-joining letters and standalone hamza |
| ar-a0-short-vowels | Fatha/kasra/damma, short ba/bi/bu |
| ar-a0-sukoon | No added vowel, closed syllables |
| ar-a0-shaddah | Doubled consonants, darasa versus darrasa |
| ar-a0-tanween | -un/-in/-an marks, introductory joined/stopping forms; case grammar deferred |
| ar-a0-madd | Long aa/ii/uu patterns versus short vowels |
| ar-a0-reading | Short marked phrases, word boundaries, this/what |
| ar-a0-greetings | Hello, morning/evening, name/health, addressed gender |
| ar-a0-nouns-verbs | Everyday nouns and three masculine-singular past verbs; full conjugation deferred |
| ar-a0-simple-sentences | Present noun sentences, student gender and demonstratives |

Roman guides are approximate; connected/stopping forms, makharij, shaddah, tanween and the full content need qualified Arabic/real-audio review. Listening fallbacks explicitly save reading-only credit. Speaking stays self-practice, without recording or a pronunciation score. Fusha keeps its own content IDs and progress; English and Quranic progress are separate.

## Fusha A1 draft mapping

Each lesson has at least four Arabic examples with matching Arabic speech references, four vocabulary entries, original Roman Urdu explanations/rules, a marked reading, paired self-speaking prompts and six scored activities. Typed prompts explicitly request marked forms; person/gender/case distinctions remain significant. All material is an AI-assisted draft pending qualified review, not a certified A1 course.

| Stable lesson ID | Main teaching/context |
| --- | --- |
| ar-a1-personal-pronouns | Speaker/listener/third person, addressed gender, plural/dual awareness |
| ar-a1-demonstratives | Near/distant singular gender; introductory phrase versus statement |
| ar-a1-gender | Common feminine forms and exceptions; singular adjective agreement |
| ar-a1-plurals | Selected sound/broken plurals; introductory non-human plural agreement |
| ar-a1-word-classes | Broad ism/fil/harf categories, common particles |
| ar-a1-definite-phrases | Article, descriptive phrase versus predicate, sun/moon awareness |
| ar-a1-nominal-sentences | Basic subject/predicate and location statements |
| ar-a1-verbal-sentences | Verb-first singular doer/object, feminine past marker |
| ar-a1-past-verbs | Regular kataba with I/you/we; significant final vowels |
| ar-a1-present-verbs | Selected prefixes/persons, addressed feminine -iina, context |
| ar-a1-questions | Hal yes/no, person/place/time question words |
| ar-a1-negation | Taught la/ma/laysa patterns, limits before later mood/conjugation tables |
| ar-a1-prepositions | Fi/min/ila/ala, marked genitive phrases, location/destination |
| ar-a1-family | Family nouns, fixed my-family introductions; full attachments deferred |
| ar-a1-daily-routine | Simple present actions, morning/evening/each day |
| ar-a1-shopping | Polite requests and price questions; fictional shop context |
| ar-a1-directions | Library/station, near/far and destination clarification |
| ar-a1-guided-conversation | Integrated introduction/action/question/negation/repetition request |

Human/physical review still needs to assess correctness, level suitability, all marked forms/keys, Roman Urdu meanings, pronunciation guides and installed Arabic voice/waqf quality. Roleplay is self-practice without recording or automatic speech assessment. Full conjugation, forms, attached-pronoun/case system and idafah remain A2/later development. No prior English/Fusha A0 asset is revised by this pack.

## Fusha A2/B1 draft mapping

All lessons include original Roman Urdu teaching/rules, at least four marked Arabic examples and vocabulary entries, original reading and guided self-speaking plus six scored activity types. A2 has two and B1 four additional saved self-writing tasks, excluded from quiz points. B1 readings omit most vowel marks while marked teaching examples remain available; unmarked ambiguity must be resolved from context, not a claimed automatic diacritization engine. This authored coverage is not qualified proficiency acceptance.

| Stable A2 lesson ID | Main teaching/context |
| --- | --- |
| ar-a2-past-conjugation | Regular past person/number/gender, selected dual/plural forms |
| ar-a2-present-conjugation | Selected present paradigms and five-form awareness |
| ar-a2-form-two | Form II, teaching versus studying |
| ar-a2-forms-three-four | Common Form III/IV pairs and meaning limits |
| ar-a2-forms-five-six | Learning/teaching distinction and cooperation |
| ar-a2-derived-forms | Common VII/VIII/X vocabulary, no invented derivations |
| ar-a2-possessive-suffixes | Noun possession and ta marbuta attachments |
| ar-a2-object-suffixes | Subject/object role and return references |
| ar-a2-preposition-suffixes | Li/ila/fi/min attached forms and spelling |
| ar-a2-idafah | First noun case and second noun genitive relationship |
| ar-a2-adjective-agreement | Gender/case/definiteness and non-human plural agreement |
| ar-a2-dual-plural-cases | Dual and sound-plural case markers |
| ar-a2-imperatives | Selected addressed commands, gender/number |
| ar-a2-prohibition | Prohibition versus negative fact |
| ar-a2-kana | Past states and predicate marking |
| ar-a2-relative-clauses | Alladhi/allati, subject/object links |
| ar-a2-root-families | Taught root vocabulary families, no frequency claims |
| ar-a2-connected-account | Sequence/reason/result, saved self-writing |
| ar-a2-arrangement-details | Corrected room/day details and confirmation |
| ar-a2-practical-message | Problem/alternative/confirmation, saved message |

| Stable B1 lesson ID | Main teaching/context |
| --- | --- |
| ar-b1-case-roles | Doer/object/preposition/idafah roles before endings |
| ar-b1-kana-inna | Contrasting kana/inna effects and states |
| ar-b1-verb-moods | An/lan/lam and indicative/subjunctive/jussive |
| ar-b1-five-verbs | Noon retention/deletion versus feminine-plural noon |
| ar-b1-weak-verbs | Selected hollow/weak-final changes and context |
| ar-b1-passive | Event focus, affected nominative subject, unknown doer |
| ar-b1-participles | Active/passive role and meaningful vowel distinctions |
| ar-b1-verbal-nouns | Masdar patterns and noun constructions |
| ar-b1-agreement-order | Subject-first versus verb-first agreement |
| ar-b1-relative-relationships | Definiteness, relative clause and return pronouns |
| ar-b1-purpose-reason | Purpose versus cause/result, no guaranteed outcome |
| ar-b1-time-relationships | Background/event and sequence |
| ar-b1-conditions | Possible plans versus confirmed events |
| ar-b1-contrast | Concession, alternatives and unchanged details |
| ar-b1-reported-content | Source, perspective and uncertainty |
| ar-b1-reference-links | Pronoun tracking and genuine ambiguity |
| ar-b1-reading-evidence | Main idea/detail/not stated |
| ar-b1-listening-intention | Interest versus final commitment |
| ar-b1-description | Balanced place description, saved writing |
| ar-b1-opinions | Claim/reason/example/alternative/conclusion, saved writing |
| ar-b1-narrative | Setting/problem/response/pending outcome, saved writing |
| ar-b1-formal-message | Context/request/unresolved detail/next step, saved writing |

The four Fusha packs cover authored topics through B1 while retaining expert language, level suitability, phonetics, real voice and physical/audio review gates. Selected paradigms are not exhaustive morphology; full dictionary/grammar/SRS, assessed speaking and AI remain later milestones. No religious text or corpus frequency is bundled.
