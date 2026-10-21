package com.lingomate.ai

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.lingomate.ai.core.database.LingoMateDatabase
import com.lingomate.ai.data.LearningRepository
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
class EnglishB1RepositoryTest {
    @get:Rule val temporary = TemporaryFolder()
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val files = listOf("english-a0-starter.json", "english-a1-beginner.json",
        "english-a2-elementary.json", "english-b1-intermediate.json")
    private val sources by lazy { files.map { file ->
        context.assets.open("content/$file").bufferedReader().use { it.readText() }
    } }
    private lateinit var database: LingoMateDatabase
    private lateinit var repository: LearningRepository

    @Before fun open() {
        database = Room.databaseBuilder(context, LingoMateDatabase::class.java,
            temporary.root.resolve("b1.db").absolutePath).build()
        repository = LearningRepository(database, { sources.reversed() })
    }
    @After fun close() { database.close() }

    @Test fun reversedPackImportStillOrdersAllSeventyFourLessonsFromA0ThroughB1() = runBlocking {
        repository.installBundledContent()
        val catalog = repository.catalog.first()
        val expected = sources.flatMap { ContentCodec.parsePack(it).units.flatMap { unit -> unit.lessons }.map { lesson -> lesson.id } }
        assertEquals(expected, catalog.lessons.map { it.id })
        assertEquals(List(11) { "A0" } + List(18) { "A1" } + List(21) { "A2" } + List(24) { "B1" }, catalog.lessons.map { it.level })
        assertEquals("en-a0-alphabet", catalog.lessons.first().id)
        assertEquals("en-b1-tense-accuracy", catalog.lessons[50].id)
        assertTrue(catalog.progress.isEmpty())
    }

    @Test fun addingB1PreservesEarlierProgressActiveReadingExposureAndSavedA2Writing() = runBlocking {
        sources.take(3).forEach { repository.importPack(it) }
        val oldA0 = repository.beginOrResume("en-a0-greetings")
        submitCorrect(oldA0.session.id, oldA0.content.exercises.first())
        val oldA1 = repository.beginOrResume("en-a1-routines")
        oldA1.content.exercises.forEach { submitCorrect(oldA1.session.id, it) }
        val progress = requireNotNull(database.learningDao().progress(oldA1.session.lessonId))
        val oldWriting = repository.beginOrResume("en-a2-paragraph-writing")
        val writing = oldWriting.content.exercises.first() as Exercise.ParagraphWriting
        val ownText = ExerciseAnswer.ParagraphText("My own A2 account.\nA saved detail: café 😊", listOf(writing.checklist.first().id))
        repository.submit(oldWriting.session.id, writing.id, ownText)
        val oldReading = repository.beginOrResume("en-a2-perfect-experiences")
        oldReading.content.exercises.dropLast(1).forEach { submitCorrect(oldReading.session.id, it) }
        val listening = oldReading.content.exercises.last() as Exercise.ListeningChoice
        repository.prepareListening(oldReading.session.id, listening.id, ListeningMode.READING_ALTERNATIVE)
        val before = listOf(oldA0, oldA1, oldWriting, oldReading).map { repository.run(it.session.id) }
        val saved = requireNotNull(repository.latestParagraph(oldWriting.session.lessonId, writing.id))
        repository.installBundledContent()
        database.close()
        open()
        before.forEach { assertEquals(it, repository.run(it.session.id)) }
        assertEquals(progress, database.learningDao().progress(oldA1.session.lessonId))
        assertEquals(saved, repository.latestParagraph(oldWriting.session.lessonId, writing.id))
        assertEquals(ownText, repository.latestParagraph(oldWriting.session.lessonId, writing.id)?.answer)
        assertEquals(24, database.learningDao().packLessons("english-b1-intermediate").size)
        assertNull(database.learningDao().progress("en-b1-tense-accuracy"))
    }

    @Test fun everyB1WritingLessonKeepsOriginalWritingAndSixQuestionScoringAcrossReopen() = runBlocking {
        repository.installBundledContent()
        val lessons = ContentCodec.parsePack(sources.last()).units.flatMap { it.lessons }
            .filter { lesson -> lesson.exercises.any { it is Exercise.ParagraphWriting } }
        val originalSessions = mutableMapOf<String, String>()
        val originals = mutableMapOf<String, ExerciseAnswer.ParagraphText>()
        for (lesson in lessons) {
            val run = repository.beginOrResume(lesson.id)
            originalSessions[lesson.id] = run.session.id
            lesson.exercises.forEach { exercise ->
                if (exercise is Exercise.ParagraphWriting) {
                    val answer = ExerciseAnswer.ParagraphText("My own draft for ${lesson.title}. Mistakes is allowed in self-practice.",
                        listOf(exercise.checklist.first().id))
                    originals[lesson.id] = answer
                    repository.submit(run.session.id, exercise.id, answer)
                    repository.submit(run.session.id, exercise.id, ExerciseAnswer.ParagraphText(exercise.modelParagraph, emptyList()))
                } else submitCorrect(run.session.id, exercise)
            }
            val completed = repository.run(run.session.id)
            assertTrue(completed.complete)
            assertEquals(7, completed.attempts.size)
            assertEquals(6, completed.scoredQuestionCount)
            assertEquals(6, completed.correctCount)
            assertEquals(1, completed.answeredIn(ListeningMode.READING_ALTERNATIVE))
            assertEquals(0, completed.answeredIn(ListeningMode.AUDIO))
            val progress = requireNotNull(database.learningDao().progress(lesson.id))
            assertEquals(6, progress.bestCorrect)
            assertEquals(6, progress.questionCount)
            assertEquals(1, progress.completedRuns)
        }
        database.close()
        open()
        for (lesson in lessons) {
            val writing = lesson.exercises.filterIsInstance<Exercise.ParagraphWriting>().single()
            val saved = requireNotNull(repository.latestParagraph(lesson.id, writing.id))
            assertEquals(writing, saved.exercise)
            assertEquals(originals.getValue(lesson.id), saved.answer)
            val newRun = repository.beginOrResume(lesson.id)
            assertNotEquals(originalSessions.getValue(lesson.id), newRun.session.id)
            assertTrue(newRun.attempts.isEmpty())
            assertEquals(saved, repository.latestParagraph(lesson.id, writing.id))
            assertEquals(1, database.learningDao().progress(lesson.id)?.completedRuns)
        }
        assertEquals(4, repository.catalog.first().progress.size)
        assertNull(database.learningDao().progress("en-a2-paragraph-writing"))
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
