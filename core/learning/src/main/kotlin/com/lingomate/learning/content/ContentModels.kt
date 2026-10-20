package com.lingomate.learning.content

import com.lingomate.learning.CefrLevel
import com.lingomate.learning.LearningTrack
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ReviewStatus { PENDING_REVIEW, REVIEWED }

@Serializable
data class Provenance(
    val author: String,
    val rightsNotice: String,
    val reviewStatus: ReviewStatus,
    val reviewer: String? = null,
)

@Serializable
data class ContentPack(
    val schemaVersion: Int,
    val id: String,
    val version: Int,
    val title: String,
    val track: LearningTrack,
    val level: CefrLevel,
    val provenance: Provenance,
    val units: List<LearningUnit>,
)

@Serializable
data class LearningUnit(val id: String, val title: String, val lessons: List<LessonContent>)

@Serializable
data class SpeechReference(val text: String, val languageTag: String)

@Serializable
data class LessonExample(val text: String, val meaningRomanUrdu: String, val speech: SpeechReference? = null)

@Serializable
data class VocabularyItem(
    val id: String,
    val word: String,
    val meaningRomanUrdu: String,
    val partOfSpeech: String,
    val pronunciationGuide: String,
)

@Serializable
data class ReadingActivity(val text: String, val meaningRomanUrdu: String)

@Serializable
data class LessonContent(
    val id: String,
    val title: String,
    val objectives: List<String>,
    val explanationRomanUrdu: String,
    val rulesRomanUrdu: List<String>,
    val examples: List<LessonExample>,
    val vocabulary: List<VocabularyItem>,
    val reading: ReadingActivity? = null,
    val speakingPrompts: List<String>,
    val reviewTags: List<String>,
    val exercises: List<Exercise>,
)

@Serializable
data class ChoiceOption(val id: String, val text: String)

@Serializable
data class MatchPair(val id: String, val left: String, val right: String)

@Serializable
enum class ListeningMode { AUDIO, READING_ALTERNATIVE }

@Serializable
sealed interface Exercise {
    val id: String
    val prompt: String
    val explanationRomanUrdu: String

    @Serializable @SerialName("single_choice")
    data class SingleChoice(
        override val id: String,
        override val prompt: String,
        override val explanationRomanUrdu: String,
        val options: List<ChoiceOption>,
        val correctOptionId: String,
    ) : Exercise

    @Serializable @SerialName("listening_choice")
    data class ListeningChoice(
        override val id: String,
        override val prompt: String,
        override val explanationRomanUrdu: String,
        val speech: SpeechReference,
        val options: List<ChoiceOption>,
        val correctOptionId: String,
    ) : Exercise

    @Serializable @SerialName("multi_choice")
    data class MultiChoice(
        override val id: String,
        override val prompt: String,
        override val explanationRomanUrdu: String,
        val options: List<ChoiceOption>,
        val correctOptionIds: List<String>,
    ) : Exercise

    @Serializable @SerialName("fill_blank")
    data class FillBlank(
        override val id: String,
        override val prompt: String,
        override val explanationRomanUrdu: String,
        val acceptedAnswers: List<String>,
        val ignoreCase: Boolean = true,
    ) : Exercise

    @Serializable @SerialName("sentence_order")
    data class SentenceOrder(
        override val id: String,
        override val prompt: String,
        override val explanationRomanUrdu: String,
        val tokens: List<ChoiceOption>,
        val correctOrder: List<String>,
    ) : Exercise

    @Serializable @SerialName("match_pairs")
    data class MatchPairs(
        override val id: String,
        override val prompt: String,
        override val explanationRomanUrdu: String,
        val pairs: List<MatchPair>,
    ) : Exercise
}

@Serializable
sealed interface ExerciseAnswer {
    @Serializable @SerialName("selection")
    data class Selection(val optionIds: List<String>) : ExerciseAnswer

    @Serializable @SerialName("listening_selection")
    data class ListeningSelection(val optionId: String, val mode: ListeningMode) : ExerciseAnswer

    @Serializable @SerialName("text")
    data class Text(val value: String) : ExerciseAnswer

    @Serializable @SerialName("order")
    data class Order(val tokenIds: List<String>) : ExerciseAnswer

    @Serializable @SerialName("matches")
    data class Matches(val links: Map<String, String>) : ExerciseAnswer
}
