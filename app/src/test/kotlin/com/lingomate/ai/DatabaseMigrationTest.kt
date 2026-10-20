package com.lingomate.ai

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.lingomate.ai.core.database.LingoMateDatabase
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
        val schema = JSONObject(File(System.getProperty("lingomate.schemas"),
            "com.lingomate.ai.core.database.LingoMateDatabase/1.json").readText()).getJSONObject("database")
        context.openOrCreateDatabase(name, Context.MODE_PRIVATE, null).use { db ->
            val entities = schema.getJSONArray("entities")
            for (i in 0 until entities.length()) {
                val entity = entities.getJSONObject(i)
                val table = entity.getString("tableName")
                db.execSQL(entity.getString("createSql").replace("\${TABLE_NAME}", table))
                val indices = entity.optJSONArray("indices")
                if (indices != null) for (j in 0 until indices.length()) db.execSQL(indices.getJSONObject(j).getString("createSql").replace("\${TABLE_NAME}", table))
            }
            val setup = schema.getJSONArray("setupQueries")
            for (i in 0 until setup.length()) db.execSQL(setup.getString(i))
            db.execSQL("INSERT INTO content_packs VALUES ('old-pack', 1, 1, 'legacy fixture', 100)")
            db.execSQL("INSERT INTO lessons VALUES ('old-lesson', 'old-pack', 'ENGLISH', 'A0', 0, 'Old lesson', '{}')")
            db.execSQL("INSERT INTO lesson_progress VALUES ('old-lesson', 200, 200, 4, 5, 3)")
            db.version = 1
        }
        val database = Room.databaseBuilder(context, LingoMateDatabase::class.java, name)
            .addMigrations(LingoMateDatabase.MIGRATION_1_2).build()
        try {
            // Opening through Room executes the migration and validates every v2 table/index/FK.
            val progress = requireNotNull(database.learningDao().progress("old-lesson"))
            assertEquals(3, progress.completedRuns)
            assertEquals(4, progress.bestCorrect)
            assertEquals(5, progress.questionCount)
            assertEquals(200L, progress.completedAt)
            assertEquals("legacy-unit", database.learningDao().lesson("old-lesson")?.unitId)
            assertEquals(2, database.openHelper.writableDatabase.version)
        } finally {
            database.close()
            context.deleteDatabase(name)
        }
    }
}
