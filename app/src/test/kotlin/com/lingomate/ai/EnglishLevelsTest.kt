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
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class EnglishLevelsTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private fun source(file: String) = context.assets.open("content/$file").bufferedReader().use { it.readText() }
    private val a0 get() = source("english-a0-starter.json")
    private val a1 get() = source("english-a1-beginner.json")
    private lateinit var database: LingoMateDatabase
    private lateinit var repository: LearningRepository

    @Before fun open() {
        database = Room.inMemoryDatabaseBuilder(context, LingoMateDatabase::class.java).build()
        // Reverse import order deliberately; progression must come from the catalog query.
        repository = LearningRepository(database, { listOf(a1, a0) })
    }
    @After fun close() { database.close() }

    @Test fun allA0LessonsPrecedeA1EvenThoughBothPacksRestartTheirPositionsAtZero() = runBlocking {
        repository.installBundledContent()
        val english = repository.catalog.first().lessons.filter { it.track == LearningTrack.ENGLISH.name }
        val expected = listOf(a0, a1).flatMap { source -> ContentCodec.parsePack(source).units.flatMap { it.lessons }.map { it.id } }
        assertEquals(29, english.size)
        assertEquals(List(11) { "A0" } + List(18) { "A1" }, english.map { it.level })
        assertEquals(expected, english.map { it.id })
        assertEquals("en-a0-alphabet", english.first().id)
        assertEquals("en-a1-routines", english[11].id)
    }

    @Test fun addingA1KeepsActiveA0ReadingExposureAnswersAndCompletedProgress() = runBlocking {
        repository.importPack(a0)
        val active = repository.beginOrResume("en-a0-greetings")
        active.content.exercises.dropLast(1).forEach { submitCorrect(active.session.id, it) }
        val listening = active.content.exercises.last() as Exercise.ListeningChoice
        val before = repository.prepareListening(active.session.id, listening.id, ListeningMode.READING_ALTERNATIVE)
        val finished = repository.beginOrResume("en-a0-pronouns-be")
        finished.content.exercises.forEach { submitCorrect(finished.session.id, it) }
        val progress = requireNotNull(database.learningDao().progress(finished.session.lessonId))
        repository.installBundledContent()
        val after = repository.beginOrResume(active.session.lessonId)
        assertEquals(before, after)
        assertEquals(5, after.attempts.size)
        assertEquals(ListeningMode.READING_ALTERNATIVE, after.listeningModes[listening.id])
        assertEquals(progress, database.learningDao().progress(finished.session.lessonId))
        assertEquals(18, database.learningDao().packLessons("english-a1-beginner").size)
        assertEquals(ContentCodec.parsePack(a0).version, database.learningDao().pack("english-a0-starter")?.version)
    }

    @Test fun anA1ReadingAlternativeDoesNotCompleteAnA0LessonOrClaimListeningCredit() = runBlocking {
        repository.installBundledContent()
        val run = repository.beginOrResume("en-a1-routines")
        run.content.exercises.forEach { submitCorrect(run.session.id, it, ListeningMode.READING_ALTERNATIVE) }
        val finished = repository.run(run.session.id)
        assertTrue(finished.complete)
        assertEquals(6, finished.correctCount)
        assertEquals(0, finished.answeredIn(ListeningMode.AUDIO))
        assertEquals(1, finished.answeredIn(ListeningMode.READING_ALTERNATIVE))
        val catalog = repository.catalog.first()
        assertEquals(1, catalog.completed(LearningTrack.ENGLISH))
        assertEquals(listOf("en-a1-routines"), catalog.progress.map { it.lessonId })
        assertNull(database.learningDao().progress("en-a0-alphabet"))
    }

    private suspend fun submitCorrect(session: String, exercise: Exercise, mode: ListeningMode = ListeningMode.AUDIO) {
        val answer = when (exercise) {
            is Exercise.ParagraphWriting -> ExerciseAnswer.ParagraphText(exercise.modelParagraph, emptyList())
            is Exercise.SingleChoice -> ExerciseAnswer.Selection(listOf(exercise.correctOptionId))
            is Exercise.MultiChoice -> ExerciseAnswer.Selection(exercise.correctOptionIds)
            is Exercise.FillBlank -> ExerciseAnswer.Text(exercise.acceptedAnswers.first())
            is Exercise.SentenceOrder -> ExerciseAnswer.Order(exercise.correctOrder)
            is Exercise.MatchPairs -> ExerciseAnswer.Matches(exercise.pairs.associate { it.id to it.id })
            is Exercise.ListeningChoice -> {
                repository.prepareListening(session, exercise.id, mode)
                ExerciseAnswer.ListeningSelection(exercise.correctOptionId, mode)
            }
        }
        repository.submit(session, exercise.id, answer)
    }
}
