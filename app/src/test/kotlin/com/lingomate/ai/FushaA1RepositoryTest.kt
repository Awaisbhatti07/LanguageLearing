package com.lingomate.ai

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.lingomate.ai.core.database.LingoMateDatabase
import com.lingomate.ai.data.LearningRepository
import com.lingomate.learning.LearningTrack
import com.lingomate.learning.content.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class FushaA1RepositoryTest {
    @get:Rule val temporary = TemporaryFolder()
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private fun source(file: String) = context.assets.open("content/$file").bufferedReader().use { it.readText() }
    private val previous by lazy { listOf("english-a0-starter.json", "english-a1-beginner.json",
        "english-a2-elementary.json", "english-b1-intermediate.json", "fusha-a0-foundation.json").map(::source) }
    private val a1 by lazy { source("fusha-a1-beginner.json") }
    private lateinit var database: LingoMateDatabase
    private lateinit var repository: LearningRepository

    @Before fun open() {
        database = Room.databaseBuilder(context, LingoMateDatabase::class.java,
            temporary.root.resolve("fusha-a1.db").absolutePath).build()
        // Import A1 before A0; level ordering must come from the catalog, not import order.
        repository = LearningRepository(database, { listOf(a1) + previous.reversed() })
    }
    @After fun close() { database.close() }

    @Test fun reverseImportOrdersTwelveA0LessonsBeforeEighteenA1LessonsWithoutChangingEnglish() = runBlocking {
        repository.installBundledContent()
        repository.installBundledContent()
        val catalog = repository.catalog.first()
        val fusha = catalog.lessons.filter { it.track == "FUSHA" }
        val expected = listOf(previous.last(), a1).flatMap { ContentCodec.parsePack(it).units.flatMap { unit -> unit.lessons }.map { it.id } }
        assertEquals(expected, fusha.map { it.id })
        assertEquals(List(12) { "A0" } + List(18) { "A1" }, fusha.map { it.level })
        assertEquals(74, catalog.lessons.count { it.track == "ENGLISH" })
        assertEquals(104, catalog.lessons.size)
        assertTrue(catalog.progress.isEmpty())
    }

    @Test fun addingA1PreservesEnglishAndA0CompletedActiveReadingAndWritingAcrossReopen() = runBlocking {
        previous.forEach { repository.importPack(it) }
        val englishActive = repository.beginOrResume("en-a0-greetings")
        submitCorrect(englishActive.session.id, englishActive.content.exercises.first())
        val englishDone = repository.beginOrResume("en-a1-routines")
        englishDone.content.exercises.forEach { submitCorrect(englishDone.session.id, it) }
        val a0Done = repository.beginOrResume("ar-a0-short-vowels")
        a0Done.content.exercises.forEach { submitCorrect(a0Done.session.id, it) }
        val a0Reading = repository.beginOrResume("ar-a0-joining")
        a0Reading.content.exercises.dropLast(1).forEach { submitCorrect(a0Reading.session.id, it) }
        repository.prepareListening(a0Reading.session.id, a0Reading.content.exercises.last().id, ListeningMode.READING_ALTERNATIVE)
        val writingRun = repository.beginOrResume("en-a2-paragraph-writing")
        val writing = writingRun.content.exercises.first() as Exercise.ParagraphWriting
        val ownWriting = ExerciseAnswer.ParagraphText("My own saved English paragraph before Fusha A1.", emptyList())
        repository.submit(writingRun.session.id, writing.id, ownWriting)
        val before = listOf(englishActive, englishDone, a0Done, a0Reading, writingRun).map { repository.run(it.session.id) }
        val progress = listOf(englishDone, a0Done).map { database.learningDao().progress(it.session.lessonId) }
        val saved = repository.latestParagraph(writingRun.session.lessonId, writing.id)
        repository.installBundledContent()
        database.close(); open()
        before.forEach { assertEquals(it, repository.run(it.session.id)) }
        assertEquals(progress, listOf(englishDone, a0Done).map { database.learningDao().progress(it.session.lessonId) })
        assertEquals(saved, repository.latestParagraph(writingRun.session.lessonId, writing.id))
        assertEquals(ownWriting, repository.latestParagraph(writingRun.session.lessonId, writing.id)?.answer)
        assertEquals(1, repository.catalog.first().completed(LearningTrack.ENGLISH))
        assertEquals(1, repository.catalog.first().completed(LearningTrack.FUSHA))
        assertNull(database.learningDao().progress("ar-a1-personal-pronouns"))
    }

    @Test fun allEighteenA1LessonsKeepSixQuestionScoresAndReadingOnlyCreditAcrossReopen() = runBlocking {
        repository.installBundledContent()
        val lessons = ContentCodec.parsePack(a1).units.flatMap { it.lessons }
        val completed = lessons.map { lesson ->
            val run = repository.beginOrResume(lesson.id)
            lesson.exercises.forEach { submitCorrect(run.session.id, it) }
            val finished = repository.run(run.session.id)
            assertTrue(finished.complete)
            assertEquals(6, finished.attempts.size)
            assertEquals(6, finished.scoredQuestionCount)
            assertEquals(6, finished.correctCount)
            assertEquals(0, finished.answeredIn(ListeningMode.AUDIO))
            assertEquals(1, finished.answeredIn(ListeningMode.READING_ALTERNATIVE))
            finished
        }
        database.close(); open()
        completed.forEach { assertEquals(it, repository.run(it.session.id)) }
        assertEquals(18, repository.catalog.first().completed(LearningTrack.FUSHA))
        assertEquals(0, repository.catalog.first().completed(LearningTrack.ENGLISH))
        assertTrue(repository.catalog.first().progress.all { it.lessonId.startsWith("ar-a1-") && it.completedRuns == 1 })
        assertNull(database.learningDao().progress("ar-a0-alphabet"))
        val next = repository.beginOrResume("ar-a1-personal-pronouns")
        assertNotEquals(completed.first().session.id, next.session.id)
        assertTrue(next.attempts.isEmpty())
        assertEquals(1, database.learningDao().progress(next.session.lessonId)?.completedRuns)
    }

    private suspend fun submitCorrect(session: String, exercise: Exercise) {
        val answer = when (exercise) {
            is Exercise.ParagraphWriting -> ExerciseAnswer.ParagraphText(exercise.modelParagraph, emptyList())
            is Exercise.SingleChoice -> ExerciseAnswer.Selection(listOf(exercise.correctOptionId))
            is Exercise.MultiChoice -> ExerciseAnswer.Selection(exercise.correctOptionIds)
            is Exercise.FillBlank -> ExerciseAnswer.Text(exercise.acceptedAnswers.first())
            is Exercise.SentenceOrder -> ExerciseAnswer.Order(exercise.correctOrder)
            is Exercise.MatchPairs -> ExerciseAnswer.Matches(exercise.pairs.associate { it.id to it.id })
            is Exercise.ListeningChoice -> {
                repository.prepareListening(session, exercise.id, ListeningMode.READING_ALTERNATIVE)
                ExerciseAnswer.ListeningSelection(exercise.correctOptionId, ListeningMode.READING_ALTERNATIVE)
            }
        }
        repository.submit(session, exercise.id, answer)
    }
}
