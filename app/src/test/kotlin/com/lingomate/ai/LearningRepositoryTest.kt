package com.lingomate.ai

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.lingomate.ai.core.database.LingoMateDatabase
import com.lingomate.ai.data.LearningRepository
import com.lingomate.learning.content.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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
class LearningRepositoryTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val source get() = context.assets.open("content/english-a0-starter.json").bufferedReader().use { it.readText() }
    private val pack get() = ContentCodec.parsePack(source)
    private lateinit var database: LingoMateDatabase
    private lateinit var repository: LearningRepository
    private var now = 1000L

    @Before fun open() {
        database = Room.inMemoryDatabaseBuilder(context, LingoMateDatabase::class.java).build()
        repository = LearningRepository(database, { listOf(source) }, clock = { now++ })
    }
    @After fun close() { database.close() }

    @Test fun bundledInstallIsIdempotentAndNeverDowngradesContent() = runBlocking {
        repository.installBundledContent()
        val v2 = pack.copy(version = 2, title = "Updated pack")
        repository.importPack(ContentCodec.json.encodeToString(v2))
        repository.installBundledContent()
        assertEquals(2, database.learningDao().pack(pack.id)?.version)
        assertEquals(3, database.learningDao().packLessons(pack.id).size)
    }

    @Test fun allBundlesValidateBeforeAnyPackIsWritten() = runBlocking {
        val invalid = ContentCodec.json.encodeToString(pack.copy(version = 0))
        val badRepository = LearningRepository(database, { listOf(source, invalid) })
        try { badRepository.installBundledContent(); fail("Must reject invalid pack") } catch (_: IllegalArgumentException) { }
        assertNull(database.learningDao().pack(pack.id))
    }

    @Test fun existingLessonIdCannotBeStolenByAnotherPack() = runBlocking {
        repository.installBundledContent()
        val collision = pack.copy(id = "another-pack")
        try { repository.importPack(ContentCodec.json.encodeToString(collision)); fail("Expected conflict") } catch (_: IllegalArgumentException) { }
        assertNull(database.learningDao().pack("another-pack"))
        assertEquals(pack.id, database.learningDao().lesson("en-a0-greetings")?.packId)
    }

    @Test fun packUpdateCannotStrandAnExistingLesson() = runBlocking {
        repository.installBundledContent()
        val partial = pack.copy(version = 2, units = pack.units.map { it.copy(lessons = it.lessons.drop(1)) })
        try { repository.importPack(ContentCodec.json.encodeToString(partial)); fail("Expected refusal") } catch (_: IllegalArgumentException) { }
        assertEquals(1, database.learningDao().pack(pack.id)?.version)
        assertEquals(3, database.learningDao().packLessons(pack.id).size)
    }

    @Test fun activeSessionUsesAnImmutableContentSnapshotThroughUpdates() = runBlocking {
        repository.installBundledContent()
        val first = repository.beginOrResume("en-a0-greetings")
        val updated = pack.copy(version = 2, units = pack.units.map { unit -> unit.copy(lessons = unit.lessons.map { it.copy(title = "Updated ${it.title}") }) })
        repository.importPack(ContentCodec.json.encodeToString(updated))
        assertEquals(first.content, repository.beginOrResume("en-a0-greetings").content)
        assertTrue(repository.lesson("en-a0-greetings").title.startsWith("Updated"))
    }

    @Test fun answersAndSessionResumeAfterRepositoryRecreation() = runBlocking {
        repository.installBundledContent()
        val run = repository.beginOrResume("en-a0-greetings")
        val first = run.content.exercises.first() as Exercise.SingleChoice
        repository.submit(run.session.id, first.id, ExerciseAnswer.Selection(listOf(first.correctOptionId)))
        val reopened = LearningRepository(database, { listOf(source) })
        val restored = reopened.beginOrResume("en-a0-greetings")
        assertEquals(run.session.id, restored.session.id)
        assertEquals(1, restored.attempts.size)
        assertTrue(restored.attempts.first().correct)
    }

    @Test fun concurrentStartsCreateOnlyOneActiveSession() = runBlocking {
        repository.installBundledContent()
        val sessions = (1..8).map { async { repository.beginOrResume("en-a0-greetings").session.id } }.awaitAll()
        assertEquals(1, sessions.distinct().size)
    }

    @Test fun incompleteOrOutOfSequenceAnswersCannotCreateAttemptsOrProgress() = runBlocking {
        repository.installBundledContent()
        val run = repository.beginOrResume("en-a0-greetings")
        try { repository.submit(run.session.id, run.content.exercises.first().id, ExerciseAnswer.Selection(emptyList())); fail("Invalid answer") } catch (_: IllegalArgumentException) { }
        try { repository.submit(run.session.id, run.content.exercises[1].id, ExerciseAnswer.Text("is")); fail("Out of sequence") } catch (_: IllegalArgumentException) { }
        assertTrue(repository.run(run.session.id).attempts.isEmpty())
        assertNull(database.learningDao().progress(run.session.lessonId))
    }

    @Test fun completionAndRepeatedSubmissionAreAtomicAndIdempotent() = runBlocking {
        repository.installBundledContent()
        val initial = repository.beginOrResume("en-a0-greetings")
        for (exercise in initial.content.exercises) repository.submit(initial.session.id, exercise.id, correctAnswer(exercise))
        repeat(3) { repository.submit(initial.session.id, initial.content.exercises.last().id, correctAnswer(initial.content.exercises.last())) }
        val completed = repository.run(initial.session.id)
        val progress = requireNotNull(database.learningDao().progress(initial.session.lessonId))
        assertTrue(completed.complete)
        assertEquals(5, completed.attempts.size)
        assertEquals(5, progress.bestCorrect)
        assertEquals(1, progress.completedRuns)
        assertNull(completed.session.activeLessonKey)
        val newRun = repository.beginOrResume(initial.session.lessonId)
        assertNotEquals(initial.session.id, newRun.session.id)
        assertFalse(newRun.complete)
    }

    @Test fun duplicateAnswerCannotImproveItsPersistedScore() = runBlocking {
        repository.installBundledContent()
        val run = repository.beginOrResume("en-a0-greetings")
        val first = run.content.exercises.first() as Exercise.SingleChoice
        val wrong = first.options.first { it.id != first.correctOptionId }.id
        repository.submit(run.session.id, first.id, ExerciseAnswer.Selection(listOf(wrong)))
        val retry = repository.submit(run.session.id, first.id, correctAnswer(first))
        assertEquals(1, retry.attempts.size)
        assertFalse(retry.attempts.first().correct)
    }

    @Test fun secondRunKeepsTheBestScoreAndCountsEachCompletedRunOnce() = runBlocking {
        repository.installBundledContent()
        val first = repository.beginOrResume("en-a0-greetings")
        first.content.exercises.forEach { repository.submit(first.session.id, it.id, correctAnswer(it)) }
        val second = repository.beginOrResume(first.session.lessonId)
        second.content.exercises.forEach { exercise ->
            val answer = if (exercise is Exercise.FillBlank) ExerciseAnswer.Text("wrong") else correctAnswer(exercise)
            repository.submit(second.session.id, exercise.id, answer)
        }
        val progress = requireNotNull(database.learningDao().progress(first.session.lessonId))
        assertEquals(5, progress.bestCorrect)
        assertEquals(2, progress.completedRuns)
    }

    private fun correctAnswer(exercise: Exercise): ExerciseAnswer = when (exercise) {
        is Exercise.SingleChoice -> ExerciseAnswer.Selection(listOf(exercise.correctOptionId))
        is Exercise.MultiChoice -> ExerciseAnswer.Selection(exercise.correctOptionIds)
        is Exercise.FillBlank -> ExerciseAnswer.Text(exercise.acceptedAnswers.first())
        is Exercise.SentenceOrder -> ExerciseAnswer.Order(exercise.correctOrder)
        is Exercise.MatchPairs -> ExerciseAnswer.Matches(exercise.pairs.associate { it.id to it.id })
    }
}
