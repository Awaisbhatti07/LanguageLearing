package com.lingomate.learning

import com.lingomate.learning.content.*
import org.junit.Assert.*
import org.junit.Test

class EnglishA2ContentTest {
    private fun pack(file: String = "english-a2-elementary.json") = ContentCodec.parsePack(
        requireNotNull(javaClass.getResource("/$file")).readText(),
    )

    @Test fun a2HasTwentyOneLessonsWithTeachingListeningAndTwoUngradedParagraphs() {
        val pack = pack()
        val lessons = pack.units.flatMap { it.lessons }
        assertEquals("english-a2-elementary", pack.id)
        assertEquals(1, pack.version)
        assertEquals(LearningTrack.ENGLISH, pack.track)
        assertEquals(CefrLevel.A2, pack.level)
        assertEquals(ReviewStatus.PENDING_REVIEW, pack.provenance.reviewStatus)
        assertNull(pack.provenance.reviewer)
        assertEquals(4, pack.units.size)
        assertEquals(21, lessons.size)
        assertEquals(128, lessons.sumOf { it.exercises.size })
        assertEquals(126, lessons.sumOf { it.exercises.count { exercise -> exercise.isScored } })
        lessons.forEach { lesson ->
            assertTrue(lesson.objectives.size >= 2)
            assertTrue(lesson.rulesRomanUrdu.size >= 4)
            assertTrue(lesson.examples.size >= 4)
            assertTrue(lesson.vocabulary.size >= 5)
            assertNotNull(lesson.reading)
            assertEquals(2, lesson.speakingPrompts.size)
            assertTrue(lesson.speakingPrompts.first().contains("Role A:") && lesson.speakingPrompts.first().contains("Role B:"))
            assertTrue(lesson.speakingPrompts.any { it.contains("swap roles", ignoreCase = true) })
            assertEquals(6, lesson.exercises.filter { it.isScored }.map { it::class }.toSet().size)
            assertEquals(1, lesson.exercises.filterIsInstance<Exercise.ListeningChoice>().size)
        }
        val writing = lessons.flatMap { it.exercises }.filterIsInstance<Exercise.ParagraphWriting>()
        assertEquals(setOf("en-a2-connectors-paragraph", "en-a2-paragraph-writing-paragraph"), writing.map { it.id }.toSet())
        assertTrue(lessons.single { it.id == "en-a2-paragraph-writing" }.exercises.first() is Exercise.ParagraphWriting)
    }

    @Test fun requestedA2TopicsHaveExplicitCoverageWhileReviewRemainsPending() {
        val tags = pack().units.flatMap { it.lessons }.flatMap { it.reviewTags }.toSet()
        val required = setOf("present-perfect", "for-since", "past-continuous", "future-structures",
            "can-could", "should-advice", "must-have-to", "comparatives", "superlatives",
            "zero-conditional", "first-conditional", "phrasal-verbs", "connectors", "paragraph-writing",
            "listening-detail", "practical-roleplay")
        assertTrue("Missing A2 topics: ${required - tags}", tags.containsAll(required))
    }

    @Test fun a2IdentifiersAreSeparateFromBothEarlierLevels() {
        fun ids(pack: ContentPack) = pack.units.flatMap { unit ->
            listOf(unit.id) + unit.lessons.flatMap { lesson ->
                listOf(lesson.id) + lesson.exercises.map { it.id } + lesson.vocabulary.map { it.id }
            }
        }.toSet() + pack.id
        val a2 = ids(pack())
        for (file in listOf("english-a0-starter.json", "english-a1-beginner.json")) {
            assertTrue(file, a2.intersect(ids(pack(file))).isEmpty())
        }
        assertTrue(pack().units.flatMap { it.lessons }.all { it.id.startsWith("en-a2-") })
    }

    @Test fun answerKeysDistinguishFinishedTimeOptionalRulesAndConditionalForms() {
        val exercises = pack().units.flatMap { it.lessons }.flatMap { it.exercises }.associateBy { it.id }
        val cases = listOf(
            Triple("en-a2-perfect-past-choice", "I visited Lahore yesterday.", "I have visited Lahore yesterday."),
            Triple("en-a2-obligation-choice", "You do not have to bring lunch.", "You must not bring lunch."),
            Triple("en-a2-first-conditional-choice", "If the bus is late, we will walk.", "If the bus will be late, we walk."),
            Triple("en-a2-phrasal-verbs-choice", "Please turn it off.", "Please turn off it."),
        )
        cases.forEach { (id, correct, wrong) ->
            val exercise = exercises.getValue(id) as Exercise.SingleChoice
            fun answer(text: String) = ExerciseAnswer.Selection(listOf(exercise.options.single { it.text == text }.id))
            assertTrue(id, ExerciseGrader.grade(exercise, answer(correct)).correct)
            assertFalse(id, ExerciseGrader.grade(exercise, answer(wrong)).correct)
        }
        for ((id, correct, wrong) in listOf(
            Triple("en-a2-perfect-duration-blank", "since", "for"),
            Triple("en-a2-comparatives-blank", "better", "good"),
            Triple("en-a2-first-conditional-blank", "changes", "will change"),
        )) {
            assertTrue(id, ExerciseGrader.grade(exercises.getValue(id), ExerciseAnswer.Text(correct)).correct)
            assertFalse(id, ExerciseGrader.grade(exercises.getValue(id), ExerciseAnswer.Text(wrong)).correct)
        }
    }

    @Test fun listeningUsesCorrectedDetailsAndNewArrangements() {
        val exercises = pack().units.flatMap { it.lessons }.flatMap { it.exercises }.associateBy { it.id }
        for ((id, correct, wrong) in listOf(
            Triple("en-a2-listening-detail-listen", "Five, room twelve.", "Five, room twenty."),
            Triple("en-a2-everyday-messages-listen", "Three.", "Two."),
            Triple("en-a2-practical-roleplay-listen", "At the library entrance.", "Inside the café."),
        )) {
            val exercise = exercises.getValue(id) as Exercise.ListeningChoice
            fun answer(text: String) = ExerciseAnswer.ListeningSelection(
                exercise.options.single { it.text == text }.id, ListeningMode.AUDIO)
            assertTrue(id, ExerciseGrader.grade(exercise, answer(correct)).correct)
            assertFalse(id, ExerciseGrader.grade(exercise, answer(wrong)).correct)
        }
    }

    @Test fun roundTripPreservesTeachingAndParagraphPrompts() {
        val original = pack()
        assertEquals(original, ContentCodec.parsePack(ContentCodec.json.encodeToString(original)))
    }
}
