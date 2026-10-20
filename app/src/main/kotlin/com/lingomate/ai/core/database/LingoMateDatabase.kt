package com.lingomate.ai.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [ContentPackEntity::class, LessonEntity::class, LessonProgressEntity::class,
        LessonSessionEntity::class, ExerciseAttemptEntity::class],
    version = 2,
    exportSchema = true,
)
abstract class LingoMateDatabase : RoomDatabase() {
    abstract fun learningDao(): LearningDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE lessons ADD COLUMN unitId TEXT NOT NULL DEFAULT 'legacy-unit'")
                db.execSQL("ALTER TABLE lessons ADD COLUMN unitTitle TEXT NOT NULL DEFAULT 'Lessons'")
                db.execSQL("ALTER TABLE lessons ADD COLUMN reviewStatus TEXT NOT NULL DEFAULT 'PENDING_REVIEW'")
                db.execSQL("CREATE TABLE IF NOT EXISTS lesson_sessions (id TEXT NOT NULL, lessonId TEXT NOT NULL, contentPayload TEXT NOT NULL, packVersion INTEGER NOT NULL, track TEXT NOT NULL, startedAt INTEGER NOT NULL, completedAt INTEGER, activeLessonKey TEXT, PRIMARY KEY(id), FOREIGN KEY(lessonId) REFERENCES lessons(id) ON UPDATE NO ACTION ON DELETE RESTRICT)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_lesson_sessions_lessonId ON lesson_sessions(lessonId)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_lesson_sessions_activeLessonKey ON lesson_sessions(activeLessonKey)")
                db.execSQL("CREATE TABLE IF NOT EXISTS exercise_attempts (sessionId TEXT NOT NULL, exerciseId TEXT NOT NULL, position INTEGER NOT NULL, answerPayload TEXT NOT NULL, correct INTEGER NOT NULL, answeredAt INTEGER NOT NULL, PRIMARY KEY(sessionId, exerciseId), FOREIGN KEY(sessionId) REFERENCES lesson_sessions(id) ON UPDATE NO ACTION ON DELETE RESTRICT)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_exercise_attempts_sessionId_position ON exercise_attempts(sessionId, position)")
            }
        }

        fun open(context: Context): LingoMateDatabase = Room.databaseBuilder(
            context.applicationContext, LingoMateDatabase::class.java, "lingomate.db",
        ).addMigrations(MIGRATION_1_2).build()
        // Every future schema change needs an explicit migration. Never use destructive fallback.
    }
}
