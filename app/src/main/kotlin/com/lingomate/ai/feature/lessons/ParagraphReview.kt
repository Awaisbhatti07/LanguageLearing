package com.lingomate.ai.feature.lessons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lingomate.ai.core.designsystem.InfoCard
import com.lingomate.learning.content.Exercise
import com.lingomate.learning.content.ExerciseAnswer

@Composable
internal fun ParagraphReview(exercise: Exercise.ParagraphWriting, answer: ExerciseAnswer.ParagraphText,
    title: String = "Paragraph saved", languageTag: String = "en") {
    if (languageTag == "ar") ArabicParagraphCard(title, exercise.prompt, answer.text)
    else InfoCard(title, "Original prompt: ${exercise.prompt}\n\n${answer.text}")
    Text("Self-practice saved on this device. No automatic writing grade; this does not change your quiz score.")
    exercise.checklist.forEach { criterion ->
        Text("${if (criterion.id in answer.checkedCriterionIds) "Checked" else "Not checked"}: ${criterion.text}")
    }
    if (languageTag == "ar") ArabicParagraphCard("Example paragraph · One possible response",
        null, exercise.modelParagraph, exercise.modelMeaningRomanUrdu)
    else InfoCard("Example paragraph · One possible response", "${exercise.modelParagraph}\n\n${exercise.modelMeaningRomanUrdu}")
    Text(exercise.explanationRomanUrdu)
}

@Composable
private fun ArabicParagraphCard(title: String, prompt: String?, paragraph: String, meaning: String? = null) {
    Card {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            prompt?.let { Text("Original prompt: $it") }
            LearningText(paragraph, "ar")
            meaning?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}
