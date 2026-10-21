package com.lingomate.ai.feature.lessons

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.lingomate.ai.core.designsystem.InfoCard
import com.lingomate.learning.content.Exercise
import com.lingomate.learning.content.ExerciseAnswer

@Composable
internal fun ParagraphReview(exercise: Exercise.ParagraphWriting, answer: ExerciseAnswer.ParagraphText,
    title: String = "Paragraph saved") {
    InfoCard(title, "Original prompt: ${exercise.prompt}\n\n${answer.text}")
    Text("Self-practice saved on this device. No automatic writing grade; this does not change your quiz score.")
    exercise.checklist.forEach { criterion ->
        Text("${if (criterion.id in answer.checkedCriterionIds) "Checked" else "Not checked"}: ${criterion.text}")
    }
    InfoCard("Example paragraph · One possible response", "${exercise.modelParagraph}\n\n${exercise.modelMeaningRomanUrdu}")
    Text(exercise.explanationRomanUrdu)
}
