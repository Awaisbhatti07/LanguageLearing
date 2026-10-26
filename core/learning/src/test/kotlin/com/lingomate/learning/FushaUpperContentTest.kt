package com.lingomate.learning

import com.lingomate.learning.content.*
import java.text.Normalizer
import org.junit.Assert.*
import org.junit.Test

class FushaUpperContentTest {
    private fun pack(file: String) = ContentCodec.parsePack(requireNotNull(javaClass.getResource("/$file")).readText())
    private val a2 get() = pack("fusha-a2-elementary.json")
    private val b1 get() = pack("fusha-b1-intermediate.json")
    private fun lessons(pack: ContentPack) = pack.units.flatMap { it.lessons }
    private fun exercises() = (lessons(a2) + lessons(b1)).flatMap { it.exercises }.associateBy { it.id }

    @Test fun upperLevelsHaveSubstantialTeachingAndEveryRequestedActivityWithoutQualifiedReviewClaims() {
        assertEquals(CefrLevel.A2, a2.level)
        assertEquals(CefrLevel.B1, b1.level)
        for ((pack, count, activityCount) in listOf(Triple(a2, 20, 122), Triple(b1, 22, 136))) {
            assertEquals(LearningTrack.FUSHA, pack.track)
            assertEquals(1, pack.version)
            assertEquals(ReviewStatus.PENDING_REVIEW, pack.provenance.reviewStatus)
            assertNull(pack.provenance.reviewer)
            assertEquals(count, lessons(pack).size)
            assertEquals(activityCount, lessons(pack).sumOf { it.exercises.size })
            for (lesson in lessons(pack)) {
                assertTrue(lesson.objectives.size >= 2)
                assertTrue(lesson.rulesRomanUrdu.size >= 4)
                assertTrue(lesson.examples.size >= 4)
                assertTrue(lesson.vocabulary.size >= 4)
                assertNotNull(lesson.reading)
                assertTrue(lesson.reading!!.text.length >= 100)
                assertTrue(lesson.speakingPrompts.size >= 2)
                assertEquals(6, lesson.exercises.count { it.isScored })
                assertEquals(1, lesson.exercises.filterIsInstance<Exercise.ListeningChoice>().size)
                assertTrue(lesson.examples.all { example -> example.speech?.let { it.languageTag == "ar" && it.text == example.text } == true })
            }
        }
    }

    @Test fun a2CoverageIncludesConjugationFormsAttachmentsIdafahAgreementCommandsRootsAndPractice() {
        val required = setOf("conjugation", "verb-forms", "pronoun-attachments", "idafah", "adjective-agreement",
            "imperatives", "common-sentence-patterns", "roots", "reading-listening", "practical-speaking")
        val tags = lessons(a2).flatMap { it.reviewTags }.toSet()
        assertTrue("Missing A2 topics: ${required - tags}", tags.containsAll(required))
    }

    @Test fun b1CoverageIncludesAppliedGrammarRelationshipsComprehensionAndStructuredCommunication() {
        val required = setOf("applied-nahw", "applied-sarf", "intermediate-verbs", "connectors", "complex-relationships",
            "reading-passages", "listening-comprehension", "descriptions", "opinions", "intermediate-conversation", "structured-writing")
        val tags = lessons(b1).flatMap { it.reviewTags }.toSet()
        assertTrue("Missing B1 topics: ${required - tags}", tags.containsAll(required))
        assertTrue(lessons(b1).all { lesson -> lesson.reading!!.text.none { it in '\u064b'..'\u0652' } })
        assertTrue(lessons(b1).all { lesson -> lesson.examples.any { it.text.any { char -> char in '\u064b'..'\u0652' } } })
    }

    @Test fun grammarKeysDistinguishPersonCaseMoodVoiceAndReferences() {
        val cases = listOf(
            Triple("ar-a2-past-conjugation-single-choice", "كَتَبْنَ", "كَتَبْنَا"),
            Triple("ar-a2-idafah-single-choice", "كِتَابُ الطَّالِبِ", "كِتَابٌ الطَّالِبِ"),
            Triple("ar-a2-adjective-agreement-single-choice", "الْكُتُبُ جَدِيدَةٌ.", "الْكُتُبُ جَدِيدُونَ."),
            Triple("ar-a2-kana-single-choice", "كَانَ الْبَيْتُ كَبِيرًا.", "كَانَ الْبَيْتُ كَبِيرٌ."),
            Triple("ar-b1-kana-inna-single-choice", "إِنَّ الطَّالِبَ مُسْتَعِدٌّ.", "إِنَّ الطَّالِبُ مُسْتَعِدًّا."),
            Triple("ar-b1-verb-moods-single-choice", "لَمْ أَكْتُبْ", "لَنْ أَكْتُبَ"),
            Triple("ar-b1-five-verbs-single-choice", "تَكْتُبِي", "تَكْتُبِينَ"),
            Triple("ar-b1-weak-verbs-single-choice", "يَقُلْ", "يَقُولُ"),
            Triple("ar-b1-participles-single-choice", "مُنَظِّمٌ", "مُنَظَّمٌ"),
        )
        val all = exercises()
        for ((id, right, wrong) in cases) {
            val exercise = all.getValue(id) as Exercise.SingleChoice
            fun answer(value: String) = ExerciseAnswer.Selection(listOf(exercise.options.single {
                normalizeAnswer(it.text) == normalizeAnswer(value)
            }.id))
            assertTrue(id, ExerciseGrader.grade(exercise, answer(right)).correct)
            assertFalse(id, ExerciseGrader.grade(exercise, answer(wrong)).correct)
        }
    }

    @Test fun exactMarkedTypingPreservesMeaningAndCanonicalEquivalence() {
        val all = exercises()
        val past = all.getValue("ar-a2-past-conjugation-fill-blank")
        assertTrue(ExerciseGrader.grade(past, ExerciseAnswer.Text("كَتَبْنَ")).correct)
        assertFalse(ExerciseGrader.grade(past, ExerciseAnswer.Text("كَتَبْنَا")).correct)
        val voice = all.getValue("ar-b1-passive-fill-blank")
        assertTrue(ExerciseGrader.grade(voice, ExerciseAnswer.Text("يُكْتَبُ")).correct)
        assertFalse(ExerciseGrader.grade(voice, ExerciseAnswer.Text("يَكْتُبُ")).correct)
        assertFalse(ExerciseGrader.grade(voice, ExerciseAnswer.Text("يكتب")).correct)
        val pronoun = all.getValue("ar-a2-preposition-suffixes-fill-blank")
        assertTrue(ExerciseGrader.grade(pronoun, ExerciseAnswer.Text(Normalizer.normalize("إِلَيَّ", Normalizer.Form.NFD))).correct)
    }

    @Test fun listeningKeepsCorrectedDetailUncertaintyAndNotStatedLimits() {
        val cases = listOf(
            Triple("ar-a2-arrangement-details-listening-choice", "Room three.", "Room two."),
            Triple("ar-b1-passive-listening-choice", "The speaker does not identify the doer.", "Khalid, certainly."),
            Triple("ar-b1-conditions-listening-choice", "The reply has not arrived yet.", "The invitation has already been sent."),
            Triple("ar-b1-reading-evidence-listening-choice", "Reading is limited to inside; price is unspecified.", "The event is definitely free."),
            Triple("ar-b1-listening-intention-listening-choice", "Interest with confirmation pending.", "Unconditional confirmed attendance."),
            Triple("ar-b1-narrative-listening-choice", "A question was sent and the reply remains pending.", "The problem was definitely solved."),
        )
        val all = exercises()
        for ((id, right, wrong) in cases) {
            val exercise = all.getValue(id) as Exercise.ListeningChoice
            fun answer(value: String) = ExerciseAnswer.ListeningSelection(exercise.options.single { it.text == value }.id, ListeningMode.AUDIO)
            assertTrue(id, ExerciseGrader.grade(exercise, answer(right)).correct)
            assertFalse(id, ExerciseGrader.grade(exercise, answer(wrong)).correct)
        }
    }

    @Test fun sixWritingTasksRemainUnscoredAndAcceptOriginalMarkedOrUnmarkedArabic() {
        val writingA2 = lessons(a2).flatMap { it.exercises }.filterIsInstance<Exercise.ParagraphWriting>()
        val writingB1 = lessons(b1).flatMap { it.exercises }.filterIsInstance<Exercise.ParagraphWriting>()
        assertEquals(2, writingA2.size)
        assertEquals(4, writingB1.size)
        for (writing in writingA2 + writingB1) {
            for (text in listOf("كتبت فقرة بنفسي.\nهذا مثال خاص بي.", "كَتَبْتُ نَصًّا.", writing.modelParagraph)) {
                val answer = ExerciseAnswer.ParagraphText(text, listOf(writing.checklist.first().id))
                val grade = ExerciseGrader.grade(writing, answer)
                assertTrue(grade.valid)
                assertFalse(grade.scored)
                assertFalse(grade.correct)
                assertEquals(answer, ContentCodec.json.decodeFromString<ExerciseAnswer>(ContentCodec.json.encodeToString<ExerciseAnswer>(answer)))
            }
        }
    }

    @Test fun allEightPacksHaveDistinctLessonActivityAndVocabularyIdentifiers() {
        val files = listOf("english-a0-starter.json", "english-a1-beginner.json", "english-a2-elementary.json",
            "english-b1-intermediate.json", "fusha-a0-foundation.json", "fusha-a1-beginner.json",
            "fusha-a2-elementary.json", "fusha-b1-intermediate.json")
        val ids = files.flatMap { file -> lessons(pack(file)).flatMap { lesson ->
            listOf(lesson.id) + lesson.exercises.map { it.id } + lesson.vocabulary.map { it.id }
        } }
        assertEquals(ids.size, ids.toSet().size)
        assertEquals(146, files.sumOf { lessons(pack(it)).size })
    }

    @Test fun packsRoundTripWithoutReversingArabicOrStoringPresentationGlyphs() {
        for (pack in listOf(a2, b1)) {
            val source = ContentCodec.json.encodeToString(pack)
            assertEquals(pack, ContentCodec.parsePack(source))
            assertTrue(Normalizer.isNormalized(source, Normalizer.Form.NFC))
            assertFalse(source.any { it in '\ufb50'..'\ufdff' || it in '\ufe70'..'\ufeff' || it in '\u202a'..'\u202e' })
        }
    }
}
