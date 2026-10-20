package com.lingomate.ai

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.lingomate.ai.core.database.LingoMateDatabase
import com.lingomate.ai.data.LearningRepository
import com.lingomate.learning.content.*
import java.io.File
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class DatabaseMigrationTest {
    @Test fun versionOneLearnerProgressSurvivesTheExplicitMigrationAndSchemaValidation() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "migration-${UUID.randomUUID()}.db"
        historicalDatabase(context, name, 1) { db ->
            db.execSQL("INSERT INTO content_packs VALUES ('old-pack', 1, 1, 'legacy fixture', 100)")
            db.execSQL("INSERT INTO lessons VALUES ('old-lesson', 'old-pack', 'ENGLISH', 'A0', 0, 'Old lesson', '{}')")
            db.execSQL("INSERT INTO lesson_progress VALUES ('old-lesson', 200, 200, 4, 5, 3)")
        }
        val database = Room.databaseBuilder(context, LingoMateDatabase::class.java, name)
            .addMigrations(LingoMateDatabase.MIGRATION_1_2, LingoMateDatabase.MIGRATION_2_3).build()
        try {
            // Opening through Room runs both migrations and validates the complete current schema.
            val progress = requireNotNull(database.learningDao().progress("old-lesson"))
            assertEquals(3, progress.completedRuns)
            assertEquals(4, progress.bestCorrect)
            assertEquals(5, progress.questionCount)
            assertEquals(200L, progress.completedAt)
            assertEquals("legacy-unit", database.learningDao().lesson("old-lesson")?.unitId)
            assertEquals(3, database.openHelper.writableDatabase.version)
            assertTrue(database.learningDao().listeningPresentations("old-session").isEmpty())
        } finally {
            database.close()
            context.deleteDatabase(name)
        }
    }

    @Test fun versionTwoAnswersSnapshotsAndProgressSurviveListeningMigration() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "migration-${UUID.randomUUID()}.db"
        val pack = ContentCodec.parsePack(requireNotNull(javaClass.getResource("/content/english-a0-v2.json")).readText())
        val lesson = pack.units.flatMap { it.lessons }.first { it.id == "en-a0-greetings" }
        val first = lesson.exercises.first() as Exercise.SingleChoice
        val payload = ContentCodec.json.encodeToString(lesson)
        val answer = ContentCodec.json.encodeToString<ExerciseAnswer>(ExerciseAnswer.Selection(listOf(first.correctOptionId)))
        historicalDatabase(context, name, 2) { db ->
            db.execSQL("INSERT INTO content_packs VALUES (?, 2, 1, 'legacy provenance', 100)", arrayOf(pack.id))
            db.execSQL("INSERT INTO lessons VALUES (?, ?, 'ENGLISH', 'A0', 0, ?, ?, 'legacy-unit', 'Lessons', 'PENDING_REVIEW')",
                arrayOf(lesson.id, pack.id, lesson.title, payload))
            db.execSQL("INSERT INTO lesson_progress VALUES (?, 200, 200, 4, 5, 3)", arrayOf(lesson.id))
            db.execSQL("INSERT INTO lesson_sessions VALUES ('saved-session', ?, ?, 2, 'ENGLISH', 300, NULL, ?)",
                arrayOf(lesson.id, payload, lesson.id))
            db.execSQL("INSERT INTO exercise_attempts VALUES ('saved-session', ?, 0, ?, 1, 400)", arrayOf(first.id, answer))
        }
        val database = Room.databaseBuilder(context, LingoMateDatabase::class.java, name)
            .addMigrations(LingoMateDatabase.MIGRATION_2_3).build()
        try {
            val run = LearningRepository(database, { emptyList() }).beginOrResume(lesson.id)
            assertEquals("saved-session", run.session.id)
            assertEquals(lesson, run.content)
            assertEquals(2, run.session.packVersion)
            assertEquals(answer, run.attempts.single().answerPayload)
            assertTrue(run.listeningModes.isEmpty())
            val progress = requireNotNull(database.learningDao().progress(lesson.id))
            assertEquals(3, progress.completedRuns)
            assertEquals(4, progress.bestCorrect)
            assertEquals(3, database.openHelper.writableDatabase.version)
        } finally {
            database.close()
            context.deleteDatabase(name)
        }
    }

    private fun historicalDatabase(context: Context, name: String, version: Int, seed: (SQLiteDatabase) -> Unit) {
        val schema = JSONObject(File(System.getProperty("lingomate.schemas"),
            "com.lingomate.ai.core.database.LingoMateDatabase/$version.json").readText()).getJSONObject("database")
        context.openOrCreateDatabase(name, Context.MODE_PRIVATE, null).use { db ->
            val entities = schema.getJSONArray("entities")
            for (i in 0 until entities.length()) {
                val entity = entities.getJSONObject(i)
                val table = entity.getString("tableName")
                db.execSQL(entity.getString("createSql").replace("\${TABLE_NAME}", table))
                val indices = entity.optJSONArray("indices")
                if (indices != null) for (j in 0 until indices.length()) {
                    db.execSQL(indices.getJSONObject(j).getString("createSql").replace("\${TABLE_NAME}", table))
                }
            }
            val setup = schema.getJSONArray("setupQueries")
            for (i in 0 until setup.length()) db.execSQL(setup.getString(i))
            seed(db)
            db.version = version
        }
    }
}
