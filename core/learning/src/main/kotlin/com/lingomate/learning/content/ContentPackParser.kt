package com.lingomate.learning.content

import com.lingomate.learning.LearningTrack
import kotlinx.serialization.json.Json

object ContentCodec {
    const val MAX_PACK_BYTES = 2 * 1024 * 1024
    val json = Json {
        ignoreUnknownKeys = false
        isLenient = false
        coerceInputValues = false
        encodeDefaults = true
    }

    fun parsePack(source: String): ContentPack {
        require(source.length <= MAX_PACK_BYTES && source.toByteArray(Charsets.UTF_8).size <= MAX_PACK_BYTES) {
            "Content pack exceeds size limit"
        }
        var quoted = false
        var escaped = false
        var depth = 0
        source.forEach { character ->
            if (quoted) {
                if (escaped) escaped = false
                else if (character == '\\') escaped = true
                else if (character == '"') quoted = false
            } else when (character) {
                '"' -> quoted = true
                '{', '[' -> { depth += 1; require(depth <= 32) { "Content nesting exceeds limit" } }
                '}', ']' -> depth -= 1
            }
        }
        return json.decodeFromString<ContentPack>(source).also(ContentValidator::validate)
    }
}

object ContentValidator {
    private val idPattern = Regex("[a-z][a-z0-9-]{2,79}")

    fun validate(pack: ContentPack) {
        require(pack.schemaVersion == 1) { "Unsupported content schema" }
        require(pack.version > 0) { "Pack version must be positive" }
        identifier(pack.id)
        text(pack.title)
        text(pack.provenance.author)
        text(pack.provenance.rightsNotice)
        if (pack.provenance.reviewStatus == ReviewStatus.REVIEWED) text(pack.provenance.reviewer.orEmpty())
        require(pack.track != LearningTrack.QURANIC) {
            "Quranic content requires the separately verified source and integrity pipeline"
        }
        require(pack.units.size in 1..50) { "Pack needs 1 to 50 units" }
        unique(pack.units.map { it.id })
        val lessons = pack.units.flatMap { unit ->
            text(unit.title)
            require(unit.lessons.size in 1..50) { "Unit needs 1 to 50 lessons" }
            unit.lessons
        }
        require(lessons.size <= 500) { "Too many lessons" }
        unique(lessons.map { it.id })
        unique(lessons.flatMap { it.exercises }.map { it.id })
        lessons.forEach { lesson -> validateLesson(lesson, pack.track) }
    }

    fun validateLesson(lesson: LessonContent, track: LearningTrack) {
        identifier(lesson.id)
        text(lesson.title)
        require(lesson.objectives.size in 1..8)
        lesson.objectives.forEach(::text)
        text(lesson.explanationRomanUrdu, maximum = 12_000)
        require(lesson.rulesRomanUrdu.size in 1..20)
        lesson.rulesRomanUrdu.forEach(::text)
        require(lesson.examples.size in 1..20)
        lesson.examples.forEach { example ->
            text(example.text)
            text(example.meaningRomanUrdu)
            example.speech?.let { speech ->
                text(speech.text)
                require(speech.text == example.text) { "Spoken example must match displayed text" }
                require(speech.languageTag in setOf("en", "en-US", "en-GB", "ar"))
                require(speech.languageTag.substringBefore('-') == track.languageTag)
            }
        }
        require(lesson.vocabulary.size in 1..50)
        unique(lesson.vocabulary.map { it.id })
        lesson.vocabulary.forEach {
            text(it.word); text(it.meaningRomanUrdu); text(it.partOfSpeech); text(it.pronunciationGuide)
        }
        lesson.reading?.let { text(it.text, 8_000); text(it.meaningRomanUrdu, 8_000) }
        require(lesson.speakingPrompts.size in 1..10)
        lesson.speakingPrompts.forEach(::text)
        require(lesson.reviewTags.size in 1..20)
        lesson.reviewTags.forEach(::identifier)
        require(lesson.exercises.size in 2..30)
        unique(lesson.exercises.map { it.id })
        lesson.exercises.forEach(::validateExercise)
    }

    fun validateExercise(exercise: Exercise) {
        identifier(exercise.id)
        text(exercise.prompt)
        text(exercise.explanationRomanUrdu)
        when (exercise) {
            is Exercise.SingleChoice -> {
                options(exercise.options)
                require(exercise.correctOptionId in exercise.options.map { it.id })
            }
            is Exercise.MultiChoice -> {
                options(exercise.options)
                require(exercise.correctOptionIds.isNotEmpty())
                require(exercise.correctOptionIds.distinct().size == exercise.correctOptionIds.size)
                require(exercise.correctOptionIds.all { answer -> exercise.options.any { it.id == answer } })
            }
            is Exercise.FillBlank -> {
                require(exercise.acceptedAnswers.size in 1..20)
                exercise.acceptedAnswers.forEach(::text)
                require(exercise.acceptedAnswers.map { normalizeAnswer(it, exercise.ignoreCase) }.distinct().size == exercise.acceptedAnswers.size)
            }
            is Exercise.SentenceOrder -> {
                require(exercise.tokens.size in 2..20)
                unique(exercise.tokens.map { it.id })
                exercise.tokens.forEach { text(it.text) }
                require(exercise.correctOrder.size == exercise.tokens.size && exercise.correctOrder.toSet() == exercise.tokens.map { it.id }.toSet())
            }
            is Exercise.MatchPairs -> {
                require(exercise.pairs.size in 2..10)
                unique(exercise.pairs.map { it.id })
                exercise.pairs.forEach { text(it.left); text(it.right) }
                require(exercise.pairs.map { normalizeAnswer(it.left) }.distinct().size == exercise.pairs.size)
                require(exercise.pairs.map { normalizeAnswer(it.right) }.distinct().size == exercise.pairs.size)
            }
        }
    }

    private fun options(options: List<ChoiceOption>) {
        require(options.size in 2..8)
        unique(options.map { it.id })
        options.forEach { text(it.text) }
        require(options.map { normalizeAnswer(it.text) }.distinct().size == options.size)
    }

    private fun unique(ids: List<String>) {
        ids.forEach(::identifier)
        require(ids.distinct().size == ids.size) { "Duplicate stable identifier" }
    }

    private fun identifier(value: String) { require(idPattern.matches(value)) { "Invalid stable identifier" } }
    private fun text(value: String, maximum: Int = 2_000) {
        require(value.isNotBlank() && value.length <= maximum) { "Missing or oversized content text" }
        var index = 0
        while (index < value.length) {
            val character = value[index]
            require(!character.isISOControl() || character in "\n\r\t") { "Invalid control character" }
            if (Character.isHighSurrogate(character)) {
                require(index + 1 < value.length && Character.isLowSurrogate(value[index + 1])) { "Malformed Unicode" }
                index += 1
            } else require(!Character.isLowSurrogate(character)) { "Malformed Unicode" }
            index += 1
        }
    }
}
