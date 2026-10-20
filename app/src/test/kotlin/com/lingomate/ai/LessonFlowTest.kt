package com.lingomate.ai

import android.content.Context
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.lingomate.ai.core.database.LingoMateDatabase
import com.lingomate.ai.core.designsystem.LingoMateTheme
import com.lingomate.ai.core.navigation.LingoMateRoot
import com.lingomate.ai.core.preferences.UserPreferences
import com.lingomate.ai.core.preferences.UserSettings
import com.lingomate.ai.data.LearningRepository
import com.lingomate.ai.feature.settings.AppState
import com.lingomate.ai.feature.settings.AppViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class LessonFlowTest {
    @get:Rule val compose = createComposeRule()
    @get:Rule val temporary = TemporaryFolder()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var database: LingoMateDatabase
    private lateinit var repository: LearningRepository

    @Before fun prepare() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, LingoMateDatabase::class.java).build()
        repository = LearningRepository(database, {
            listOf(context.assets.open("content/english-a0-starter.json").bufferedReader().use { it.readText() })
        })
        runBlocking { repository.installBundledContent() }
        val preferences = UserPreferences(PreferenceDataStoreFactory.create(scope = scope) {
            temporary.root.resolve("lesson-flow.preferences_pb")
        })
        val model = AppViewModel(preferences)
        compose.setContent { LingoMateTheme { LingoMateRoot(AppState(UserSettings(onboardingComplete = true)), model, repository) } }
    }

    @After fun close() { scope.cancel(); database.close() }

    @Test fun allFiveActivityTypesSaveAnswersShowFeedbackAndUpdateCourseProgress() {
        click("Continue English")
        click("Open: Hello and introductions")
        click("Start practice")
        click("Hello")
        click("Check answer")
        click("Next activity")
        waitFor("Your answer")
        compose.onNodeWithText("Your answer").performTextInput("is")
        click("Check answer")
        click("Next activity")
        click("My"); click("name"); click("is"); click("Sara.")
        click("Check answer")
        click("Next activity")
        // Choose the last expression first to exercise matching with sparse draft slots.
        click("My name is Ali."); click("Mera naam Ali hai.")
        click("Hello"); click("Mulaqat ki greeting")
        click("Goodbye"); click("Alvida")
        click("Check answer")
        click("Next activity")
        click("Hello"); click("Good morning")
        click("Check answer")
        click("See results")
        waitFor("Practice complete")
        compose.onNodeWithText("5 of 5 activities correct. This is a practice result, not a language-proficiency score.").performScrollTo().assertIsDisplayed()
        click("Back to course")
        waitFor("Completed · Best practice result 5/5\nLanguage review pending.")
        runBlocking {
            val progress = requireNotNull(database.learningDao().progress("en-a0-greetings"))
            assertEquals(5, progress.bestCorrect)
            assertEquals(1, progress.completedRuns)
        }
    }

    @Test fun leavingTheLessonResumesItsPersistedAnswerAndFeedback() {
        click("Continue English")
        click("Open: Hello and introductions")
        click("Start practice")
        click("Goodbye")
        click("Check answer")
        waitFor("Keep practising")
        compose.onNodeWithContentDescription("Back").performClick()
        click("Resume: Hello and introductions")
        waitFor("Keep practising")
        click("Next activity")
        waitFor("My name ___ Ali.")
        runBlocking {
            val session = requireNotNull(database.learningDao().activeSession("en-a0-greetings"))
            assertEquals(1, database.learningDao().attempts(session.id).size)
            assertNull(database.learningDao().progress("en-a0-greetings"))
        }
    }

    private fun waitFor(text: String) {
        compose.waitUntil(10_000) { compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty() }
    }
    private fun click(text: String) { waitFor(text); compose.onNodeWithText(text).performScrollTo().performClick() }
}
