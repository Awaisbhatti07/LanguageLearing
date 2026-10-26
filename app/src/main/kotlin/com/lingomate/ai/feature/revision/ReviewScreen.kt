package com.lingomate.ai.feature.revision

import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import com.lingomate.ai.core.designsystem.InfoCard
import com.lingomate.ai.core.designsystem.ScreenColumn
import com.lingomate.ai.core.designsystem.SectionTitle
import com.lingomate.ai.feature.courses.LearningUiState

@Composable
fun ReviewScreen(learning: LearningUiState, onRetry: () -> Unit, onLearn: () -> Unit, onLesson: (String) -> Unit) {
    ScreenColumn {
        SectionTitle("A little revision goes a long way")
        when {
            learning.failed -> InfoCard("Progress could not be loaded", "Your learning data has not been reset.", "Try again", onRetry)
            learning.loading -> Text("Loading your learning history…")
            learning.catalog.progress.isEmpty() -> InfoCard("Your review list starts here", "Complete a lesson to build your learning history. You can revisit completed lessons here. Scheduled spaced revision is still being developed.", "Find a lesson", onLearn)
            else -> {
                InfoCard("Revisit what you learned", "These are completed lessons and saved practice results. Scheduled spaced revision is still being developed.")
                learning.catalog.progress.sortedBy { it.lastStudiedAt }.forEach { progress ->
                    learning.catalog.lessons.firstOrNull { it.id == progress.lessonId }?.let { lesson ->
                        InfoCard(
                            lesson.title,
                            "Best practice result: ${progress.bestCorrect}/${progress.questionCount}\n${progress.completedRuns} completed practice ${if (progress.completedRuns == 1) "run" else "runs"}",
                            "Practise again",
                            { onLesson(lesson.id) },
                        )
                    }
                }
            }
        }
    }
}
