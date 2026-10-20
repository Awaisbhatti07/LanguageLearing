package com.lingomate.learning

import com.lingomate.learning.content.*
import org.junit.Assert.*
import org.junit.Test

class ListeningContentTest {
    private val exercise = Exercise.ListeningChoice("listen-name", "What is the speaker's name?",
        "My name is Sara ka matlab mera naam Sara hai.", SpeechReference("My name is Sara.", "en"),
        listOf(ChoiceOption("name-sara", "Sara"), ChoiceOption("name-ali", "Ali")), "name-sara")

    @Test fun correctAndIncorrectListeningChoicesHaveDeterministicFeedback() {
        val correct = ExerciseGrader.grade(exercise, ExerciseAnswer.ListeningSelection("name-sara", ListeningMode.AUDIO))
        assertTrue(correct.valid); assertTrue(correct.correct)
        assertEquals("Sara", correct.expectedAnswer)
        assertEquals(exercise.explanationRomanUrdu, correct.explanationRomanUrdu)
        val wrong = ExerciseGrader.grade(exercise, ExerciseAnswer.ListeningSelection("name-ali", ListeningMode.AUDIO))
        assertTrue(wrong.valid); assertFalse(wrong.correct)
    }

    @Test fun readingAlternativeKeepsItsModeWhenAnswerIsEncodedAndDecoded() {
        val answer: ExerciseAnswer = ExerciseAnswer.ListeningSelection("name-sara", ListeningMode.READING_ALTERNATIVE)
        val restored = ContentCodec.json.decodeFromString<ExerciseAnswer>(ContentCodec.json.encodeToString(answer))
        assertEquals(answer, restored)
        assertTrue(ExerciseGrader.grade(exercise, restored).correct)
    }

    @Test fun plainChoiceTextAndUnknownOptionCannotBypassListeningAnswerShape() {
        assertFalse(ExerciseGrader.grade(exercise, ExerciseAnswer.Selection(listOf("name-sara"))).valid)
        assertFalse(ExerciseGrader.grade(exercise, ExerciseAnswer.Text("Sara")).valid)
        assertFalse(ExerciseGrader.grade(exercise, ExerciseAnswer.ListeningSelection("name-missing", ListeningMode.AUDIO)).valid)
    }

    @Test fun audioMustHaveNonblankValidUnicodeAndASupportedLanguage() {
        listOf(SpeechReference(" ", "en"), SpeechReference("bad\uD800", "en"),
            SpeechReference("Hello", "unknown"), SpeechReference("x".repeat(2001), "en")).forEach { speech ->
            assertThrows(IllegalArgumentException::class.java) { ContentValidator.validateExercise(exercise.copy(speech = speech)) }
        }
    }

    @Test fun listeningOptionsMustBeUnambiguousAndReferToARealAnswer() {
        assertThrows(IllegalArgumentException::class.java) { ContentValidator.validateExercise(exercise.copy(correctOptionId = "name-missing")) }
        assertThrows(IllegalArgumentException::class.java) { ContentValidator.validateExercise(exercise.copy(options = listOf(
            ChoiceOption("name-sara", "Sara"), ChoiceOption("name-other", " SARA ")))) }
    }

    @Test fun speechLanguageMustMatchTheLessonTrack() {
        val pack = ContentCodec.parsePack(requireNotNull(javaClass.getResource("/english-a0-starter.json")).readText())
        val lesson = pack.units.flatMap { it.lessons }.first().copy(exercises = listOf(exercise,
            Exercise.SingleChoice("other-choice", "Choose hello", "Hello aik greeting hai.",
                listOf(ChoiceOption("word-hello", "Hello"), ChoiceOption("word-bye", "Bye")), "word-hello")))
        ContentValidator.validateLesson(lesson, LearningTrack.ENGLISH)
        assertThrows(IllegalArgumentException::class.java) {
            ContentValidator.validateLesson(lesson.copy(exercises = listOf(exercise.copy(speech = SpeechReference("مَرْحَبًا", "ar")),
                lesson.exercises.last())), LearningTrack.ENGLISH)
        }
    }

    @Test fun oldAnswerPayloadsStillDecodeWithoutListeningFields() {
        assertEquals(ExerciseAnswer.Selection(listOf("name-sara")),
            ContentCodec.json.decodeFromString<ExerciseAnswer>("""{"type":"selection","optionIds":["name-sara"]}"""))
    }
}
