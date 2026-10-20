package com.lingomate.ai.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.lingomate.ai.core.designsystem.InfoCard
import com.lingomate.ai.core.designsystem.ScreenColumn
import com.lingomate.ai.core.designsystem.SectionTitle
import com.lingomate.ai.core.preferences.ThemeMode
import com.lingomate.ai.core.preferences.UserPreferences
import com.lingomate.ai.core.preferences.UserSettings

@Composable
fun SettingsScreen(settings: UserSettings, saving: Boolean, onTheme: (ThemeMode) -> Unit, onGoal: (Int) -> Unit) {
    ScreenColumn {
        SectionTitle("Make it yours", "Simple preferences for your daily practice.")
        Text("Appearance")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ThemeMode.entries.forEach { theme ->
                FilterChip(selected = settings.theme == theme, enabled = !saving, onClick = { onTheme(theme) }, label = {
                    Text(theme.name.lowercase().replaceFirstChar { it.uppercase() })
                })
            }
        }
        Text("Daily lesson goal")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            UserPreferences.GOALS.forEach { goal ->
                FilterChip(selected = settings.dailyGoal == goal, enabled = !saving, onClick = { onGoal(goal) }, label = {
                    Text("$goal ${if (goal == 1) "lesson" else "lessons"}")
                })
            }
        }
        InfoCard("Private by default", "No account, analytics or cloud sync. Learning progress is stored on this device. Android automatic cloud backup is disabled; manual backup will be added before release.")
        InfoCard("Optional AI", "An online teacher needs a separately configured secure service and may cost money. No paid API or microphone access is enabled in this build.")
        InfoCard("About LingoMate AI", "Version 0.1.0 · Development preview\nEnglish UI · Roman Urdu explanations\nContent availability and review status are shown in each track.")
    }
}
