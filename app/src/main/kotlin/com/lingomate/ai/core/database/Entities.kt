package com.lingomate.ai.core.database

import androidx.room.Entity
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
