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
class FushaUpperRepositoryTest {
    @get:Rule val temporary = TemporaryFolder()
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private fun source(file: String) = context.assets.open("content/$file").bufferedReader().use { it.readText() }
    private val earlier by lazy { listOf("english-a0-starter.json", "english-a1-beginner.json", "english-a2-elementary.json",
        "english-b1-intermediate.json", "fusha-a0-foundation.json", "fusha-a1-beginner.json").map(::source) }
    private val upper by lazy { listOf("fusha-a2-elementary.json", "fusha-b1-intermediate.json").map(::source) }
    private lateinit var database: LingoMateDatabase
    private lateinit var repository: LearningRepository

    @Before fun open() {
        database = Room.databaseBuilder(context, LingoMateDatabase::class.java,
            temporary.root.resolve("fusha-upper.db").absolutePath).build()
        repository = LearningRepository(database, { upper.reversed() + earlier.reversed() })
    }
    @After fun close() { database.close() }

    @Test fun reversedImportOrdersAllFourFushaLevelsAndKeepsEnglishSeparate() = runBlocking {
        repository.installBundledContent()
        repository.installBundledContent()
        val catalog = repository.catalog.first()
        val fusha = catalog.lessons.filter { it.track == "FUSHA" }
        val expected = (earlier.takeLast(2) + upper).flatMap { ContentCodec.parsePack(it).units.flatMap { unit -> unit.lessons }.map { it.id } }
        assertEquals(expected, fusha.map { it.id })
        assertEquals(List(12) { "A0" } + List(18) { "A1" } + List(20) { "A2" } + List(22) { "B1" }, fusha.map { it.level })
        assertEquals(146, catalog.lessons.size)
        assertEquals(74, catalog.lessons.count { it.track == "ENGLISH" })
        assertTrue(catalog.progress.isEmpty())
    }

    @Test fun addingUpperLevelsPreservesEnglishAndEarlierFushaLearningAcrossReopen() = runBlocking {
        earlier.forEach { repository.importPack(it) }
        val englishDone = repository.beginOrResume("en-a1-routines")
        englishDone.content.exercises.forEach { submitCorrect(englishDone.session.id, it) }
        val a0Active = repository.beginOrResume("ar-a0-short-vowels")
        submitCorrect(a0Active.session.id, a0Active.content.exercises.first())
        val a1Done = repository.beginOrResume("ar-a1-personal-pronouns")
        a1Done.content.exercises.forEach { submitCorrect(a1Done.session.id, it) }
        val a1Reading = repository.beginOrResume("ar-a1-present-verbs")
        a1Reading.content.exercises.dropLast(1).forEach { submitCorrect(a1Reading.session.id, it) }
        repository.prepareListening(a1Reading.session.id, a1Reading.content.exercises.last().id, ListeningMode.READING_ALTERNATIVE)
        val englishWriting = repository.beginOrResume("en-a2-paragraph-writing")
        val englishTask = englishWriting.content.exercises.first() as Exercise.ParagraphWriting
        repository.submit(englishWriting.session.id, englishTask.id, ExerciseAnswer.ParagraphText("My English writing before the Fusha update.\nOriginal text.", emptyList()))
        val before = listOf(englishDone, a0Active, a1Done, a1Reading, englishWriting).map { repository.run(it.session.id) }
        val progress = listOf(englishDone, a1Done).map { database.learningDao().progress(it.session.lessonId) }
        val englishSaved = repository.latestParagraph(englishWriting.session.lessonId, englishTask.id)
        // Import A2 first and create original writing before adding B1.
        repository.importPack(upper.first())
        val a2Run = repository.beginOrResume("ar-a2-practical-message")
        val a2Task = a2Run.content.exercises.first() as Exercise.ParagraphWriting
        val ownArabic = ExerciseAnswer.ParagraphText("هذا نصي الأصلي.\nأريد تغيير الموعد. 😊", listOf(a2Task.checklist.first().id))
        repository.submit(a2Run.session.id, a2Task.id, ownArabic)
        val a2Before = repository.run(a2Run.session.id)
        val a2Saved = repository.latestParagraph(a2Run.session.lessonId, a2Task.id)
        repository.installBundledContent()
        database.close(); open()
        before.forEach { assertEquals(it, repository.run(it.session.id)) }
        assertEquals(progress, listOf(englishDone, a1Done).map { database.learningDao().progress(it.session.lessonId) })
        assertEquals(englishSaved, repository.latestParagraph(englishWriting.session.lessonId, englishTask.id))
        assertEquals(a2Before, repository.beginOrResume(a2Run.session.lessonId))
        assertEquals(a2Saved, repository.latestParagraph(a2Run.session.lessonId, a2Task.id))
        assertEquals(ownArabic, repository.latestParagraph(a2Run.session.lessonId, a2Task.id)?.answer)
        assertNull(database.learningDao().progress("ar-b1-case-roles"))
    }

    @Test fun allFortyTwoUpperLessonsCompleteWithSixScoredQuestionsAndReadingOnlyCredit() = runBlocking {
        repository.installBundledContent()
        val lessons = upper.flatMap { ContentCodec.parsePack(it).units.flatMap { unit -> unit.lessons } }
        val completed = lessons.map { lesson ->
            val run = repository.beginOrResume(lesson.id)
            lesson.exercises.forEach { submitCorrect(run.session.id, it) }
            repository.run(run.session.id).also {
                assertTrue(it.complete)
                assertEquals(6, it.correctCount)
                assertEquals(6, it.scoredQuestionCount)
                assertEquals(lesson.exercises.size, it.attempts.size)
                assertEquals(1, it.answeredIn(ListeningMode.READING_ALTERNATIVE))
                assertEquals(0, it.answeredIn(ListeningMode.AUDIO))
            }
        }
        database.close(); open()
        completed.forEach { assertEquals(it, repository.run(it.session.id)) }
        val catalog = repository.catalog.first()
        assertEquals(42, catalog.completed(LearningTrack.FUSHA))
        assertEquals(0, catalog.completed(LearningTrack.ENGLISH))
        assertTrue(catalog.progress.all { (it.lessonId.startsWith("ar-a2-") || it.lessonId.startsWith("ar-b1-")) && it.completedRuns == 1 })
        assertNull(database.learningDao().progress("ar-a0-alphabet"))
        assertNull(database.learningDao().progress("ar-a1-personal-pronouns"))
    }

    @Test fun allSixArabicWritingTasksKeepOriginalTextChecklistsAndPromptsThroughRetryUpdateAndReopen() = runBlocking {
        repository.installBundledContent()
        val lessons = upper.flatMap { ContentCodec.parsePack(it).units.flatMap { unit -> unit.lessons } }
            .filter { it.exercises.any { exercise -> exercise is Exercise.ParagraphWriting } }
        assertEquals(6, lessons.size)
        val originals = lessons.associate { lesson ->
            val task = lesson.exercises.filterIsInstance<Exercise.ParagraphWriting>().single()
            val answer = ExerciseAnswer.ParagraphText("فقرتي الأصلية عن ${lesson.title}.\nCafe\u0301 😊", listOf(task.checklist.first().id))
            val run = repository.beginOrResume(lesson.id)
            lesson.exercises.forEach { exercise ->
                if (exercise is Exercise.ParagraphWriting) {
                    repository.submit(run.session.id, exercise.id, answer)
                    repository.submit(run.session.id, exercise.id, ExerciseAnswer.ParagraphText(exercise.modelParagraph, emptyList()))
                } else submitCorrect(run.session.id, exercise)
            }
            assertEquals(6, repository.run(run.session.id).correctCount)
            assertEquals(6, database.learningDao().progress(lesson.id)?.questionCount)
            lesson.id to requireNotNull(repository.latestParagraph(lesson.id, task.id))
        }
        for (source in upper) {
            val pack = ContentCodec.parsePack(source)
            repository.importPack(ContentCodec.json.encodeToString(pack.copy(version = 2, units = pack.units.map { unit ->
                unit.copy(lessons = unit.lessons.map { lesson -> lesson.copy(exercises = lesson.exercises.map { exercise ->
                    if (exercise is Exercise.ParagraphWriting) exercise.copy(prompt = "Updated prompt for later practice") else exercise
                }) })
            })))
        }
        database.close(); open()
        for (lesson in lessons) {
            val task = lesson.exercises.filterIsInstance<Exercise.ParagraphWriting>().single()
            assertEquals(originals.getValue(lesson.id), repository.latestParagraph(lesson.id, task.id))
            assertEquals(task, repository.latestParagraph(lesson.id, task.id)?.exercise)
            val next = repository.beginOrResume(lesson.id)
            assertEquals(2, next.session.packVersion)
            assertTrue(next.attempts.isEmpty())
            assertEquals("Updated prompt for later practice", (next.content.exercises.filterIsInstance<Exercise.ParagraphWriting>().single()).prompt)
            assertEquals(1, database.learningDao().progress(lesson.id)?.completedRuns)
        }
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
