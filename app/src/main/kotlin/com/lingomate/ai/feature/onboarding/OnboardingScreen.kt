package com.lingomate.ai.feature.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lingomate.ai.core.designsystem.InfoCard
import com.lingomate.ai.core.designsystem.ScreenColumn
import com.lingomate.ai.core.designsystem.SectionTitle
import com.lingomate.ai.core.preferences.UserPreferences

@Composable
fun OnboardingScreen(saving: Boolean, onStart: (Int) -> Unit) {
    var goal by rememberSaveable { mutableIntStateOf(3) }
    ScreenColumn {
        Text("LINGOMATE AI", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
        SectionTitle("A little practice.\nA new possibility.", "Learn at your pace, with explanations in Roman Urdu.")
        InfoCard("Your learning space", "English, Arabic Fusha and Quranic Arabic are separate learning tracks. Lessons will clearly show which content is available.")
        InfoCard("Made for everyday learning", "Use bundled lessons without an account or internet. Your progress stays on this device.")
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Choose a daily goal", style = MaterialTheme.typography.titleMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                UserPreferences.GOALS.forEach { count ->
                    FilterChip(
                        selected = goal == count, onClick = { goal = count }, enabled = !saving,
                        label = { Text("$count ${if (count == 1) "lesson" else "lessons"}") },
                    )
                }
            }
            Text("You can change this anytime. There is no penalty for a missed day.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Button(onClick = { onStart(goal) }, enabled = !saving, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            Text(if (saving) "Saving…" else "Start learning")
        }
    }
}
