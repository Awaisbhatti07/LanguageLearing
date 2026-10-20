package com.lingomate.ai.feature.courses

import androidx.compose.runtime.Composable
import com.lingomate.ai.core.designsystem.InfoCard
import com.lingomate.ai.core.designsystem.ScreenColumn
import com.lingomate.ai.core.designsystem.SectionTitle
import com.lingomate.ai.feature.home.TrackCard
import com.lingomate.learning.LearningTrack

@Composable
fun LearnScreen(onTrack: (LearningTrack) -> Unit) {
    ScreenColumn {
        SectionTitle("Find your next step", "Choose a learning track. Each path keeps its own progress.")
        LearningTrack.entries.forEach { TrackCard(it, onTrack) }
    }
}

@Composable
fun CourseScreen(track: LearningTrack) {
    ScreenColumn {
        SectionTitle(track.title, "From first words to everyday understanding.")
        InfoCard(
            "Content is being prepared",
            if (track == LearningTrack.QURANIC) {
                "Quranic lessons need verified sources, attribution and language review. No Quranic text or frequency counts are included in this build."
            } else {
                "No lessons are installed for this track yet. Completed levels will appear here as their content becomes available."
            },
        )
    }
}
