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
class EnglishA2RepositoryTest {
    @get:Rule val temporary = TemporaryFolder()
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private fun source(file: String) = context.assets.open("content/$file").bufferedReader().use { it.readText() }
    private val a0 by lazy { source("english-a0-starter.json") }
    private val a1 by lazy { source("english-a1-beginner.json") }
    private val a2 by lazy { source("english-a2-elementary.json") }
    private val writingLesson = "en-a2-paragraph-writing"
    private var now = 1_000L
    private lateinit var database: LingoMateDatabase
    private lateinit var repository: LearningRepository

    @Before fun open() {
        database = Room.databaseBuilder(context, LingoMateDatabase::class.java,
            temporary.root.resolve("a2.db").absolutePath).build()
        repository = LearningRepository(database, { listOf(a2, a1, a0) }, clock = { now })
    }
    @After fun close() { database.close() }

    @Test fun reverseImportStillOrdersAllFiftyLessonsByLevelThenLessonPosition() = runBlocking {
        repository.installBundledContent()
        val lessons = repository.catalog.first().lessons
        assertEquals(List(11) { "A0" } + List(18) { "A1" } + List(21) { "A2" }, lessons.map { it.level })
        val expected = listOf(a0, a1, a2).flatMap { ContentCodec.parsePack(it).units.flatMap { unit -> unit.lessons }.map { lesson -> lesson.id } }
        assertEquals(expected, lessons.map { it.id })
        assertEquals("en-a0-alphabet", lessons.first().id)
        assertEquals("en-a2-perfect-experiences", lessons[29].id)
    }

    @Test fun addingA2PreservesEarlierSnapshotsAnswersReadingExposureAndCompletion() = runBlocking {
        repository.importPack(a0)
        repository.importPack(a1)
        val active = listOf("en-a0-greetings", "en-a1-routines").map { id ->
            val run = repository.beginOrResume(id)
            run.content.exercises.dropLast(1).forEach { submitCorrect(run.session.id, it) }
            val listening = run.content.exercises.last() as Exercise.ListeningChoice
            repository.prepareListening(run.session.id, listening.id, ListeningMode.READING_ALTERNATIVE)
        }
        val completed = repository.beginOrResume("en-a1-do-does")
        completed.content.exercises.forEach { submitCorrect(completed.session.id, it) }
        val before = requireNotNull(database.learningDao().progress(completed.session.lessonId))
        repository.installBundledContent()
        active.forEach { assertEquals(it, repository.beginOrResume(it.session.lessonId)) }
        assertEquals(before, database.learningDao().progress(completed.session.lessonId))
        assertEquals(21, database.learningDao().packLessons("english-a2-elementary").size)
        assertNull(database.learningDao().progress(writingLesson))
    }

    @Test fun savedParagraphIsImmutableOnRetryAndDoesNotCompleteOrScoreTheLesson() = runBlocking {
        repository.installBundledContent()
        val run = repository.beginOrResume(writingLesson)
        val writing = run.content.exercises.first() as Exercise.ParagraphWriting
        val answer = ExerciseAnswer.ParagraphText("I goed to class.\nThis is my own writing.", listOf(writing.checklist.first().id))
        repository.submit(run.session.id, writing.id, answer)
        val retry = repository.submit(run.session.id, writing.id,
            ExerciseAnswer.ParagraphText(writing.modelParagraph, writing.checklist.map { it.id }))
        assertEquals(1, retry.attempts.size)
        assertFalse(retry.attempts.single().correct)
        assertEquals(answer, ContentCodec.json.decodeFromString<ExerciseAnswer>(retry.attempts.single().answerPayload))
        assertEquals(0, retry.correctCount)
        assertEquals(6, retry.scoredQuestionCount)
        assertFalse(retry.complete)
        assertNull(database.learningDao().progress(writingLesson))
        assertEquals(answer, repository.latestParagraph(writingLesson, writing.id)?.answer)
    }

    @Test fun sevenActivitiesCompleteOnceWithOnlySixScoredQuestions() = runBlocking {
        repository.installBundledContent()
        val run = repository.beginOrResume(writingLesson)
        run.content.exercises.forEach { submitCorrect(run.session.id, it) }
        val finished = repository.run(run.session.id)
        assertTrue(finished.complete)
        assertEquals(7, finished.attempts.size)
        assertEquals(6, finished.correctCount)
        assertEquals(6, finished.scoredQuestionCount)
        assertEquals(1, finished.answeredIn(ListeningMode.READING_ALTERNATIVE))
        assertEquals(0, finished.answeredIn(ListeningMode.AUDIO))
        val before = requireNotNull(database.learningDao().progress(writingLesson))
        assertEquals(6, before.bestCorrect)
        assertEquals(6, before.questionCount)
        assertEquals(1, before.completedRuns)
        submitCorrect(run.session.id, run.content.exercises.last())
        assertEquals(before, database.learningDao().progress(writingLesson))
    }

    @Test fun latestWritingKeepsItsOriginalPromptAcrossReopenUpdatesAndNewRunsWithAdjustedClocks() = runBlocking {
        repository.installBundledContent()
        val first = repository.beginOrResume(writingLesson)
        val original = first.content.exercises.first() as Exercise.ParagraphWriting
        val firstAnswer = ExerciseAnswer.ParagraphText("I visited a library.\nمرحبا 😊", emptyList())
        repository.submit(first.session.id, original.id, firstAnswer)
        database.close()
        open()
        assertEquals(firstAnswer, repository.latestParagraph(writingLesson, original.id)?.answer)
        assertEquals(first.session.id, repository.beginOrResume(writingLesson).session.id)
        first.content.exercises.drop(1).forEach { submitCorrect(first.session.id, it) }
        val updated = ContentCodec.parsePack(a2).let { pack -> pack.copy(version = 2, units = pack.units.map { unit ->
            unit.copy(lessons = unit.lessons.map { lesson ->
                if (lesson.id != writingLesson) lesson else lesson.copy(exercises = lesson.exercises.map { exercise ->
                    if (exercise is Exercise.ParagraphWriting) exercise.copy(prompt = "A different prompt for a later version.") else exercise
                })
            })
        }) }
        repository.importPack(ContentCodec.json.encodeToString(updated))
        val next = repository.beginOrResume(writingLesson)
        assertNotEquals(first.session.id, next.session.id)
        assertEquals(original, repository.latestParagraph(writingLesson, original.id)?.exercise)
        assertEquals(firstAnswer, repository.latestParagraph(writingLesson, original.id)?.answer)
        // Equal timestamps must use save order, not random session UUID order.
        val secondAnswer = ExerciseAnswer.ParagraphText("A second saved paragraph.", emptyList())
        repository.submit(next.session.id, original.id, secondAnswer)
        assertEquals(secondAnswer, repository.latestParagraph(writingLesson, original.id)?.answer)
        next.content.exercises.drop(1).forEach { submitCorrect(next.session.id, it) }
        // A backwards system-clock adjustment must not make new writing disappear.
        now = 100L
        val third = repository.beginOrResume(writingLesson)
        val thirdAnswer = ExerciseAnswer.ParagraphText("My most recent paragraph.", emptyList())
        repository.submit(third.session.id, original.id, thirdAnswer)
        database.close()
        open()
        val latest = requireNotNull(repository.latestParagraph(writingLesson, original.id))
        assertEquals(thirdAnswer, latest.answer)
        assertEquals("A different prompt for a later version.", latest.exercise.prompt)
        assertEquals(firstAnswer, ContentCodec.json.decodeFromString<ExerciseAnswer>(repository.run(first.session.id).attempts.first().answerPayload))
        assertEquals(original, repository.run(first.session.id).content.exercises.first())
    }

    @Test fun invalidWritingCannotCreateAnAttemptOrProgress() = runBlocking {
        repository.installBundledContent()
        val run = repository.beginOrResume(writingLesson)
        val writing = run.content.exercises.first() as Exercise.ParagraphWriting
        for (answer in listOf(
            ExerciseAnswer.ParagraphText(" ", emptyList()),
            ExerciseAnswer.ParagraphText("Some text.", listOf("foreign-check")),
            ExerciseAnswer.ParagraphText("a".repeat(MAX_PARAGRAPH_CHARACTERS + 1), emptyList()),
            ExerciseAnswer.Text("Wrong answer type"),
        )) {
            try { repository.submit(run.session.id, writing.id, answer); fail("Invalid paragraph accepted") }
            catch (_: IllegalArgumentException) { }
        }
        assertTrue(repository.run(run.session.id).attempts.isEmpty())
        assertNull(repository.latestParagraph(writingLesson, writing.id))
        assertNull(database.learningDao().progress(writingLesson))
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
                val run = repository.run(session)
                if (run.attempts.none { it.exerciseId == exercise.id }) {
                    repository.prepareListening(session, exercise.id, ListeningMode.READING_ALTERNATIVE)
                }
                ExerciseAnswer.ListeningSelection(exercise.correctOptionId, ListeningMode.READING_ALTERNATIVE)
            }
        }
        repository.submit(session, exercise.id, answer)
    }
}
