package com.lingomate.ai

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.lingomate.ai.core.database.ContentPackEntity
import com.lingomate.ai.core.database.LessonEntity
import com.lingomate.ai.core.database.LessonProgressEntity
import com.lingomate.ai.core.database.LingoMateDatabase
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class LocalPersistenceTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val name = "persistence-${UUID.randomUUID()}.db"
    private lateinit var database: LingoMateDatabase

    @Before fun open() {
        database = Room.databaseBuilder(context, LingoMateDatabase::class.java, name).build()
    }

    @After fun close() {
        database.close()
        context.deleteDatabase(name)
    }

    @Test fun progressSurvivesDatabaseReopen() = runBlocking {
        val dao = database.learningDao()
        seed()
        val progress = LessonProgressEntity("lesson-1", 1000, 1000, 2, 3, 1)
        dao.upsertProgress(progress)
        database.close()
        open()
        assertEquals(progress, database.learningDao().progress("lesson-1"))
    }

    @Test fun updatingContentDoesNotReplaceLearnerProgress() = runBlocking {
        seed()
        val dao = database.learningDao()
        val progress = LessonProgressEntity("lesson-1", 1000, 1000, 2, 3, 1)
        dao.upsertProgress(progress)
        dao.upsertPack(ContentPackEntity("pack-1", 2, 1, "test update", 2000))
        dao.upsertLessons(listOf(LessonEntity("lesson-1", "pack-1", "ENGLISH", "A0", 0, "Updated", "{}")))
        assertEquals("Updated", dao.lesson("lesson-1")?.title)
        assertEquals(progress, dao.progress("lesson-1"))
    }

    private suspend fun seed() {
        database.learningDao().upsertPack(ContentPackEntity("pack-1", 1, 1, "test fixture", 0))
        database.learningDao().upsertLessons(listOf(
            LessonEntity("lesson-1", "pack-1", "ENGLISH", "A0", 0, "Example", "{}"),
        ))
    }
}
