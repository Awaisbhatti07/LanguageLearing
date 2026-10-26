package com.lingomate.ai.feature.lessons

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lingomate.ai.core.audio.SpeechPlayer
import com.lingomate.learning.content.Exercise
import com.lingomate.learning.content.ListeningMode

@Composable
internal fun ListeningPrompt(
    exercise: Exercise.ListeningChoice,
    saving: Boolean,
    mode: ListeningMode?,
    onMode: (ListeningMode) -> Unit,
    speechPlayer: SpeechPlayer? = null,
) {
    val player = speechPlayer ?: rememberLessonSpeechPlayer()
    StopSpeechWhenInactive(player, exercise.id)
    val speech by player.state.collectAsStateWithLifecycle()
    // A request from a previous composition cannot confirm hearing in this activity.
    var pendingRequest by remember(exercise.id, player) { mutableStateOf<String?>(null) }
    LaunchedEffect(speech.completedRequestId, pendingRequest, saving, mode) {
        if (!saving && pendingRequest != null && speech.completedRequestId == pendingRequest) {
            pendingRequest = null
            if (mode != ListeningMode.READING_ALTERNATIVE) onMode(ListeningMode.AUDIO)
        }
    }
    Text(if (mode == ListeningMode.READING_ALTERNATIVE) "Reading alternative" else "Listening practice",
        style = MaterialTheme.typography.titleMedium)
    Text("Listen to the whole audio, then choose an answer. The transcript stays hidden unless you choose reading.")
    OutlinedButton(onClick = {
        pendingRequest = player.speak(exercise.speech.text, exercise.speech.languageTag)
    }, enabled = !saving && !speech.initializing) {
        Text(if (mode == ListeningMode.AUDIO) "Replay audio" else "Play audio")
    }
    if (speech.initializing) Text("Preparing the offline voice… You can choose reading while you wait.")
    if (speech.playing) TextButton(onClick = { pendingRequest = null; player.stop() }) { Text("Stop audio") }
    speech.message?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    when (mode) {
        ListeningMode.AUDIO -> Text("Playback finished. You can answer or replay the audio.")
        ListeningMode.READING_ALTERNATIVE -> {
            if (exercise.speech.languageTag == "ar") {
                Text("Transcript")
                LearningText(exercise.speech.text, "ar")
            } else Text("Transcript: ${exercise.speech.text}")
            Text("This answer will be saved as reading practice, including after you reopen the lesson.")
        }
        null -> Text("Answer choices unlock after playback finishes or you choose the reading alternative.")
    }
    if (mode != ListeningMode.READING_ALTERNATIVE) {
        TextButton(onClick = {
            pendingRequest = null
            player.stop()
            onMode(ListeningMode.READING_ALTERNATIVE)
        }, enabled = !saving) { Text("Use reading instead") }
    }
    Text("Audio uses an installed offline device voice. No microphone or online service is used.",
        style = MaterialTheme.typography.bodySmall)
}
