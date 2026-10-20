package com.lingomate.ai.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lingomate.ai.core.designsystem.InfoCard
import com.lingomate.ai.core.designsystem.ScreenColumn
import com.lingomate.ai.core.designsystem.SectionTitle
import com.lingomate.ai.core.preferences.UserSettings
import com.lingomate.learning.LearningTrack
import com.lingomate.ai.data.LearningCatalog
import com.lingomate.ai.feature.courses.LearningUiState
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun HomeScreen(settings: UserSettings, learning: LearningUiState, onRetry: () -> Unit, onTrack: (LearningTrack) -> Unit) {
    ScreenColumn {
        Text("YOUR DAILY MOMENT", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        SectionTitle("Small steps.\nReal progress.", "Make room for something new today.")
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val studied = learning.catalog.sessions.filter { session ->
            session.completedAt?.let { Instant.ofEpochMilli(it).atZone(zone).toLocalDate() == today } == true
        }.map { it.lessonId }.distinct().size
        InfoCard("Today's goal · ${settings.dailyGoal} lessons", "$studied distinct lessons practised today. Start with a short lesson, practise its examples, then return for a review.")
        if (learning.failed) InfoCard("Lessons could not be loaded", "Saved learning data has not been reset.", "Try again", onRetry)
        else if (learning.loading) Text("Loading your offline lessons…")
        LearningTrack.entries.forEach { TrackCard(it, learning.catalog, onTrack = onTrack) }
    }
}

@Composable
fun TrackCard(track: LearningTrack, catalog: LearningCatalog, onTrack: (LearningTrack) -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                when (track) {
                    LearningTrack.ENGLISH -> "Learn English"
                    LearningTrack.FUSHA -> "Learn Arabic"
                    LearningTrack.QURANIC -> "Understand Quranic Arabic"
                },
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                when (track) {
                    LearningTrack.ENGLISH -> "Build confidence, one sentence at a time."
                    LearningTrack.FUSHA -> "A separate path for Modern Standard Arabic."
                    LearningTrack.QURANIC -> "Vocabulary and comprehension with verified sources."
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            val total = catalog.lessons.count { it.track == track.name }
            val completed = catalog.completed(track)
            Text(if (total == 0) "No lessons installed yet" else "$completed of $total installed lessons completed", style = MaterialTheme.typography.labelLarge)
            if (total > 0) LinearProgressIndicator(progress = { completed.toFloat() / total }, modifier = Modifier.fillMaxWidth())
            OutlinedButton(onClick = { onTrack(track) }) { Text(if (total > 0) "Continue ${track.title}" else "Explore ${track.title}") }
        }
    }
}
