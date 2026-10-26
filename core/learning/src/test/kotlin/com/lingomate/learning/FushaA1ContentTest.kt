package com.lingomate.learning

import com.lingomate.learning.content.*
import java.text.Normalizer
import org.junit.Assert.*
import org.junit.Test

class FushaA1ContentTest {
    private fun pack(file: String = "fusha-a1-beginner.json") = ContentCodec.parsePack(
        requireNotNull(javaClass.getResource("/$file")).readText(),
    )
    private fun lessons() = pack().units.flatMap { it.lessons }
    private fun exercise(id: String) = lessons().flatMap { it.exercises }.single { it.id == id }

    @Test fun a1HasEighteenSubstantialLessonsWithSixActivitiesEachAndPendingReview() {
        val pack = pack()
        assertEquals("fusha-a1-beginner", pack.id)
        assertEquals(1, pack.version)
        assertEquals(LearningTrack.FUSHA, pack.track)
        assertEquals(CefrLevel.A1, pack.level)
        assertEquals(ReviewStatus.PENDING_REVIEW, pack.provenance.reviewStatus)
        assertNull(pack.provenance.reviewer)
        assertEquals(4, pack.units.size)
        assertEquals(18, lessons().size)
        assertEquals(108, lessons().sumOf { it.exercises.size })
        lessons().forEach { lesson ->
            assertTrue(lesson.objectives.size >= 2)
            assertTrue(lesson.rulesRomanUrdu.size >= 4)
            assertTrue(lesson.examples.size >= 4)
            assertTrue(lesson.vocabulary.size >= 4)
            assertNotNull(lesson.reading)
            assertTrue(lesson.speakingPrompts.size >= 2)
            assertEquals(6, lesson.exercises.map { it::class }.toSet().size)
            assertTrue(lesson.examples.all { it.speech?.languageTag == "ar" })
            assertEquals("ar", lesson.exercises.filterIsInstance<Exercise.ListeningChoice>().single().speech.languageTag)
        }
    }

    @Test fun coverageIncludesAllRequestedA1TopicsAndEverydayConversation() {
        val required = setOf("personal-pronouns", "demonstrative-pronouns", "masculine-feminine", "singular-plural",
            "nouns-verbs-particles", "nominal-sentences", "verbal-sentences", "basic-past", "basic-present",
            "questions", "negation", "prepositions", "daily-vocabulary", "guided-conversation")
        val tags = lessons().flatMap { it.reviewTags }.toSet()
        assertTrue("Missing topics: ${required - tags}", tags.containsAll(required))
    }

    @Test fun authoredKeysDistinguishPronounGenderPersonCaseAndSentenceMeaning() {
        val cases = listOf(
            Triple("ar-a1-personal-pronouns-single-choice", "أَنْتِ", "أَنْتَ"),
            Triple("ar-a1-demonstratives-single-choice", "هٰذِهِ مَدْرَسَةٌ.", "تِلْكَ مَدْرَسَةٌ."),
            Triple("ar-a1-gender-single-choice", "مَدْرَسَةٌ كَبِيرَةٌ", "مَدْرَسَةٌ كَبِيرٌ"),
            Triple("ar-a1-definite-phrases-single-choice", "الْقَلَمُ جَدِيدٌ", "الْقَلَمُ الْجَدِيدُ"),
            Triple("ar-a1-past-verbs-single-choice", "كَتَبْتُ", "كَتَبْتَ"),
            Triple("ar-a1-present-verbs-single-choice", "أَنْتِ تَكْتُبِينَ.", "أَنْتَ تَكْتُبُ."),
            Triple("ar-a1-prepositions-single-choice", "فِي الْبَيْتِ", "فِي الْبَيْتُ"),
            Triple("ar-a1-negation-single-choice", "أَنَا لَا أَكْتُبُ.", "أَنَا أَكْتُبُ."),
        )
        for ((id, correct, wrong) in cases) {
            val exercise = exercise(id) as Exercise.SingleChoice
            fun answer(text: String) = ExerciseAnswer.Selection(listOf(exercise.options.single {
                normalizeAnswer(it.text) == normalizeAnswer(text)
            }.id))
            assertTrue(id, ExerciseGrader.grade(exercise, answer(correct)).correct)
            assertFalse(id, ExerciseGrader.grade(exercise, answer(wrong)).correct)
        }
    }

    @Test fun markedTypingDoesNotCollapsePersonEndingsOrRemoveHarakat() {
        val past = exercise("ar-a1-past-verbs-fill-blank")
        assertTrue(ExerciseGrader.grade(past, ExerciseAnswer.Text("كَتَبْتُ")).correct)
        assertFalse(ExerciseGrader.grade(past, ExerciseAnswer.Text("كَتَبْتَ")).correct)
        assertFalse(ExerciseGrader.grade(past, ExerciseAnswer.Text("كتبت")).correct)
        val present = exercise("ar-a1-present-verbs-fill-blank")
        assertTrue(ExerciseGrader.grade(present, ExerciseAnswer.Text(Normalizer.normalize("أَكْتُبُ", Normalizer.Form.NFD))).correct)
        assertFalse(ExerciseGrader.grade(present, ExerciseAnswer.Text("اكتب")).correct)
    }

    @Test fun listeningKeysAnswerTheRequestedDetailInsteadOfADistractingDetail() {
        val cases = listOf(
            Triple("ar-a1-verbal-sentences-listening-choice", "Sara.", "Khalid."),
            Triple("ar-a1-negation-listening-choice", "The speaker.", "The woman."),
            Triple("ar-a1-prepositions-listening-choice", "In the bag.", "On the desk."),
            Triple("ar-a1-daily-routine-listening-choice", "In the evening.", "In the morning."),
            Triple("ar-a1-shopping-listening-choice", "A pen.", "A book."),
            Triple("ar-a1-directions-listening-choice", "To the library.", "To the station."),
            Triple("ar-a1-guided-conversation-listening-choice", "Studies Arabic.", "Works."),
        )
        for ((id, correct, wrong) in cases) {
            val exercise = exercise(id) as Exercise.ListeningChoice
            fun answer(text: String) = ExerciseAnswer.ListeningSelection(exercise.options.single { it.text == text }.id, ListeningMode.AUDIO)
            assertTrue(id, ExerciseGrader.grade(exercise, answer(correct)).correct)
            assertFalse(id, ExerciseGrader.grade(exercise, answer(wrong)).correct)
        }
    }

    @Test fun a1IdentifiersDoNotCollideWithAnyOfTheFiveExistingPacks() {
        fun ids(pack: ContentPack) = pack.units.flatMap { it.lessons }.flatMap {
            listOf(it.id) + it.exercises.map { exercise -> exercise.id } + it.vocabulary.map { word -> word.id }
        }.toSet()
        val a1 = ids(pack())
        for (file in listOf("english-a0-starter.json", "english-a1-beginner.json", "english-a2-elementary.json",
            "english-b1-intermediate.json", "fusha-a0-foundation.json")) {
            assertTrue(file, a1.intersect(ids(pack(file))).isEmpty())
        }
        assertTrue(lessons().all { it.id.startsWith("ar-a1-") })
    }

    @Test fun roundTripRetainsLogicalArabicMarksScriptsAndOriginalProvenance() {
        val original = pack()
        val encoded = ContentCodec.json.encodeToString(original)
        assertEquals(original, ContentCodec.parsePack(encoded))
        assertTrue(Normalizer.isNormalized(encoded, Normalizer.Form.NFC))
        assertFalse(encoded.any { it in '\ufb50'..'\ufdff' || it in '\ufe70'..'\ufeff' || it in '\u202a'..'\u202e' })
    }
}
