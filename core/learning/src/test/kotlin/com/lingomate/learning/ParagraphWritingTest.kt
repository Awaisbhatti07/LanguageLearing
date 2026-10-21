package com.lingomate.learning

import com.lingomate.learning.content.*
import org.junit.Assert.*
import org.junit.Test

class ParagraphWritingTest {
    private val pack by lazy { ContentCodec.parsePack(
        requireNotNull(javaClass.getResource("/english-a2-elementary.json")).readText()) }
    private val lesson get() = pack.units.flatMap { it.lessons }.single { it.id == "en-a2-paragraph-writing" }
    private val writing get() = lesson.exercises.first() as Exercise.ParagraphWriting

    @Test fun arbitraryWritingIsSavedSelfPracticeWithNoAutomaticQualityScore() {
        for (text in listOf("I went to class. I enjoyed it.", "I goed class yesterday.", writing.modelParagraph)) {
            val grade = ExerciseGrader.grade(writing, ExerciseAnswer.ParagraphText(text, emptyList()))
            assertTrue(grade.valid)
            assertFalse(grade.scored)
            assertFalse(grade.correct)
        }
        val checked = ExerciseAnswer.ParagraphText("A different account.", writing.checklist.map { it.id })
        assertTrue(ExerciseGrader.grade(writing, checked).valid)
        assertFalse(ExerciseGrader.grade(writing, checked).scored)
        val quiz = lesson.exercises.filterIsInstance<Exercise.SingleChoice>().single()
        assertTrue(ExerciseGrader.grade(quiz, ExerciseAnswer.Selection(listOf(quiz.correctOptionId))).scored)
    }

    @Test fun malformedOversizedOrBlankWritingAndForeignChecklistIdsAreRejected() {
        val invalid = listOf("", " \n\t", "a".repeat(MAX_PARAGRAPH_CHARACTERS + 1), "text\u0000", "\uD800", "\uDC00")
        invalid.forEach { assertFalse(ExerciseGrader.grade(writing, ExerciseAnswer.ParagraphText(it, emptyList())).valid) }
        for (ids in listOf(listOf("unknown-check"), listOf(writing.checklist.first().id, writing.checklist.first().id))) {
            assertFalse(ExerciseGrader.grade(writing, ExerciseAnswer.ParagraphText("Some writing.", ids)).valid)
        }
        assertFalse(ExerciseGrader.grade(writing, ExerciseAnswer.Text("Wrong answer type")).valid)
    }

    @Test fun originalUnicodeAndLineBreaksRoundTripWithoutNormalizingLearnerWriting() {
        val answer: ExerciseAnswer = ExerciseAnswer.ParagraphText("I practised.\nمرحبا 😊\tCafe\u0301", listOf(writing.checklist.first().id))
        assertTrue(ExerciseGrader.grade(writing, answer).valid)
        assertEquals(answer, ContentCodec.json.decodeFromString<ExerciseAnswer>(ContentCodec.json.encodeToString(answer)))
        assertTrue(ExerciseGrader.grade(writing,
            ExerciseAnswer.ParagraphText("a".repeat(MAX_PARAGRAPH_CHARACTERS), emptyList())).valid)
    }

    @Test fun contentNeedsAValidModelUniqueChecklistAndAtLeastOneScoredActivity() {
        fun rejected(exercise: Exercise.ParagraphWriting) {
            try { ContentValidator.validateExercise(exercise); fail("Invalid paragraph definition accepted") }
            catch (_: IllegalArgumentException) { }
        }
        rejected(writing.copy(modelParagraph = ""))
        rejected(writing.copy(modelParagraph = "a".repeat(MAX_PARAGRAPH_CHARACTERS + 1)))
        rejected(writing.copy(modelMeaningRomanUrdu = ""))
        rejected(writing.copy(checklist = listOf(writing.checklist.first(), writing.checklist.first())))
        try {
            ContentValidator.validateLesson(lesson.copy(exercises = listOf(writing, writing.copy(id = "another-paragraph"))), LearningTrack.ENGLISH)
            fail("A lesson without scored questions accepted")
        } catch (_: IllegalArgumentException) { }
    }
}
