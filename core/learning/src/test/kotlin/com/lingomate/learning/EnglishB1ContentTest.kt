package com.lingomate.learning

import com.lingomate.learning.content.*
import org.junit.Assert.*
import org.junit.Test

class EnglishB1ContentTest {
    private fun pack(file: String = "english-b1-intermediate.json") = ContentCodec.parsePack(
        requireNotNull(javaClass.getResource("/$file")).readText(),
    )
    private fun exercises() = pack().units.flatMap { it.lessons }.flatMap { it.exercises }.associateBy { it.id }

    @Test fun b1HasTwentyFourSubstantialLessonsWithListeningAndFourUngradedWritingTasks() {
        val pack = pack()
        val lessons = pack.units.flatMap { it.lessons }
        assertEquals("english-b1-intermediate", pack.id)
        assertEquals(1, pack.version)
        assertEquals(CefrLevel.B1, pack.level)
        assertEquals(LearningTrack.ENGLISH, pack.track)
        assertEquals(ReviewStatus.PENDING_REVIEW, pack.provenance.reviewStatus)
        assertNull(pack.provenance.reviewer)
        assertEquals(4, pack.units.size)
        assertEquals(List(4) { 6 }, pack.units.map { it.lessons.size })
        assertEquals(24, lessons.size)
        assertEquals(148, lessons.sumOf { it.exercises.size })
        assertEquals(144, lessons.sumOf { it.exercises.count { activity -> activity.isScored } })
        lessons.forEach { lesson ->
            assertTrue(lesson.objectives.size >= 2)
            assertTrue(lesson.rulesRomanUrdu.size >= 4)
            assertTrue(lesson.examples.size >= 4)
            assertTrue(lesson.vocabulary.size >= 5)
            assertNotNull(lesson.reading)
            assertTrue(lesson.reading!!.text.split(Regex("\\s+")).size >= 75)
            assertEquals(2, lesson.speakingPrompts.size)
            assertTrue(lesson.speakingPrompts.all { it.contains("Role A:") && it.contains("Role B:") })
            assertTrue(lesson.speakingPrompts.all { it.contains("swap roles", ignoreCase = true) })
            assertEquals(6, lesson.exercises.filter { it.isScored }.map { it::class }.toSet().size)
            assertEquals(1, lesson.exercises.filterIsInstance<Exercise.ListeningChoice>().size)
        }
    }

    @Test fun requiredB1TopicsArePresentWithoutClaimingQualifiedReview() {
        val tags = pack().units.flatMap { it.lessons }.flatMap { it.reviewTags }.toSet()
        val required = setOf("tense-accuracy", "conditionals", "second-conditional", "passive-voice",
            "reported-speech", "relative-clauses", "complex-sentences", "connected-speech", "storytelling",
            "giving-opinions", "workplace-conversations", "interviews", "presentations", "intermediate-reading",
            "intermediate-listening", "intermediate-writing")
        assertTrue("Missing B1 topics: ${required - tags}", tags.containsAll(required))
        assertEquals(ReviewStatus.PENDING_REVIEW, pack().provenance.reviewStatus)
    }

    @Test fun allB1IdentifiersRemainSeparateFromEarlierLearnerContent() {
        fun ids(pack: ContentPack) = listOf(pack.id) + pack.units.flatMap { unit ->
            listOf(unit.id) + unit.lessons.flatMap { lesson ->
                listOf(lesson.id) + lesson.vocabulary.map { it.id } + lesson.exercises.flatMap { exercise ->
                    listOf(exercise.id) + when (exercise) {
                        is Exercise.ParagraphWriting -> exercise.checklist.map { it.id }
                        is Exercise.SingleChoice -> exercise.options.map { it.id }
                        is Exercise.MultiChoice -> exercise.options.map { it.id }
                        is Exercise.ListeningChoice -> exercise.options.map { it.id }
                        is Exercise.SentenceOrder -> exercise.tokens.map { it.id }
                        is Exercise.MatchPairs -> exercise.pairs.map { it.id }
                        is Exercise.FillBlank -> emptyList()
                    }
                }
            }
        }
        val b1 = ids(pack())
        assertEquals(b1.size, b1.toSet().size)
        for (file in listOf("english-a0-starter.json", "english-a1-beginner.json", "english-a2-elementary.json")) {
            assertTrue(file, b1.toSet().intersect(ids(pack(file)).toSet()).isEmpty())
        }
        assertTrue(pack().units.flatMap { it.lessons }.all { it.id.startsWith("en-b1-") })
    }

    @Test fun grammarKeysDistinguishBackshiftWordOrderParticiplesAndImaginedConditions() {
        val exercises = exercises()
        for ((id, correct, wrong) in listOf(
            Triple("en-b1-tense-accuracy-choice", "Did she finish the report?", "Did she finished the report?"),
            Triple("en-b1-second-conditional-choice", "If I had time, I would practise.", "If I would have time, I practise."),
            Triple("en-b1-passive-past-choice", "The message was written by Mina.", "The message was wrote by Mina."),
            Triple("en-b1-reported-statements-choice", "Mina said that she was busy.", "Mina said me that she was busy."),
            Triple("en-b1-reported-questions-choice", "She asked where I lived.", "She asked where did I live."),
            Triple("en-b1-nondefining-relatives-choice", "Mina, who organises the club, is here.", "Mina, that organises the club, is here."),
            Triple("en-b1-complex-sentences-choice", "Although it was noisy, we heard the talk.", "Although it was noisy, but we heard the talk."),
        )) {
            val exercise = exercises.getValue(id) as Exercise.SingleChoice
            fun answer(text: String) = ExerciseAnswer.Selection(listOf(exercise.options.single { it.text == text }.id))
            assertTrue(id, ExerciseGrader.grade(exercise, answer(correct)).correct)
            assertFalse(id, ExerciseGrader.grade(exercise, answer(wrong)).correct)
        }
        for ((id, correct, wrong) in listOf(
            Triple("en-b1-perfect-past-blank", "gone", "been"),
            Triple("en-b1-past-perfect-blank", "seen", "saw"),
            Triple("en-b1-reported-statements-blank", "would", "will"),
            Triple("en-b1-reported-questions-blank", "could", "can"),
            Triple("en-b1-workplace-blank", "by", "until"),
        )) {
            assertTrue(id, ExerciseGrader.grade(exercises.getValue(id), ExerciseAnswer.Text(correct)).correct)
            assertFalse(id, ExerciseGrader.grade(exercises.getValue(id), ExerciseAnswer.Text(wrong)).correct)
        }
    }

    @Test fun comprehensionKeysUseEvidenceRatherThanAssumedCommitmentOrWrongTime() {
        val exercises = exercises()
        for ((id, correct, wrong) in listOf(
            Triple("en-b1-past-perfect-listen", "The organisers changed the time.", "The volunteer gave the speaker a summary."),
            Triple("en-b1-connected-speech-listen", "After the meeting.", "Before the meeting."),
            Triple("en-b1-presentations-listen", "It is a proposal for a trial.", "It is an approved permanent project."),
            Triple("en-b1-listening-inference-listen", "Interest in helping, with availability still unconfirmed.", "A definite promise to arrive by six."),
            Triple("en-b1-writing-messages-listen", "Whether the upstairs room may be used.", "Whether Thursday comes before Friday."),
        )) {
            val exercise = exercises.getValue(id) as Exercise.ListeningChoice
            fun answer(text: String) = ExerciseAnswer.ListeningSelection(
                exercise.options.single { it.text == text }.id, ListeningMode.AUDIO)
            assertTrue(id, ExerciseGrader.grade(exercise, answer(correct)).correct)
            assertFalse(id, ExerciseGrader.grade(exercise, answer(wrong)).correct)
        }
        val reading = exercises.getValue("en-b1-reading-purpose-multi") as Exercise.MultiChoice
        fun selection(vararg text: String) = ExerciseAnswer.Selection(text.map { value -> reading.options.single { it.text == value }.id })
        assertTrue(ExerciseGrader.grade(reading, selection("The trial is proposed for four weeks.",
            "The usual system continues if too few volunteers are available.")).correct)
        assertFalse(ExerciseGrader.grade(reading, selection("A final total cost has been approved.", "The change is already permanent.")).correct)
    }

    @Test fun allFourWritingTasksRemainUngradedWithOptionalLearnerChecks() {
        val lessons = pack().units.flatMap { it.lessons }
        val writing = lessons.flatMap { it.exercises }.filterIsInstance<Exercise.ParagraphWriting>()
        assertEquals(setOf("en-b1-storytelling-paragraph", "en-b1-opinions-paragraph",
            "en-b1-presentations-paragraph", "en-b1-writing-messages-paragraph"), writing.map { it.id }.toSet())
        assertTrue(lessons.single { it.id == "en-b1-writing-messages" }.exercises.first() is Exercise.ParagraphWriting)
        writing.forEach { exercise ->
            for (answer in listOf(
                ExerciseAnswer.ParagraphText("My own practice writing, with mistakes allowed.", emptyList()),
                ExerciseAnswer.ParagraphText(exercise.modelParagraph, exercise.checklist.map { it.id }),
            )) {
                val result = ExerciseGrader.grade(exercise, answer)
                assertTrue(result.valid)
                assertFalse(result.scored)
                assertFalse(result.correct)
            }
        }
    }

    @Test fun roundTripPreservesOriginalScriptsModelsAndReviewStatus() {
        val original = pack()
        assertEquals(original, ContentCodec.parsePack(ContentCodec.json.encodeToString(original)))
    }
}
