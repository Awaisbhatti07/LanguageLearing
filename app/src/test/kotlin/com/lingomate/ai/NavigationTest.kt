package com.lingomate.ai

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.lingomate.ai.core.designsystem.LingoMateTheme
import com.lingomate.ai.core.navigation.LingoMateRoot
import com.lingomate.ai.core.preferences.UserPreferences
import com.lingomate.ai.core.preferences.UserSettings
import com.lingomate.ai.feature.onboarding.OnboardingScreen
import com.lingomate.ai.feature.settings.AppState
import com.lingomate.ai.feature.settings.AppViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class NavigationTest {
    @get:Rule val compose = createComposeRule()
    @get:Rule val temporary = TemporaryFolder()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @After fun close() { scope.cancel() }

    @Test fun allTabsAreReachableAndDescribeUnavailableFeaturesHonestly() {
        showRoot()
        compose.onNodeWithText("Small steps.\nReal progress.").assertIsDisplayed()
        compose.onNodeWithText("Learn").performClick()
        compose.onNodeWithText("Find your next step").assertIsDisplayed()
        compose.onNodeWithText("Speak").performClick()
        compose.onNodeWithText("AI teacher is not available yet").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Review").performClick()
        compose.onNodeWithText("Your review list starts here").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Home").performClick()
        compose.onNodeWithText("Small steps.\nReal progress.").assertIsDisplayed()
    }

    @Test fun settingsBackReturnsToTheSelectedTab() {
        showRoot()
        compose.onNodeWithText("Learn").performClick()
        compose.onNodeWithContentDescription("Settings").performClick()
        compose.onNodeWithText("Make it yours").assertIsDisplayed()
        compose.onNodeWithContentDescription("Back").performClick()
        compose.onNodeWithText("Find your next step").assertIsDisplayed()
    }

    @Test fun onboardingSendsTheSelectedDailyGoal() {
        var submitted = -1
        compose.setContent { LingoMateTheme { OnboardingScreen(false) { submitted = it } } }
        compose.onNodeWithText("1 lesson").performScrollTo().performClick()
        compose.onNodeWithText("Start learning").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(1, submitted) }
    }

    private fun showRoot() {
        val preferences = UserPreferences(PreferenceDataStoreFactory.create(scope = scope) {
            temporary.root.resolve("navigation.preferences_pb")
        })
        val model = AppViewModel(preferences)
        compose.setContent {
            LingoMateTheme {
                LingoMateRoot(AppState(settings = UserSettings(onboardingComplete = true)), model)
            }
        }
    }
}
