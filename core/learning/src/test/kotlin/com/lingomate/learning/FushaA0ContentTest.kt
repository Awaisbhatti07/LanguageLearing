package com.lingomate.learning

import com.lingomate.learning.content.*
import java.text.Normalizer
import org.junit.Assert.*
import org.junit.Test

class FushaA0ContentTest {
    private fun pack(file: String = "fusha-a0-foundation.json") = ContentCodec.parsePack(
        requireNotNull(javaClass.getResource("/$file")).readText(),
    )
    private fun lessons() = pack().units.flatMap { it.lessons }
    private fun exercise(id: String) = lessons().flatMap { it.exercises }.single { it.id == id }

    @Test fun foundationHasTwelveCompleteLessonsAndSeventyTwoActivitiesPendingReview() {
        val pack = pack()
        assertEquals("fusha-a0-foundation", pack.id)
        assertEquals(1, pack.version)
        assertEquals(LearningTrack.FUSHA, pack.track)
        assertEquals(CefrLevel.A0, pack.level)
        assertEquals(ReviewStatus.PENDING_REVIEW, pack.provenance.reviewStatus)
        assertNull(pack.provenance.reviewer)
        assertEquals(3, pack.units.size)
        assertEquals(12, lessons().size)
        assertEquals(72, lessons().sumOf { it.exercises.size })
        lessons().forEach { lesson ->
            assertTrue(lesson.objectives.size >= 2)
            assertTrue(lesson.rulesRomanUrdu.size >= 4)
            assertTrue(lesson.examples.size >= 3)
            assertTrue(lesson.vocabulary.size >= 3)
            assertNotNull(lesson.reading)
            assertTrue(lesson.speakingPrompts.size >= 2)
            assertEquals(6, lesson.exercises.map { it::class }.toSet().size)
            assertTrue(lesson.examples.all { it.speech?.languageTag == "ar" })
            assertEquals("ar", lesson.exercises.filterIsInstance<Exercise.ListeningChoice>().single().speech.languageTag)
        }
    }

    @Test fun coverageIncludesEveryRequestedA0TopicAndAllTwentyEightBasicLetters() {
        val required = setOf("alphabet", "letter-shapes", "letter-joining", "pronunciation-basics", "fatha", "kasra",
            "damma", "sukoon", "shaddah", "tanween", "madd", "basic-reading", "greetings", "basic-nouns", "basic-verbs", "basic-sentences")
        val tags = lessons().flatMap { it.reviewTags }.toSet()
        assertTrue("Missing topics: ${required - tags}", tags.containsAll(required))
        val alphabet = lessons().single { it.id == "ar-a0-alphabet" }.rulesRomanUrdu.take(4).joinToString(" ")
        val expected = "ابتثجحخدذرزسشصضطظعغفقكلمنهوي".toSet()
        assertEquals(expected, alphabet.filter { it in '\u0627'..'\u064a' }.toSet())
    }

    @Test fun shortVowelsAndShaddahKeysDistinguishDifferentArabicForms() {
        val cases = listOf(
            Triple("ar-a0-short-vowels-single-choice", "بِ", "بَ"),
            Triple("ar-a0-shaddah-single-choice", "دَرَّسَ", "دَرَسَ"),
            Triple("ar-a0-tanween-single-choice", "كِتَابٍ", "كِتَابٌ"),
            Triple("ar-a0-simple-sentences-single-choice", "أَنَا طَالِبَةٌ.", "أَنَا طَالِبٌ."),
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

    @Test fun markedAnswersPreserveHarakatAndAcceptCanonicalUnicodeEquivalence() {
        val short = exercise("ar-a0-short-vowels-fill-blank")
        assertTrue(ExerciseGrader.grade(short, ExerciseAnswer.Text("بَ")).correct)
        assertFalse(ExerciseGrader.grade(short, ExerciseAnswer.Text("بِ")).correct)
        assertFalse(ExerciseGrader.grade(short, ExerciseAnswer.Text("ب")).correct)
        val pronoun = exercise("ar-a0-simple-sentences-fill-blank")
        assertTrue(ExerciseGrader.grade(pronoun, ExerciseAnswer.Text(Normalizer.normalize("أَنَا", Normalizer.Form.NFD))).correct)
        assertFalse(ExerciseGrader.grade(pronoun, ExerciseAnswer.Text("انا")).correct)
        val original = pack()
        assertEquals(original, ContentCodec.parsePack(ContentCodec.json.encodeToString(original)))
    }

    @Test fun ArabicDisplayTextUsesLogicalCharactersWithoutPresentationFormsOrBidiOverrides() {
        val source = ContentCodec.json.encodeToString(pack())
        assertFalse(source.any { it in '\ufb50'..'\ufdff' || it in '\ufe70'..'\ufeff' || it in '\u202a'..'\u202e' })
        for (mark in "ًٌٍَُِّْ") assertTrue("Missing Arabic mark $mark", mark in source)
    }

    @Test fun newIdentifiersCannotCollideWithAnyEnglishLessonOrActivity() {
        fun ids(pack: ContentPack) = pack.units.flatMap { it.lessons }.flatMap {
            listOf(it.id) + it.exercises.map { exercise -> exercise.id } + it.vocabulary.map { word -> word.id }
        }.toSet()
        val arabic = ids(pack())
        for (file in listOf("english-a0-starter.json", "english-a1-beginner.json", "english-a2-elementary.json", "english-b1-intermediate.json")) {
            assertTrue(file, arabic.intersect(ids(pack(file))).isEmpty())
        }
        assertTrue(lessons().all { it.id.startsWith("ar-a0-") })
    }
}
