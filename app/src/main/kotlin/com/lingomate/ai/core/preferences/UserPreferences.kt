package com.lingomate.ai.core.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ThemeMode { DARK, LIGHT, SYSTEM }

data class UserSettings(
    val theme: ThemeMode = ThemeMode.DARK,
    val dailyGoal: Int = 3,
    val onboardingComplete: Boolean = false,
)

class UserPreferences(private val store: DataStore<Preferences>) {
    val settings: Flow<UserSettings> = store.data.map { preferences ->
        UserSettings(
            theme = ThemeMode.entries.firstOrNull { it.name == preferences[THEME] } ?: ThemeMode.DARK,
            dailyGoal = preferences[GOAL]?.takeIf { it in GOALS } ?: 3,
            onboardingComplete = preferences[ONBOARDED] ?: false,
        )
    }

    suspend fun setTheme(theme: ThemeMode) { store.edit { it[THEME] = theme.name } }

    suspend fun setDailyGoal(goal: Int) {
        require(goal in GOALS) { "Unsupported daily goal" }
        store.edit { it[GOAL] = goal }
    }

    suspend fun completeOnboarding(goal: Int) {
        require(goal in GOALS) { "Unsupported daily goal" }
        store.edit {
            it[GOAL] = goal
            it[ONBOARDED] = true
        }
    }

    companion object {
        val GOALS = listOf(1, 3, 5)
        private val THEME = stringPreferencesKey("theme")
        private val GOAL = intPreferencesKey("daily_goal_lessons")
        private val ONBOARDED = booleanPreferencesKey("onboarding_complete")
    }
}
