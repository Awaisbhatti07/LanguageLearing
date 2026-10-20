package com.lingomate.ai.feature.lessons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.lingomate.ai.core.audio.SpeechPlayer
import com.lingomate.learning.content.Exercise
import com.lingomate.learning.content.ExerciseAnswer
import com.lingomate.learning.content.ListeningMode

@Composable
fun ExerciseInput(
    exercise: Exercise,
    saving: Boolean,
    listeningMode: ListeningMode?,
    onListeningMode: (ListeningMode) -> Unit,
    speechPlayer: SpeechPlayer? = null,
    onSubmit: (ExerciseAnswer) -> Unit,
) {
    var selected by rememberSaveable(exercise.id) { mutableStateOf(emptyList<String>()) }
    var text by rememberSaveable(exercise.id) { mutableStateOf("") }
    var links by rememberSaveable(exercise.id) { mutableStateOf(emptyList<String>()) }
    var pendingLeft by rememberSaveable(exercise.id) { mutableStateOf<String?>(null) }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(exercise.prompt, style = MaterialTheme.typography.titleLarge)
        when (exercise) {
            is Exercise.ListeningChoice -> {
                ListeningPrompt(exercise, saving, listeningMode, onListeningMode, speechPlayer)
                exercise.options.forEach { option ->
                    Row(Modifier.fillMaxWidth().selectable(selected = option.id in selected,
                        enabled = !saving && listeningMode != null, role = Role.RadioButton,
                        onClick = { selected = listOf(option.id) }).padding(vertical = 8.dp)) {
                        RadioButton(selected = option.id in selected, onClick = null, enabled = !saving && listeningMode != null)
                        Text(option.text, modifier = Modifier.padding(12.dp))
                    }
                }
            }
            is Exercise.SingleChoice -> exercise.options.forEach { option ->
                Row(Modifier.fillMaxWidth().selectable(selected = option.id in selected, enabled = !saving,
                    role = Role.RadioButton, onClick = { selected = listOf(option.id) }).padding(vertical = 8.dp)) {
                    RadioButton(selected = option.id in selected, onClick = null)
                    Text(option.text, modifier = Modifier.padding(12.dp))
                }
            }
            is Exercise.MultiChoice -> {
                Text("Choose all answers that apply.")
                exercise.options.forEach { option ->
                    Row(Modifier.fillMaxWidth().toggleable(value = option.id in selected, enabled = !saving,
                        role = Role.Checkbox, onValueChange = { selected = if (option.id in selected) selected - option.id else selected + option.id }).padding(vertical = 8.dp)) {
                        Checkbox(checked = option.id in selected, onCheckedChange = null)
                        Text(option.text, modifier = Modifier.padding(12.dp))
                    }
                }
            }
            is Exercise.FillBlank -> OutlinedTextField(
                value = text, onValueChange = { if (it.length <= 1000) text = it }, enabled = !saving,
                label = { Text("Your answer") }, modifier = Modifier.fillMaxWidth(),
            )
            is Exercise.SentenceOrder -> {
                Text("Tap words in sentence order. Use Undo or Clear to change your answer.")
                Text(selected.joinToString(" ") { id -> exercise.tokens.first { it.id == id }.text }.ifEmpty { "Your sentence will appear here." })
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    exercise.tokens.filter { it.id !in selected }.forEach { token ->
                        OutlinedButton(onClick = { selected = selected + token.id }, enabled = !saving) { Text(token.text) }
                    }
                }
                Row {
                    TextButton(onClick = { selected = selected.dropLast(1) }, enabled = selected.isNotEmpty() && !saving) { Text("Undo") }
                    TextButton(onClick = { selected = emptyList() }, enabled = selected.isNotEmpty() && !saving) { Text("Clear") }
                }
            }
            is Exercise.MatchPairs -> {
                Text("Choose an expression, then choose its meaning. Match each pair once.")
                exercise.pairs.forEachIndexed { index, pair ->
                    val match = links.getOrNull(index)?.takeIf { it.isNotBlank() }
                    OutlinedButton(onClick = { pendingLeft = pair.id }, enabled = match == null && !saving, modifier = Modifier.fillMaxWidth()) {
                        Text(if (match != null) "${pair.left} → ${exercise.pairs.first { it.id == match }.right}" else if (pendingLeft == pair.id) "Selected: ${pair.left}" else pair.left)
                    }
                }
                // A stable rotation prevents identical display order from revealing the mapping.
                (exercise.pairs.drop(1) + exercise.pairs.take(1)).filter { it.id !in links }.forEach { pair ->
                    OutlinedButton(onClick = {
                        val index = exercise.pairs.indexOfFirst { it.id == pendingLeft }
                        if (index >= 0) {
                            val updated = links.toMutableList()
                            while (updated.size <= index) updated.add("")
                            updated[index] = pair.id
                            links = updated
                            pendingLeft = null
                        }
                    }, enabled = pendingLeft != null && !saving, modifier = Modifier.fillMaxWidth()) { Text(pair.right) }
                }
                TextButton(onClick = { links = emptyList(); pendingLeft = null }, enabled = links.isNotEmpty() && !saving) { Text("Clear pairs") }
            }
        }
        val answer: ExerciseAnswer? = when (exercise) {
            is Exercise.ListeningChoice -> selected.singleOrNull()?.let { id ->
                listeningMode?.let { ExerciseAnswer.ListeningSelection(id, it) }
            }
            is Exercise.SingleChoice, is Exercise.MultiChoice -> selected.takeIf { it.isNotEmpty() }?.let { ExerciseAnswer.Selection(it) }
            is Exercise.FillBlank -> text.takeIf { it.isNotBlank() }?.let { ExerciseAnswer.Text(it) }
            is Exercise.SentenceOrder -> selected.takeIf { it.size == exercise.tokens.size }?.let { ExerciseAnswer.Order(it) }
            is Exercise.MatchPairs -> links.takeIf { it.size == exercise.pairs.size && it.none(String::isBlank) }?.let { values ->
                ExerciseAnswer.Matches(exercise.pairs.mapIndexed { index, pair -> pair.id to values[index] }.toMap())
            }
        }
        Button(onClick = { answer?.let(onSubmit) }, enabled = answer != null && !saving, modifier = Modifier.fillMaxWidth()) {
            Text(if (saving) "Saving practice…" else "Check answer")
        }
    }
}
