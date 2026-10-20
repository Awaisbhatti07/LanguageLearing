package com.lingomate.learning

import com.lingomate.learning.content.*
import org.junit.Assert.*
import org.junit.Test

class ContentEngineTest {
    private fun pack(): ContentPack = ContentCodec.parsePack(
        requireNotNull(javaClass.getResource("/english-a0-starter.json")).readText(),
    )
    private fun rejected(block: () -> Unit) {
        assertThrows(IllegalArgumentException::class.java, block)
    }
    private val single = Exercise.SingleChoice("test-single", "Choose a greeting", "Hello aik greeting hai.",
        listOf(ChoiceOption("opt-hello", "Hello"), ChoiceOption("opt-bye", "Goodbye")), "opt-hello")
    private val multiple = Exercise.MultiChoice("test-multiple", "Choose pronouns", "I aur you pronouns hain.",
        listOf(ChoiceOption("opt-i", "I"), ChoiceOption("opt-you", "you"), ChoiceOption("opt-book", "book")), listOf("opt-i", "opt-you"))
    private val text = Exercise.FillBlank("test-fill", "I ___ a student.", "I ke saath am aata hai.", listOf("am"))
    private val order = Exercise.SentenceOrder("test-order", "Make a sentence", "Subject pehle aata hai.",
        listOf(ChoiceOption("tok-here", "here."), ChoiceOption("tok-am", "am"), ChoiceOption("tok-i", "I")), listOf("tok-i", "tok-am", "tok-here"))
    private val matches = Exercise.MatchPairs("test-match", "Match meanings", "I ka matlab main hai.",
        listOf(MatchPair("pair-i", "I", "main"), MatchPair("pair-you", "you", "aap")))

    @Test fun bundledPackHasRealLessonsAndAllSixDeterministicActivityTypes() {
        val pack = pack()
        val lessons = pack.units.flatMap { it.lessons }
        assertEquals(11, lessons.size)
        assertTrue(lessons.all { it.exercises.size >= 6 && it.reading != null })
        assertEquals(6, lessons.flatMap { it.exercises }.map { it::class }.toSet().size)
        assertTrue(lessons.all { it.exercises.count { exercise -> exercise is Exercise.ListeningChoice } == 1 })
        assertEquals(ReviewStatus.PENDING_REVIEW, pack.provenance.reviewStatus)
    }
    @Test fun a0PackCoversEveryPlannedFoundationSkillWithoutEmptyLessons() {
        val lessons = pack().units.flatMap { it.lessons }
        val tags = lessons.flatMap { it.reviewTags }.toSet()
        val required = setOf("alphabet", "pronunciation-basics", "basic-vocabulary", "greetings", "introductions",
            "subject-pronouns", "be-verb", "indefinite-articles", "singular-plural", "basic-sentences", "numbers", "time", "dates", "everyday-expressions")
        assertTrue("Missing A0 coverage: ${required - tags}", tags.containsAll(required))
        assertEquals(66, lessons.sumOf { it.exercises.size })
        assertTrue(lessons.all { it.examples.size >= 3 && it.vocabulary.size >= 3 })
    }
    @Test fun packRoundTripPreservesDisplayTextAndHarakat() {
        val original = pack().let { pack -> pack.copy(track = LearningTrack.FUSHA, units = pack.units.map { unit ->
            unit.copy(lessons = unit.lessons.map { lesson -> lesson.copy(examples = listOf(
                LessonExample("أَنَا طَالِبٌ", "Main talib-e-ilm hoon.", SpeechReference("أَنَا طَالِبٌ", "ar")),
            ), exercises = lesson.exercises.map { exercise ->
                if (exercise is Exercise.ListeningChoice) exercise.copy(speech = SpeechReference("أَنَا طَالِبٌ", "ar")) else exercise
            }) })
        }) }
        val encoded = ContentCodec.json.encodeToString(original)
        assertEquals(original, ContentCodec.parsePack(encoded))
        assertTrue(ContentCodec.parsePack(encoded).units.first().lessons.first().examples.first().text.contains("ٌ"))
    }
    @Test fun unsupportedSchemaIsRejected() { rejected { ContentValidator.validate(pack().copy(schemaVersion = 99)) } }
    @Test fun invalidPackVersionIsRejected() { rejected { ContentValidator.validate(pack().copy(version = 0)) } }
    @Test fun duplicateLessonIdentifiersAreRejected() {
        val pack = pack(); val unit = pack.units.first()
        rejected { ContentValidator.validate(pack.copy(units = listOf(unit.copy(lessons = listOf(unit.lessons.first(), unit.lessons.first()))))) }
    }
    @Test fun unsourcedQuranicPackIsRejected() { rejected { ContentValidator.validate(pack().copy(track = LearningTrack.QURANIC)) } }
    @Test fun reviewedContentRequiresNamedReviewer() {
        val pack = pack()
        rejected { ContentValidator.validate(pack.copy(provenance = pack.provenance.copy(reviewStatus = ReviewStatus.REVIEWED))) }
    }
    @Test fun unsupportedExerciseTypeCannotBeSilentlyIgnored() {
        val source = ContentCodec.json.encodeToString(pack()).replace("single_choice", "pretend_score")
        rejected { ContentCodec.parsePack(source) }
    }
    @Test fun unknownJsonFieldsAreRejected() {
        val source = ContentCodec.json.encodeToString(pack()).replaceFirst("{", "{\"surprise\":true,")
        rejected { ContentCodec.parsePack(source) }
    }
    @Test fun oversizedPackIsRejectedBeforeDecoding() { rejected { ContentCodec.parsePack(" ".repeat(ContentCodec.MAX_PACK_BYTES + 1)) } }
    @Test fun excessiveJsonNestingIsRejected() { rejected { ContentCodec.parsePack("[".repeat(33) + "]".repeat(33)) } }
    @Test fun malformedUnicodeTextIsRejected() {
        val pack = pack(); val unit = pack.units.first(); val lesson = unit.lessons.first()
        rejected { ContentValidator.validate(pack.copy(units = listOf(unit.copy(lessons = listOf(lesson.copy(title = "bad\uD800")))))) }
    }
    @Test fun missingTeachingExplanationIsRejected() {
        val lesson = pack().units.first().lessons.first().copy(explanationRomanUrdu = " ")
        rejected { ContentValidator.validateLesson(lesson, LearningTrack.ENGLISH) }
    }
    @Test fun spokenTextMustMatchTheDisplayedExample() {
        val lesson = pack().units.first().lessons.first().copy(examples = listOf(LessonExample("Hello", "Salam", SpeechReference("Goodbye", "en"))))
        rejected { ContentValidator.validateLesson(lesson, LearningTrack.ENGLISH) }
    }
    @Test fun choiceCannotReferToAMissingAnswer() { rejected { ContentValidator.validateExercise(single.copy(correctOptionId = "missing-answer")) } }
    @Test fun equivalentDisplayChoicesAreRejected() {
        rejected { ContentValidator.validateExercise(single.copy(options = listOf(ChoiceOption("one-opt", "Hello"), ChoiceOption("two-opt", " HELLO ")))) }
    }
    @Test fun ambiguousMatchingMeaningsAreRejected() {
        rejected { ContentValidator.validateExercise(matches.copy(pairs = listOf(MatchPair("one-pair", "I", "main"), MatchPair("two-pair", "me", "MAIN")))) }
    }
    @Test fun singleChoiceGradesRightAndWrongAnswers() {
        assertTrue(ExerciseGrader.grade(single, ExerciseAnswer.Selection(listOf("opt-hello"))).correct)
        assertFalse(ExerciseGrader.grade(single, ExerciseAnswer.Selection(listOf("opt-bye"))).correct)
    }
    @Test fun unknownAndDuplicateSelectionsAreInvalid() {
        assertFalse(ExerciseGrader.grade(single, ExerciseAnswer.Selection(listOf("unknown"))).valid)
        assertFalse(ExerciseGrader.grade(multiple, ExerciseAnswer.Selection(listOf("opt-i", "opt-i"))).valid)
        assertFalse(ExerciseGrader.grade(single, ExerciseAnswer.Selection(emptyList())).valid)
    }
    @Test fun multiChoiceNeedsTheExactSetWithoutOrderDependence() {
        assertTrue(ExerciseGrader.grade(multiple, ExerciseAnswer.Selection(listOf("opt-you", "opt-i"))).correct)
        assertFalse(ExerciseGrader.grade(multiple, ExerciseAnswer.Selection(listOf("opt-i"))).correct)
        assertFalse(ExerciseGrader.grade(multiple, ExerciseAnswer.Selection(listOf("opt-i", "opt-you", "opt-book"))).correct)
    }
    @Test fun textUsesWhitespaceCaseAndNfcNormalizationWithoutChangingMeaning() {
        assertTrue(ExerciseGrader.grade(text, ExerciseAnswer.Text(" \u00A0AM\t")).correct)
        assertEquals("café", normalizeAnswer("cafe\u0301"))
        assertFalse(ExerciseGrader.grade(text, ExerciseAnswer.Text("am!")).correct)
    }
    @Test fun arabicHarakatAreNeverStrippedFromAnAnswer() {
        val exercise = text.copy(acceptedAnswers = listOf("كِتَابٌ"))
        assertTrue(ExerciseGrader.grade(exercise, ExerciseAnswer.Text("كِتَابٌ")).correct)
        assertFalse(ExerciseGrader.grade(exercise, ExerciseAnswer.Text("كتاب")).correct)
    }
    @Test fun caseSensitiveActivitiesHonorTheirPolicy() {
        val exercise = text.copy(acceptedAnswers = listOf("I"), ignoreCase = false)
        assertFalse(ExerciseGrader.grade(exercise, ExerciseAnswer.Text("i")).correct)
        assertTrue(ExerciseGrader.grade(exercise, ExerciseAnswer.Text("I")).correct)
    }
    @Test fun blankAndOversizedTextAreInvalid() {
        assertFalse(ExerciseGrader.grade(text, ExerciseAnswer.Text("  ")).valid)
        assertFalse(ExerciseGrader.grade(text, ExerciseAnswer.Text("a".repeat(1001))).valid)
    }
    @Test fun wrongAnswerTypeIsInvalid() { assertFalse(ExerciseGrader.grade(single, ExerciseAnswer.Text("Hello")).valid) }
    @Test fun sentenceOrderRequiresAllTokensExactlyOnce() {
        assertTrue(ExerciseGrader.grade(order, ExerciseAnswer.Order(listOf("tok-i", "tok-am", "tok-here"))).correct)
        assertFalse(ExerciseGrader.grade(order, ExerciseAnswer.Order(listOf("tok-am", "tok-i", "tok-here"))).correct)
        assertFalse(ExerciseGrader.grade(order, ExerciseAnswer.Order(listOf("tok-i", "tok-i", "tok-here"))).valid)
    }
    @Test fun repeatedIdenticalWordsAreInterchangeableInOrdering() {
        val exercise = order.copy(tokens = listOf(ChoiceOption("word-one", "very"), ChoiceOption("word-two", "very"), ChoiceOption("word-three", "good")),
            correctOrder = listOf("word-one", "word-two", "word-three"))
        assertTrue(ExerciseGrader.grade(exercise, ExerciseAnswer.Order(listOf("word-two", "word-one", "word-three"))).correct)
    }
    @Test fun matchingNeedsABijectionAndDoesNotAwardPartialCredit() {
        assertTrue(ExerciseGrader.grade(matches, ExerciseAnswer.Matches(mapOf("pair-i" to "pair-i", "pair-you" to "pair-you"))).correct)
        val swapped = ExerciseGrader.grade(matches, ExerciseAnswer.Matches(mapOf("pair-i" to "pair-you", "pair-you" to "pair-i")))
        assertTrue(swapped.valid); assertFalse(swapped.correct)
        assertFalse(ExerciseGrader.grade(matches, ExerciseAnswer.Matches(mapOf("pair-i" to "pair-i", "pair-you" to "pair-i"))).valid)
    }
    @Test fun feedbackProvidesExpectedAnswerAndRomanUrduExplanation() {
        val grade = ExerciseGrader.grade(text, ExerciseAnswer.Text("are"))
        assertTrue(grade.valid); assertFalse(grade.correct)
        assertEquals("am", grade.expectedAnswer)
        assertEquals(text.explanationRomanUrdu, grade.explanationRomanUrdu)
    }
}
