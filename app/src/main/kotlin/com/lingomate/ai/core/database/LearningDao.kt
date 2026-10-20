package com.lingomate.ai.core.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface LearningDao {
    @Query("SELECT * FROM content_packs WHERE id = :id")
    suspend fun pack(id: String): ContentPackEntity?

    @Upsert
    suspend fun upsertPack(pack: ContentPackEntity)

    @Upsert
    suspend fun upsertLessons(lessons: List<LessonEntity>)

    @Query("SELECT * FROM lessons ORDER BY track, position, id")
    fun observeLessons(): Flow<List<LessonEntity>>

    @Query("SELECT * FROM lessons WHERE id = :id")
    suspend fun lesson(id: String): LessonEntity?

    @Query("SELECT * FROM lesson_progress ORDER BY lessonId")
    fun observeProgress(): Flow<List<LessonProgressEntity>>

    @Query("SELECT * FROM lesson_progress WHERE lessonId = :lessonId")
    suspend fun progress(lessonId: String): LessonProgressEntity?

    @Upsert
    suspend fun upsertProgress(progress: LessonProgressEntity)
}
