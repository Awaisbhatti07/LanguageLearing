package com.lingomate.ai.feature.courses

import androidx.compose.runtime.Composable
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import com.lingomate.ai.core.designsystem.InfoCard
import com.lingomate.ai.core.designsystem.ScreenColumn
import com.lingomate.ai.core.designsystem.SectionTitle
import com.lingomate.ai.feature.home.TrackCard
import com.lingomate.learning.LearningTrack

@Composable
fun LearnScreen(learning: LearningUiState, onRetry: () -> Unit, onTrack: (LearningTrack) -> Unit) {
    ScreenColumn {
        SectionTitle("Find your next step", "Choose a learning track. Each path keeps its own progress.")
        if (learning.failed) InfoCard("Lessons could not be loaded", "Try again without resetting your saved data.", "Try again", onRetry)
        else if (learning.loading) Text("Loading offline content…")
        LearningTrack.entries.forEach { TrackCard(it, learning.catalog, onTrack) }
    }
}

@Composable
fun CourseScreen(track: LearningTrack, learning: LearningUiState, onRetry: () -> Unit, onLesson: (String) -> Unit) {
    ScreenColumn {
        SectionTitle(track.title, "From first words to everyday understanding.")
        val lessons = learning.catalog.lessons.filter { it.track == track.name }
        when {
            learning.failed -> InfoCard("Lessons could not be loaded", "Your saved data has not been reset.", "Try again", onRetry)
            learning.loading -> Text("Loading offline content…")
            lessons.isNotEmpty() -> {
                InfoCard("About this content", "${lessons.size} lessons are installed. Curriculum development and language review are in progress; no complete A0–B1 course or certification is claimed.")
                val next = lessons.firstOrNull { lesson -> learning.catalog.progress.none { it.lessonId == lesson.id && it.completedAt != null } } ?: lessons.first()
                Button(onClick = { onLesson(next.id) }) { Text("Continue: ${next.title}") }
                lessons.groupBy { it.level to it.unitId }.forEach { (group, unit) ->
                    Text("${group.first} · ${unit.first().unitTitle}", style = MaterialTheme.typography.titleLarge)
                    unit.forEach { lesson ->
                        val progress = learning.catalog.progress.firstOrNull { it.lessonId == lesson.id }
                        val active = learning.catalog.sessions.any { it.lessonId == lesson.id && it.completedAt == null }
                        val status = when {
                            active -> "Practice in progress · Your saved answers will resume."
                            progress?.completedAt != null -> "Completed · Best practice result ${progress.bestCorrect}/${progress.questionCount}"
                            else -> "Ready to learn · Explanation, examples and practice."
                        }
                        InfoCard(lesson.title, "$status\nLanguage review pending.", if (active) "Resume: ${lesson.title}" else "Open: ${lesson.title}", { onLesson(lesson.id) })
                    }
                }
            }
            else -> InfoCard(
            "Content is being prepared",
            if (track == LearningTrack.QURANIC) {
                "Quranic lessons need verified sources, attribution and language review. No Quranic text or frequency counts are included in this build."
            } else {
                "No lessons are installed for this track yet. Completed levels will appear here as their content becomes available."
            },
            )
        }
    }
}
