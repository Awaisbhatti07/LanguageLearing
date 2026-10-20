package com.lingomate.learning.content

import java.text.Normalizer
import java.util.Locale

fun normalizeAnswer(value: String, ignoreCase: Boolean = true): String {
    val normalized = Normalizer.normalize(value, Normalizer.Form.NFC)
        .trim().replace(Regex("[\\s\\p{Z}]+"), " ")
    return if (ignoreCase) normalized.lowercase(Locale.ROOT) else normalized
}

data class GradeResult(
    val valid: Boolean,
    val correct: Boolean,
    val expectedAnswer: String,
    val explanationRomanUrdu: String,
)

/** One point per activity; multi-choice, order and matching use all-or-nothing grading. */
object ExerciseGrader {
    fun grade(exercise: Exercise, answer: ExerciseAnswer): GradeResult {
        ContentValidator.validateExercise(exercise)
        val expected = expectedAnswer(exercise)
        fun result(valid: Boolean, correct: Boolean = false) = GradeResult(
            valid, valid && correct, expected, exercise.explanationRomanUrdu,
        )
        return when (exercise) {
            is Exercise.SingleChoice -> {
                val ids = (answer as? ExerciseAnswer.Selection)?.optionIds ?: return result(false)
                val valid = ids.size == 1 && ids.single() in exercise.options.map { it.id }
                result(valid, valid && ids.single() == exercise.correctOptionId)
            }
            is Exercise.MultiChoice -> {
                val ids = (answer as? ExerciseAnswer.Selection)?.optionIds ?: return result(false)
                val valid = ids.isNotEmpty() && ids.distinct().size == ids.size && ids.all { id -> exercise.options.any { it.id == id } }
                result(valid, ids.toSet() == exercise.correctOptionIds.toSet())
            }
            is Exercise.FillBlank -> {
                val input = (answer as? ExerciseAnswer.Text)?.value ?: return result(false)
                val normalized = normalizeAnswer(input, exercise.ignoreCase)
                val valid = normalized.isNotEmpty() && input.length <= 1_000 && '\u0000' !in input
                result(valid, exercise.acceptedAnswers.any { normalizeAnswer(it, exercise.ignoreCase) == normalized })
            }
            is Exercise.SentenceOrder -> {
                val ids = (answer as? ExerciseAnswer.Order)?.tokenIds ?: return result(false)
                val tokens = exercise.tokens.associate { it.id to it.text }
                val valid = ids.size == tokens.size && ids.toSet() == tokens.keys
                // Identical repeated words are interchangeable, even when their token IDs differ.
                result(valid, valid && ids.map(tokens::getValue) == exercise.correctOrder.map(tokens::getValue))
            }
            is Exercise.MatchPairs -> {
                val links = (answer as? ExerciseAnswer.Matches)?.links ?: return result(false)
                val ids = exercise.pairs.map { it.id }.toSet()
                val valid = links.keys == ids && links.values.toSet() == ids && links.values.size == ids.size
                result(valid, links.all { (left, right) -> left == right })
            }
        }
    }

    fun expectedAnswer(exercise: Exercise): String = when (exercise) {
        is Exercise.SingleChoice -> exercise.options.first { it.id == exercise.correctOptionId }.text
        is Exercise.MultiChoice -> exercise.options.filter { it.id in exercise.correctOptionIds }.joinToString("; ") { it.text }
        is Exercise.FillBlank -> exercise.acceptedAnswers.joinToString(" / ")
        is Exercise.SentenceOrder -> exercise.correctOrder.joinToString(" ") { id -> exercise.tokens.first { it.id == id }.text }
        is Exercise.MatchPairs -> exercise.pairs.joinToString("; ") { "${it.left} → ${it.right}" }
    }
}
