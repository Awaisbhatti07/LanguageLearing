package com.lingomate.ai

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.lingomate.ai.core.preferences.ThemeMode
import com.lingomate.ai.core.preferences.UserPreferences
import com.lingomate.ai.core.designsystem.useDarkTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class UserPreferencesTest {
    @get:Rule val temporary = TemporaryFolder()

    @Test fun defaultsAreDarkAndOnboardingIsNotSkipped() = runBlocking {
        withPreferences { preferences ->
            val settings = preferences.settings.first()
            assertEquals(ThemeMode.DARK, settings.theme)
            assertFalse(settings.onboardingComplete)
            assertEquals(3, settings.dailyGoal)
        }
    }

    @Test fun onboardingAndThemeSurviveReopeningTheStore() = runBlocking {
        withPreferences {
            it.completeOnboarding(5)
            it.setTheme(ThemeMode.LIGHT)
        }
        withPreferences {
            val settings = it.settings.first()
            assertTrue(settings.onboardingComplete)
            assertEquals(5, settings.dailyGoal)
            assertEquals(ThemeMode.LIGHT, settings.theme)
        }
    }

    @Test fun invalidGoalCannotOverwriteValidSettings() = runBlocking {
        withPreferences {
            it.setDailyGoal(1)
            try {
                it.completeOnboarding(0)
                throw AssertionError("Expected validation failure")
            } catch (_: IllegalArgumentException) { /* rejected before write */ }
            assertEquals(1, it.settings.first().dailyGoal)
            assertFalse(it.settings.first().onboardingComplete)
        }
    }

    @Test fun explicitThemeChoiceOverridesSystemPreference() {
        assertTrue(useDarkTheme(ThemeMode.DARK, systemDark = false))
        assertFalse(useDarkTheme(ThemeMode.LIGHT, systemDark = true))
        assertTrue(useDarkTheme(ThemeMode.SYSTEM, systemDark = true))
        assertFalse(useDarkTheme(ThemeMode.SYSTEM, systemDark = false))
    }

    private suspend fun withPreferences(block: suspend (UserPreferences) -> Unit) {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val store = PreferenceDataStoreFactory.create(scope = scope) {
            temporary.root.resolve("settings.preferences_pb")
        }
        try { block(UserPreferences(store)) }
        finally { scope.coroutineContext.job.cancelAndJoin() }
    }
}
