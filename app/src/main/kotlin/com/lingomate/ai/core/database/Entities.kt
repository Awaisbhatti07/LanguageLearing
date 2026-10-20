package com.lingomate.ai.core.database

import androidx.room.Entity
import androidx.room.ColumnInfo
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "content_packs")
data class ContentPackEntity(
    @PrimaryKey val id: String,
    val version: Int,
    val schemaVersion: Int,
    val provenance: String,
    val importedAt: Long,
)

@Entity(
    tableName = "lessons",
    foreignKeys = [ForeignKey(
        entity = ContentPackEntity::class,
        parentColumns = ["id"], childColumns = ["packId"],
        onDelete = ForeignKey.RESTRICT,
    )],
    indices = [Index("packId"), Index(value = ["track", "position"])],
)
data class LessonEntity(
    @PrimaryKey val id: String,
    val packId: String,
    val track: String,
    val level: String,
    val position: Int,
    val title: String,
    val payload: String,
    @ColumnInfo(defaultValue = "'legacy-unit'") val unitId: String = "legacy-unit",
    @ColumnInfo(defaultValue = "'Lessons'") val unitTitle: String = "Lessons",
    @ColumnInfo(defaultValue = "'PENDING_REVIEW'") val reviewStatus: String = "PENDING_REVIEW",
)

@Entity(
    tableName = "lesson_progress",
    foreignKeys = [ForeignKey(
        entity = LessonEntity::class,
        parentColumns = ["id"], childColumns = ["lessonId"],
        onDelete = ForeignKey.RESTRICT,
    )],
)
data class LessonProgressEntity(
    @PrimaryKey val lessonId: String,
    val lastStudiedAt: Long,
    val completedAt: Long?,
    val bestCorrect: Int,
    val questionCount: Int,
    val completedRuns: Int,
)

@Entity(
    tableName = "lesson_sessions",
    foreignKeys = [ForeignKey(
        entity = LessonEntity::class, parentColumns = ["id"], childColumns = ["lessonId"],
        onDelete = ForeignKey.RESTRICT,
    )],
    indices = [Index("lessonId"), Index(value = ["activeLessonKey"], unique = true)],
)
data class LessonSessionEntity(
    @PrimaryKey val id: String,
    val lessonId: String,
    val contentPayload: String,
    val packVersion: Int,
    val track: String,
    val startedAt: Long,
    val completedAt: Long?,
    val activeLessonKey: String?,
)

@Entity(
    tableName = "exercise_attempts",
    primaryKeys = ["sessionId", "exerciseId"],
    foreignKeys = [ForeignKey(
        entity = LessonSessionEntity::class, parentColumns = ["id"], childColumns = ["sessionId"],
        onDelete = ForeignKey.RESTRICT,
    )],
    indices = [Index(value = ["sessionId", "position"], unique = true)],
)
data class ExerciseAttemptEntity(
    val sessionId: String,
    val exerciseId: String,
    val position: Int,
    val answerPayload: String,
    val correct: Boolean,
    val answeredAt: Long,
)

@Entity(
    tableName = "listening_presentations",
    primaryKeys = ["sessionId", "exerciseId"],
    foreignKeys = [ForeignKey(
        entity = LessonSessionEntity::class, parentColumns = ["id"], childColumns = ["sessionId"],
        onDelete = ForeignKey.RESTRICT,
    )],
)
data class ListeningPresentationEntity(
    val sessionId: String,
    val exerciseId: String,
    val mode: String,
)
