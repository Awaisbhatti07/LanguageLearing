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

@Composable
fun HomeScreen(settings: UserSettings, onTrack: (LearningTrack) -> Unit) {
    ScreenColumn {
        Text("YOUR DAILY MOMENT", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        SectionTitle("Small steps.\nReal progress.", "Make room for something new today.")
        InfoCard("Today's goal · ${settings.dailyGoal} lessons", "Start with a short lesson, practise its examples, then return for a review.")
        LearningTrack.entries.forEach { TrackCard(it, onTrack = onTrack) }
    }
}

@Composable
fun TrackCard(track: LearningTrack, onTrack: (LearningTrack) -> Unit) {
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
            Text("No lessons completed", style = MaterialTheme.typography.labelLarge)
            LinearProgressIndicator(progress = { 0f }, modifier = Modifier.fillMaxWidth())
            OutlinedButton(onClick = { onTrack(track) }) { Text("Explore ${track.title}") }
        }
    }
}
