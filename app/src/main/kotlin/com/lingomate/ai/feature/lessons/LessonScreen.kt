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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lingomate.ai.core.designsystem.InfoCard
import com.lingomate.ai.core.designsystem.ScreenColumn
import com.lingomate.ai.core.designsystem.SectionTitle
import com.lingomate.learning.LearningTrack
import com.lingomate.learning.content.ContentCodec
import com.lingomate.learning.content.Exercise
import com.lingomate.learning.content.ExerciseAnswer
import com.lingomate.learning.content.ExerciseGrader
import com.lingomate.learning.content.ListeningMode

@Composable
fun LessonScreen(model: LessonViewModel, onBackToCourse: () -> Unit) {
    val state by model.state.collectAsStateWithLifecycle()
    val run = state.run
    when {
        state.loading -> ScreenColumn { CircularProgressIndicator(); Text("Opening your lesson…") }
        state.failed || run == null -> ScreenColumn {
            InfoCard("Lesson could not be opened", "Your saved progress has not been reset. Try opening this lesson again.", "Try again", model::load)
        }
        // Saving, playback and errors keep the current position; a new lesson step starts at its top.
        else -> ScreenColumn(scrollKey = listOf(run.session.id, state.practice, state.feedbackPosition,
            run.complete, run.attempts.size)) {
            val content = run.content
            val languageTag = LearningTrack.valueOf(run.session.track).languageTag
            SectionTitle(content.title, "Explanations in Roman Urdu · Learning material pending language review")
            if (state.writeFailed) InfoCard("Practice could not be saved", "Try your last action again. Your earlier saved answers are preserved.", "Dismiss", model::dismissWriteError)
            val feedback = state.feedbackPosition
            when {
                feedback != null -> {
                    val exercise = content.exercises[feedback]
                    val attempt = run.attempts[feedback]
                    val answer = ContentCodec.json.decodeFromString<ExerciseAnswer>(attempt.answerPayload)
                    if (exercise is Exercise.ParagraphWriting && answer is ExerciseAnswer.ParagraphText) {
                        ParagraphReview(exercise, answer, languageTag = languageTag)
                    } else {
                        val grade = ExerciseGrader.grade(exercise, answer)
                        if (languageTag == "ar") StudyTextCard(grade.expectedAnswer, grade.explanationRomanUrdu,
                            languageTag, if (grade.correct) "Correct" else "Keep practising")
                        else InfoCard(if (grade.correct) "Correct" else "Keep practising", "Answer: ${grade.expectedAnswer}\n\n${grade.explanationRomanUrdu}")
                    }
                    if (exercise is Exercise.ListeningChoice && answer is ExerciseAnswer.ListeningSelection) {
                        Text(if (answer.mode == ListeningMode.AUDIO) "Saved as listening practice." else "Saved as a reading alternative, not a listening result.")
                        if (languageTag == "ar") {
                            Text("Transcript")
                            LearningText(exercise.speech.text, languageTag)
                        } else Text("Transcript: ${exercise.speech.text}")
                    }
                    Text("Answer saved on this device.")
                    Button(onClick = model::next) { Text(if (run.complete) "See results" else "Next activity") }
                }
                run.complete -> {
                    val scoredLabel = if (content.exercises.any { it is Exercise.ParagraphWriting }) "scored activities" else "activities"
                    InfoCard("Practice complete", "${run.correctCount} of ${run.scoredQuestionCount} $scoredLabel correct. This is a practice result, not a language-proficiency score.")
                    run.attempts.forEachIndexed { index, attempt ->
                        val exercise = content.exercises[index]
                        if (exercise is Exercise.ParagraphWriting) {
                            val answer = ContentCodec.json.decodeFromString<ExerciseAnswer>(attempt.answerPayload) as ExerciseAnswer.ParagraphText
                            ParagraphReview(exercise, answer, languageTag = languageTag)
                        }
                    }
                    if (content.exercises.any { it is Exercise.ListeningChoice }) {
                        Text("Listening answers: ${run.answeredIn(ListeningMode.AUDIO)} · Reading alternatives: ${run.answeredIn(ListeningMode.READING_ALTERNATIVE)}")
                    }
                    Text("Your answers and completion are saved. Revisit this lesson anytime to practise again.")
                    Button(onClick = onBackToCourse) { Text("Back to course") }
                }
                state.practice -> {
                    Text("Activity ${run.attempts.size + 1} of ${content.exercises.size}", color = MaterialTheme.colorScheme.primary)
                    key(run.session.id, run.attempts.size) {
                        val exercise = content.exercises[run.attempts.size]
                        ExerciseInput(exercise, state.saving, run.listeningModes[exercise.id],
                            onListeningMode = { model.prepareListening(exercise.id, it) }, languageTag = languageTag) { model.submit(exercise.id, it) }
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
                    content.vocabulary.forEach { item -> StudyTextCard(item.word, "${item.meaningRomanUrdu}\n${item.partOfSpeech} · Say: ${item.pronunciationGuide}", languageTag) }
                    content.reading?.let { StudyTextCard(it.text, it.meaningRomanUrdu, languageTag, "Read and understand") }
                    InfoCard("Say it in your own words", content.speakingPrompts.joinToString("\n\n") + "\n\nSelf-practice only. No recording or pronunciation score.")
                    state.savedParagraphs.forEach { paragraph ->
                        ParagraphReview(paragraph.exercise, paragraph.answer, "Most recent saved paragraph", languageTag)
                    }
                    Button(onClick = model::startPractice, modifier = Modifier.fillMaxWidth()) { Text(if (run.attempts.isEmpty()) "Start practice" else "Resume practice") }
                }
            }
        }
    }
}

@Composable
private fun LessonExamples(examples: List<com.lingomate.learning.content.LessonExample>, track: LearningTrack) {
    val player = rememberLessonSpeechPlayer()
    StopSpeechWhenInactive(player)
    val speech by player.state.collectAsStateWithLifecycle()
    examples.forEach { example ->
        Card {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                LearningText(example.text, track.languageTag, style = MaterialTheme.typography.titleLarge)
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

@Composable
private fun StudyTextCard(original: String, meaning: String, languageTag: String, heading: String? = null) {
    Card {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            heading?.let { Text(it, style = MaterialTheme.typography.titleLarge) }
            LearningText(original, languageTag, style = if (heading == null) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyLarge)
            Text(meaning, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
