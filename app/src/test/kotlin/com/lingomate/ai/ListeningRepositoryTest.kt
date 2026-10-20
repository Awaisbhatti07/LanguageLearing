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
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ListeningRepositoryTest {
    @get:Rule val temporary = TemporaryFolder()
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val pack by lazy {
        val bundled = ContentCodec.parsePack(context.assets.open("content/english-a0-starter.json").bufferedReader().use { it.readText() })
        val lesson = bundled.units.flatMap { it.lessons }.first { it.id == "en-a0-greetings" }
        bundled.copy(units = listOf(bundled.units.first().copy(lessons = listOf(lesson.copy(
            exercises = listOf(lesson.exercises.filterIsInstance<Exercise.ListeningChoice>().single(), lesson.exercises.first()),
        )))))
    }
    private val lesson get() = pack.units.single().lessons.single()
    private val listening get() = lesson.exercises.first() as Exercise.ListeningChoice
    private lateinit var database: LingoMateDatabase
    private lateinit var repository: LearningRepository

    private fun open() {
        database = Room.databaseBuilder(context, LingoMateDatabase::class.java, temporary.root.resolve("listening.db").absolutePath).build()
        repository = LearningRepository(database, { listOf(ContentCodec.json.encodeToString(pack)) })
    }

    @Before fun prepare() = runBlocking { open(); repository.installBundledContent() }
    @After fun close() { database.close() }

    @Test fun answerRequiresSavedPlaybackOrTranscriptExposure() = runBlocking {
        val run = repository.beginOrResume(lesson.id)
        for (answer in listOf(ExerciseAnswer.ListeningSelection(listening.correctOptionId, ListeningMode.AUDIO),
            ExerciseAnswer.ListeningSelection(listening.correctOptionId, ListeningMode.READING_ALTERNATIVE),
            ExerciseAnswer.Selection(listOf(listening.correctOptionId)))) {
            try { repository.submit(run.session.id, listening.id, answer); fail("Listening must be prepared first") }
            catch (_: IllegalArgumentException) { }
        }
        assertTrue(repository.run(run.session.id).attempts.isEmpty())
        assertNull(database.learningDao().progress(lesson.id))
    }

    @Test fun transcriptExposureSurvivesDatabaseReopeningAndCannotBecomeAudioCredit() = runBlocking {
        val run = repository.beginOrResume(lesson.id)
        repository.prepareListening(run.session.id, listening.id, ListeningMode.READING_ALTERNATIVE)
        database.close()
        open()
        val resumed = repository.beginOrResume(lesson.id)
        assertEquals(run.session.id, resumed.session.id)
        assertEquals(ListeningMode.READING_ALTERNATIVE, resumed.listeningModes[listening.id])
        val replayed = repository.prepareListening(run.session.id, listening.id, ListeningMode.AUDIO)
        assertEquals(ListeningMode.READING_ALTERNATIVE, replayed.listeningModes[listening.id])
        try {
            repository.submit(run.session.id, listening.id, ExerciseAnswer.ListeningSelection(listening.correctOptionId, ListeningMode.AUDIO))
            fail("Reading exposure must remain reading")
        } catch (_: IllegalArgumentException) { }
        val answered = repository.submit(run.session.id, listening.id,
            ExerciseAnswer.ListeningSelection(listening.correctOptionId, ListeningMode.READING_ALTERNATIVE))
        assertEquals(1, answered.answeredIn(ListeningMode.READING_ALTERNATIVE))
        assertEquals(0, answered.answeredIn(ListeningMode.AUDIO))
        assertTrue(answered.attempts.single().correct)
    }

    @Test fun completedPlaybackAndItsAnswerSurviveRecreationWithoutChangingGeneralProgress() = runBlocking {
        val run = repository.beginOrResume(lesson.id)
        repository.prepareListening(run.session.id, listening.id, ListeningMode.AUDIO)
        database.close()
        open()
        val answered = repository.submit(run.session.id, listening.id,
            ExerciseAnswer.ListeningSelection(listening.correctOptionId, ListeningMode.AUDIO))
        assertEquals(1, answered.answeredIn(ListeningMode.AUDIO))
        assertNull(database.learningDao().progress(lesson.id))
        val last = lesson.exercises.last() as Exercise.SingleChoice
        val completed = repository.submit(run.session.id, last.id, ExerciseAnswer.Selection(listOf(last.correctOptionId)))
        assertTrue(completed.complete)
        assertEquals(2, database.learningDao().progress(lesson.id)?.bestCorrect)
        assertEquals(1, database.learningDao().progress(lesson.id)?.completedRuns)
    }

    @Test fun duplicateSubmissionCannotChangeASavedReadingAnswerOrItsScore() = runBlocking {
        val run = repository.beginOrResume(lesson.id)
        repository.prepareListening(run.session.id, listening.id, ListeningMode.READING_ALTERNATIVE)
        val wrong = listening.options.first { it.id != listening.correctOptionId }.id
        repository.submit(run.session.id, listening.id, ExerciseAnswer.ListeningSelection(wrong, ListeningMode.READING_ALTERNATIVE))
        val retry = repository.submit(run.session.id, listening.id, ExerciseAnswer.ListeningSelection(listening.correctOptionId, ListeningMode.AUDIO))
        assertFalse(retry.attempts.single().correct)
        assertEquals(ExerciseAnswer.ListeningSelection(wrong, ListeningMode.READING_ALTERNATIVE),
            ContentCodec.json.decodeFromString<ExerciseAnswer>(retry.attempts.single().answerPayload))
    }

    @Test fun futureNonListeningAndAlreadyAnsweredActivitiesCannotChangePresentation() = runBlocking {
        val run = repository.beginOrResume(lesson.id)
        val last = lesson.exercises.last()
        try { repository.prepareListening(run.session.id, last.id, ListeningMode.AUDIO); fail("Future activity") }
        catch (_: IllegalArgumentException) { }
        repository.prepareListening(run.session.id, listening.id, ListeningMode.AUDIO)
        repository.submit(run.session.id, listening.id, ExerciseAnswer.ListeningSelection(listening.correctOptionId, ListeningMode.AUDIO))
        for (exercise in listOf(listening, last)) {
            try { repository.prepareListening(run.session.id, exercise.id, ListeningMode.READING_ALTERNATIVE); fail("Not a current listening activity") }
            catch (_: IllegalArgumentException) { }
        }
        assertEquals(ListeningMode.AUDIO, repository.run(run.session.id).listeningModes[listening.id])
    }

    @Test fun concurrentModeRequestsNeverUndoReadingExposureOrCreateDuplicateRows() = runBlocking {
        val run = repository.beginOrResume(lesson.id)
        (0..7).map { index -> async {
            repository.prepareListening(run.session.id, listening.id,
                if (index == 3) ListeningMode.READING_ALTERNATIVE else ListeningMode.AUDIO)
        } }.awaitAll()
        assertEquals(ListeningMode.READING_ALTERNATIVE, repository.run(run.session.id).listeningModes[listening.id])
        assertEquals(1, database.learningDao().listeningPresentations(run.session.id).size)
        assertTrue(repository.run(run.session.id).attempts.isEmpty())
    }

    @Test fun contentUpdateKeepsActiveTranscriptAndExposureButANewRunUsesNewContent() = runBlocking {
        val run = repository.beginOrResume(lesson.id)
        repository.prepareListening(run.session.id, listening.id, ListeningMode.READING_ALTERNATIVE)
        val updatedListening = listening.copy(speech = SpeechReference("Hello. My name is Ali.", "en"),
            correctOptionId = listening.options.first { it.text == "Ali" }.id)
        val updated = pack.copy(version = pack.version + 1, units = listOf(pack.units.single().copy(
            lessons = listOf(lesson.copy(exercises = listOf(updatedListening, lesson.exercises.last()))))))
        repository.importPack(ContentCodec.json.encodeToString(updated))
        val resumed = repository.beginOrResume(lesson.id)
        assertEquals(run.content, resumed.content)
        assertEquals(ListeningMode.READING_ALTERNATIVE, resumed.listeningModes[listening.id])
        repository.submit(run.session.id, listening.id, ExerciseAnswer.ListeningSelection(listening.correctOptionId, ListeningMode.READING_ALTERNATIVE))
        val last = lesson.exercises.last() as Exercise.SingleChoice
        repository.submit(run.session.id, last.id, ExerciseAnswer.Selection(listOf(last.correctOptionId)))
        val next = repository.beginOrResume(lesson.id)
        assertEquals(updatedListening, next.content.exercises.first())
        assertTrue(next.listeningModes.isEmpty())
        assertNotEquals(run.session.id, next.session.id)
    }
}
