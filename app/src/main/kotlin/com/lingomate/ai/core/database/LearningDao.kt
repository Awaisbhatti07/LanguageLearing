package com.lingomate.ai.core.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import androidx.room.Insert
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

    @Query("SELECT * FROM lessons WHERE packId = :packId ORDER BY position, id")
    suspend fun packLessons(packId: String): List<LessonEntity>

    @Query("SELECT * FROM lesson_sessions WHERE lessonId = :lessonId AND completedAt IS NULL LIMIT 1")
    suspend fun activeSession(lessonId: String): LessonSessionEntity?

    @Query("SELECT * FROM lesson_sessions WHERE id = :id")
    suspend fun session(id: String): LessonSessionEntity?

    @Insert
    suspend fun insertSession(session: LessonSessionEntity)

    @Query("UPDATE lesson_sessions SET completedAt = :completedAt, activeLessonKey = NULL WHERE id = :id AND completedAt IS NULL")
    suspend fun completeSession(id: String, completedAt: Long): Int

    @Query("SELECT * FROM exercise_attempts WHERE sessionId = :sessionId ORDER BY position")
    suspend fun attempts(sessionId: String): List<ExerciseAttemptEntity>

    @Insert
    suspend fun insertAttempt(attempt: ExerciseAttemptEntity)

    @Query("SELECT * FROM listening_presentations WHERE sessionId = :sessionId")
    suspend fun listeningPresentations(sessionId: String): List<ListeningPresentationEntity>

    @Upsert
    suspend fun upsertListeningPresentation(presentation: ListeningPresentationEntity)

    @Query("SELECT * FROM lesson_sessions ORDER BY startedAt DESC, id")
    fun observeSessions(): Flow<List<LessonSessionEntity>>
}
