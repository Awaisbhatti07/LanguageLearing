package com.lingomate.ai

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.lingomate.ai.core.audio.SpeechPlayer
import com.lingomate.ai.core.audio.SpeechState
import com.lingomate.ai.core.designsystem.LingoMateTheme
import com.lingomate.ai.core.designsystem.ScreenColumn
import com.lingomate.ai.feature.lessons.ExerciseInput
import com.lingomate.learning.content.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ListeningExerciseTest {
    @get:Rule val compose = createComposeRule()
    private val exercise = Exercise.ListeningChoice("listen-name", "What is the speaker's name?",
        "Speaker ka naam Sara hai.", SpeechReference("Hello. My name is Sara.", "en"),
        listOf(ChoiceOption("name-sara", "Sara"), ChoiceOption("name-ali", "Ali")), "name-sara")
    private val mode = mutableStateOf<ListeningMode?>(null)
    private val currentExercise = mutableStateOf(exercise)
    private val player = FakeSpeechPlayer()
    private val modeRequests = mutableListOf<ListeningMode>()
    private val answers = mutableListOf<ExerciseAnswer>()

    private fun show(persistImmediately: Boolean = true) {
        compose.setContent { LingoMateTheme { ScreenColumn {
            ExerciseInput(currentExercise.value, saving = false, listeningMode = mode.value,
                onListeningMode = { requested ->
                    modeRequests.add(requested)
                    if (persistImmediately) mode.value = requested
                }, speechPlayer = player, onSubmit = { answers.add(it) })
        } } }
    }

    @Test fun transcriptIsHiddenAndAnswerRemainsLockedUntilWholePlaybackCompletes() {
        show()
        compose.onNodeWithText("Transcript: ${exercise.speech.text}").assertDoesNotExist()
        compose.onNodeWithText("Check answer").performScrollTo().assertIsNotEnabled()
        click("Play audio")
        compose.onNodeWithText("Sara").performScrollTo().assertIsNotEnabled()
        compose.runOnIdle { player.complete(requireNotNull(player.latestRequest)) }
        compose.waitUntil(5_000) { mode.value == ListeningMode.AUDIO }
        compose.onNodeWithText("Transcript: ${exercise.speech.text}").assertDoesNotExist()
        click("Sara")
        click("Check answer")
        compose.runOnIdle {
            assertEquals(listOf(ExerciseAnswer.ListeningSelection("name-sara", ListeningMode.AUDIO)), answers)
        }
    }

    @Test fun missingVoiceOffersReadingButTranscriptWaitsForPersistence() {
        player.unavailable = true
        show(persistImmediately = false)
        click("Play audio")
        compose.onNodeWithText("No installed offline voice.").performScrollTo().assertIsDisplayed()
        click("Use reading instead")
        compose.runOnIdle { assertEquals(listOf(ListeningMode.READING_ALTERNATIVE), modeRequests) }
        compose.onNodeWithText("Transcript: ${exercise.speech.text}").assertDoesNotExist()
        compose.onNodeWithText("Sara").performScrollTo().assertIsNotEnabled()
        // Simulate successful durable storage; a failed write would leave mode null and text hidden.
        compose.runOnIdle { mode.value = ListeningMode.READING_ALTERNATIVE }
        compose.onNodeWithText("Transcript: ${exercise.speech.text}").performScrollTo().assertIsDisplayed()
        click("Sara"); click("Check answer")
        compose.runOnIdle {
            assertEquals(ExerciseAnswer.ListeningSelection("name-sara", ListeningMode.READING_ALTERNATIVE), answers.single())
        }
    }

    @Test fun stoppedAndStalePlaybackCannotUnlockTheCurrentListeningAnswer() {
        show()
        click("Play audio")
        val old = requireNotNull(player.latestRequest)
        click("Stop audio")
        compose.runOnIdle { player.complete(old) }
        compose.waitForIdle()
        compose.runOnIdle { assertTrue(modeRequests.isEmpty()) }
        click("Play audio")
        val current = requireNotNull(player.latestRequest)
        compose.runOnIdle { player.complete(old) }
        compose.waitForIdle()
        compose.runOnIdle { assertTrue(modeRequests.isEmpty()) }
        compose.runOnIdle { player.complete(current) }
        compose.waitUntil(5_000) { mode.value == ListeningMode.AUDIO }
    }

    @Test fun audioAfterReadingDoesNotRequestListeningCredit() {
        show()
        click("Use reading instead")
        compose.waitUntil(5_000) { mode.value == ListeningMode.READING_ALTERNATIVE }
        click("Play audio")
        compose.runOnIdle { player.complete(requireNotNull(player.latestRequest)) }
        compose.waitForIdle()
        compose.runOnIdle { assertEquals(listOf(ListeningMode.READING_ALTERNATIVE), modeRequests) }
        click("Sara"); click("Check answer")
        compose.runOnIdle {
            assertEquals(ExerciseAnswer.ListeningSelection("name-sara", ListeningMode.READING_ALTERNATIVE), answers.single())
        }
    }

    @Test fun anOldActivityCompletionCannotUnlockADifferentListeningQuestion() {
        show()
        click("Play audio")
        val old = requireNotNull(player.latestRequest)
        compose.runOnIdle { currentExercise.value = exercise.copy(id = "listen-next") }
        compose.waitForIdle()
        compose.runOnIdle { player.complete(old) }
        compose.waitForIdle()
        compose.runOnIdle { assertTrue(modeRequests.isEmpty()) }
        compose.onNodeWithText("Sara").performScrollTo().assertIsNotEnabled()
    }

    private fun click(text: String) { compose.onNodeWithText(text).performScrollTo().performClick() }

    private class FakeSpeechPlayer : SpeechPlayer {
        override val state = MutableStateFlow(SpeechState(initializing = false))
        var unavailable = false
        var latestRequest: String? = null
        private var count = 0
        override fun speak(text: String, languageTag: String): String? {
            if (unavailable) {
                state.value = SpeechState(initializing = false, message = "No installed offline voice.")
                return null
            }
            val request = "fake-${++count}"
            latestRequest = request
            state.value = SpeechState(initializing = false, playing = true, requestId = request)
            return request
        }
        // Deliberately permits stale callbacks so the UI's request matching is exercised.
        fun complete(request: String) { state.value = SpeechState(initializing = false, completedRequestId = request) }
        override fun stop() { state.value = SpeechState(initializing = false) }
        override fun close() { stop() }
    }
}
