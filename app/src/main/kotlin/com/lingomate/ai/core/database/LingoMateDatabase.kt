package com.lingomate.ai.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ContentPackEntity::class, LessonEntity::class, LessonProgressEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class LingoMateDatabase : RoomDatabase() {
    abstract fun learningDao(): LearningDao

    companion object {
        fun open(context: Context): LingoMateDatabase = Room.databaseBuilder(
            context.applicationContext, LingoMateDatabase::class.java, "lingomate.db",
        ).build()
        // Every future schema change needs an explicit migration. Never use destructive fallback.
    }
}
