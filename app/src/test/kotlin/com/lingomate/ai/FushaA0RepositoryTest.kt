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
class FushaA0RepositoryTest {
    @get:Rule val temporary = TemporaryFolder()
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private fun source(file: String) = context.assets.open("content/$file").bufferedReader().use { it.readText() }
    private val english by lazy { listOf("english-a0-starter.json", "english-a1-beginner.json",
        "english-a2-elementary.json", "english-b1-intermediate.json").map(::source) }
    private val arabic by lazy { source("fusha-a0-foundation.json") }
    private lateinit var database: LingoMateDatabase
    private lateinit var repository: LearningRepository

    @Before fun open() {
        database = Room.databaseBuilder(context, LingoMateDatabase::class.java,
            temporary.root.resolve("fusha.db").absolutePath).build()
        repository = LearningRepository(database, { listOf(arabic) + english.reversed() })
    }
    @After fun close() { database.close() }

    @Test fun addingFushaKeepsEnglishAnswersWritingReadingModesAndCompletedProgressAcrossReopen() = runBlocking {
        english.forEach { repository.importPack(it) }
        val active = repository.beginOrResume("en-a0-greetings")
        submitCorrect(active.session.id, active.content.exercises.first())
        val completed = repository.beginOrResume("en-a1-routines")
        completed.content.exercises.forEach { submitCorrect(completed.session.id, it) }
        val progress = database.learningDao().progress(completed.session.lessonId)
        val writingRun = repository.beginOrResume("en-a2-paragraph-writing")
        val writing = writingRun.content.exercises.first() as Exercise.ParagraphWriting
        repository.submit(writingRun.session.id, writing.id, ExerciseAnswer.ParagraphText(
            "My original English paragraph stays unchanged.", listOf(writing.checklist.first().id)))
        val reading = repository.beginOrResume("en-b1-tense-accuracy")
        reading.content.exercises.dropLast(1).forEach { submitCorrect(reading.session.id, it) }
        repository.prepareListening(reading.session.id, reading.content.exercises.last().id, ListeningMode.READING_ALTERNATIVE)
        val before = listOf(active, completed, writingRun, reading).map { repository.run(it.session.id) }
        val savedWriting = repository.latestParagraph(writingRun.session.lessonId, writing.id)
        repository.installBundledContent()
        database.close(); open()
        before.forEach { assertEquals(it, repository.run(it.session.id)) }
        assertEquals(progress, database.learningDao().progress(completed.session.lessonId))
        assertEquals(savedWriting, repository.latestParagraph(writingRun.session.lessonId, writing.id))
        val catalog = repository.catalog.first()
        assertEquals(74, catalog.lessons.count { it.track == "ENGLISH" })
        assertEquals(12, catalog.lessons.count { it.track == "FUSHA" })
        assertEquals(1, catalog.completed(LearningTrack.ENGLISH))
        assertEquals(0, catalog.completed(LearningTrack.FUSHA))
        val expected = ContentCodec.parsePack(arabic).units.flatMap { it.lessons }.map { it.id }
        assertEquals(expected, catalog.lessons.filter { it.track == "FUSHA" }.map { it.id })
    }

    @Test fun allTwelveFushaLessonsCompleteSeparatelyUsingDurableReadingAlternatives() = runBlocking {
        repository.installBundledContent()
        val lessons = ContentCodec.parsePack(arabic).units.flatMap { it.lessons }
        val sessions = lessons.map { lesson ->
            val run = repository.beginOrResume(lesson.id)
            lesson.exercises.forEach { submitCorrect(run.session.id, it) }
            val finished = repository.run(run.session.id)
            assertTrue(finished.complete)
            assertEquals(6, finished.correctCount)
            assertEquals(6, finished.scoredQuestionCount)
            assertEquals(0, finished.answeredIn(ListeningMode.AUDIO))
            assertEquals(1, finished.answeredIn(ListeningMode.READING_ALTERNATIVE))
            finished
        }
        database.close(); open()
        sessions.forEach { assertEquals(it, repository.run(it.session.id)) }
        val catalog = repository.catalog.first()
        assertEquals(12, catalog.completed(LearningTrack.FUSHA))
        assertEquals(0, catalog.completed(LearningTrack.ENGLISH))
        assertTrue(catalog.progress.all { it.lessonId.startsWith("ar-a0-") && it.completedRuns == 1 })
    }

    @Test fun wrongHarakatStayWrongAndCannotBeReplacedByASecondSubmission() = runBlocking {
        repository.installBundledContent()
        val run = repository.beginOrResume("ar-a0-short-vowels")
        submitCorrect(run.session.id, run.content.exercises.first())
        val fill = run.content.exercises[1] as Exercise.FillBlank
        repository.submit(run.session.id, fill.id, ExerciseAnswer.Text("بِ"))
        repository.submit(run.session.id, fill.id, ExerciseAnswer.Text("بَ"))
        database.close(); open()
        val resumed = repository.beginOrResume(run.session.lessonId)
        assertEquals(run.session.id, resumed.session.id)
        assertFalse(resumed.attempts.last().correct)
        val stored = ContentCodec.json.decodeFromString<ExerciseAnswer>(resumed.attempts.last().answerPayload)
        assertEquals(ExerciseAnswer.Text("بِ"), stored)
        run.content.exercises.drop(2).forEach { submitCorrect(run.session.id, it) }
        assertEquals(5, repository.run(run.session.id).correctCount)
        assertNull(database.learningDao().progress("en-a0-alphabet"))
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
