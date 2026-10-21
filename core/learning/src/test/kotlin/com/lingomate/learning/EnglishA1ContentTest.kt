package com.lingomate.learning

import com.lingomate.learning.content.*
import org.junit.Assert.*
import org.junit.Test

class EnglishA1ContentTest {
    private fun pack(file: String = "english-a1-beginner.json"): ContentPack = ContentCodec.parsePack(
        requireNotNull(javaClass.getResource("/$file")).readText(),
    )

    @Test fun a1BundleHasCompleteTeachingAndAllSixActivitiesInEveryLesson() {
        val pack = pack()
        val lessons = pack.units.flatMap { it.lessons }
        assertEquals("english-a1-beginner", pack.id)
        assertEquals(1, pack.version)
        assertEquals(LearningTrack.ENGLISH, pack.track)
        assertEquals(CefrLevel.A1, pack.level)
        assertEquals(ReviewStatus.PENDING_REVIEW, pack.provenance.reviewStatus)
        assertEquals(4, pack.units.size)
        assertEquals(18, lessons.size)
        assertEquals(108, lessons.sumOf { it.exercises.size })
        lessons.forEach { lesson ->
            assertTrue(lesson.objectives.size >= 2)
            assertTrue(lesson.rulesRomanUrdu.size >= 4)
            assertTrue(lesson.examples.size >= 4)
            assertTrue(lesson.vocabulary.size >= 5)
            assertNotNull(lesson.reading)
            assertTrue(lesson.speakingPrompts.size >= 2)
            assertEquals(6, lesson.exercises.map { it::class }.toSet().size)
            assertEquals(1, lesson.exercises.filterIsInstance<Exercise.ListeningChoice>().size)
        }
    }

    @Test fun a1CoverageIncludesEveryRequestedTopicWithoutMarkingReviewComplete() {
        val pack = pack()
        val tags = pack.units.flatMap { it.lessons }.flatMap { it.reviewTags }.toSet()
        val required = setOf("present-simple", "present-continuous", "past-simple", "basic-future",
            "negatives-questions", "prepositions", "common-verbs", "adjectives-adverbs", "daily-routines",
            "family", "shopping", "travel", "workplace", "guided-listening-speaking")
        assertTrue("Missing A1 coverage: ${required - tags}", tags.containsAll(required))
        assertEquals(ReviewStatus.PENDING_REVIEW, pack.provenance.reviewStatus)
        assertNull(pack.provenance.reviewer)
    }

    @Test fun a1IdentifiersDoNotCollideWithExistingA0LearnerContent() {
        val a0 = pack("english-a0-starter.json")
        val a1 = pack()
        fun ids(pack: ContentPack): Set<String> = pack.units.flatMap { it.lessons }.flatMap { lesson ->
            listOf(lesson.id) + lesson.exercises.map { it.id } + lesson.vocabulary.map { it.id }
        }.toSet()
        assertNotEquals(a0.id, a1.id)
        assertTrue(ids(a0).intersect(ids(a1)).isEmpty())
        assertTrue(a1.units.flatMap { it.lessons }.all { it.id.startsWith("en-a1-") })
    }

    @Test fun authoredGrammarKeysDistinguishCorrectEnglishFromCommonErrors() {
        val exercises = pack().units.flatMap { it.lessons }.flatMap { it.exercises }.associateBy { it.id }
        val cases = listOf(
            Triple("en-a1-routines-choice", "She works in a shop.", "She work in a shop."),
            Triple("en-a1-do-does-choice", "She does not drive.", "She does not drives."),
            Triple("en-a1-past-irregular-choice", "Did she go home?", "Did she went home?"),
            Triple("en-a1-future-plans-choice", "She is going to visit us.", "She will to visit us."),
        )
        cases.forEach { (id, correct, wrong) ->
            val exercise = exercises.getValue(id) as Exercise.SingleChoice
            fun answer(text: String) = ExerciseAnswer.Selection(listOf(exercise.options.single { it.text == text }.id))
            assertTrue(id, ExerciseGrader.grade(exercise, answer(correct)).correct)
            assertFalse(id, ExerciseGrader.grade(exercise, answer(wrong)).correct)
        }
        val manner = exercises.getValue("en-a1-adjectives-adverbs-blank")
        assertTrue(ExerciseGrader.grade(manner, ExerciseAnswer.Text("well")).correct)
        assertFalse(ExerciseGrader.grade(manner, ExerciseAnswer.Text("good")).correct)
        val time = exercises.getValue("en-a1-time-prepositions-blank")
        assertTrue(ExerciseGrader.grade(time, ExerciseAnswer.Text("on")).correct)
        assertFalse(ExerciseGrader.grade(time, ExerciseAnswer.Text("at")).correct)
    }

    @Test fun listeningKeysAnswerTheQuestionInsteadOfAnotherDetailInTheScript() {
        val exercises = pack().units.flatMap { it.lessons }.flatMap { it.exercises }.associateBy { it.id }
        val cases = listOf(
            Triple("en-a1-shopping-listen", "Two", "Four"),
            Triple("en-a1-time-prepositions-listen", "Six", "Seven"),
            Triple("en-a1-frequency-listen", "Twice a week", "Every morning"),
            Triple("en-a1-workplace-listen", "After the meeting", "Right now"),
            Triple("en-a1-guided-conversation-listen", "On Saturday", "Yesterday"),
        )
        cases.forEach { (id, correct, wrong) ->
            val exercise = exercises.getValue(id) as Exercise.ListeningChoice
            fun answer(text: String) = ExerciseAnswer.ListeningSelection(
                exercise.options.single { it.text == text }.id, ListeningMode.AUDIO)
            assertTrue(id, ExerciseGrader.grade(exercise, answer(correct)).correct)
            assertFalse(id, ExerciseGrader.grade(exercise, answer(wrong)).correct)
        }
    }

    @Test fun packRoundTripKeepsOriginalTeachingScriptsAndPendingProvenance() {
        val original = pack()
        assertEquals(original, ContentCodec.parsePack(ContentCodec.json.encodeToString(original)))
    }
}
