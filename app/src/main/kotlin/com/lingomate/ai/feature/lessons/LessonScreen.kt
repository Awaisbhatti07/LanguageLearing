package com.lingomate.ai.feature.lessons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lingomate.ai.core.audio.DeviceSpeechPlayer
import com.lingomate.ai.core.designsystem.InfoCard
import com.lingomate.ai.core.designsystem.ScreenColumn
import com.lingomate.ai.core.designsystem.SectionTitle
import com.lingomate.learning.LearningTrack
import com.lingomate.learning.content.ContentCodec
import com.lingomate.learning.content.ExerciseAnswer
import com.lingomate.learning.content.ExerciseGrader

@Composable
fun LessonScreen(model: LessonViewModel, onBackToCourse: () -> Unit) {
    val state by model.state.collectAsStateWithLifecycle()
    val run = state.run
    when {
        state.loading -> ScreenColumn { CircularProgressIndicator(); Text("Opening your lesson…") }
        state.failed || run == null -> ScreenColumn {
            InfoCard("Lesson could not be opened", "Your saved progress has not been reset. Try opening this lesson again.", "Try again", model::load)
        }
        else -> ScreenColumn {
            val content = run.content
            SectionTitle(content.title, "Explanations in Roman Urdu · Learning material pending language review")
            if (state.writeFailed) InfoCard("Answer could not be saved", "Try Check answer again. Your earlier saved answers are preserved.", "Dismiss", model::dismissWriteError)
            val feedback = state.feedbackPosition
            when {
                feedback != null -> {
                    val exercise = content.exercises[feedback]
                    val attempt = run.attempts[feedback]
                    val answer = ContentCodec.json.decodeFromString<ExerciseAnswer>(attempt.answerPayload)
                    val grade = ExerciseGrader.grade(exercise, answer)
                    InfoCard(if (grade.correct) "Correct" else "Keep practising", "Answer: ${grade.expectedAnswer}\n\n${grade.explanationRomanUrdu}")
                    Text("Answer saved on this device.")
                    Button(onClick = model::next) { Text(if (run.complete) "See results" else "Next activity") }
                }
                run.complete -> {
                    InfoCard("Practice complete", "${run.correctCount} of ${content.exercises.size} activities correct. This is a practice result, not a language-proficiency score.")
                    Text("Your answers and completion are saved. Revisit this lesson anytime to practise again.")
                    Button(onClick = onBackToCourse) { Text("Back to course") }
                }
                state.practice -> {
                    Text("Activity ${run.attempts.size + 1} of ${content.exercises.size}", color = MaterialTheme.colorScheme.primary)
                    key(run.session.id, run.attempts.size) {
                        val exercise = content.exercises[run.attempts.size]
                        ExerciseInput(exercise, state.saving) { model.submit(exercise.id, it) }
                    }
                }
                else -> {
                    Text("What you will learn", style = MaterialTheme.typography.titleMedium)
                    content.objectives.forEach { Text("• $it") }
                    InfoCard("Understand the idea", content.explanationRomanUrdu)
                    Text("Rules and examples", style = MaterialTheme.typography.titleLarge)
                    content.rulesRomanUrdu.forEach { Text("• $it") }
                    LessonExamples(content.examples, LearningTrack.valueOf(run.session.track))
                    Text("Useful words", style = MaterialTheme.typography.titleLarge)
                    content.vocabulary.forEach { item -> InfoCard(item.word, "${item.meaningRomanUrdu}\n${item.partOfSpeech} · Say: ${item.pronunciationGuide}") }
                    content.reading?.let { InfoCard("Read and understand", "${it.text}\n\n${it.meaningRomanUrdu}") }
                    InfoCard("Say it in your own words", content.speakingPrompts.joinToString("\n\n") + "\n\nSelf-practice only. No recording or pronunciation score.")
                    Button(onClick = model::startPractice, modifier = Modifier.fillMaxWidth()) { Text(if (run.attempts.isEmpty()) "Start practice" else "Resume practice") }
                }
            }
        }
    }
}

@Composable
private fun LessonExamples(examples: List<com.lingomate.learning.content.LessonExample>, track: LearningTrack) {
    val context = LocalContext.current
    val player = remember(context) { DeviceSpeechPlayer(context) }
    val speech by player.state.collectAsStateWithLifecycle()
    val owner = LocalLifecycleOwner.current
    DisposableEffect(player, owner) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_STOP) player.stop() }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer); player.close() }
    }
    examples.forEach { example ->
        Card {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(example.text, style = MaterialTheme.typography.titleLarge.copy(
                    textDirection = if (track == LearningTrack.ENGLISH) TextDirection.Ltr else TextDirection.Rtl))
                Text(example.meaningRomanUrdu)
                example.speech?.let { reference ->
                    TextButton(onClick = { player.speak(reference.text, reference.languageTag) }, enabled = !speech.initializing) {
                        Text("Listen: ${example.text}")
                    }
                }
            }
        }
    }
    if (speech.playing) TextButton(onClick = player::stop) { Text("Stop audio") }
    speech.message?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    Text("Audio uses an installed offline device voice. Pronunciation guides are approximate.", style = MaterialTheme.typography.bodySmall)
}
