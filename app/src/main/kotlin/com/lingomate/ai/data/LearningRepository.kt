package com.lingomate.ai.data

import androidx.room.withTransaction
import com.lingomate.ai.core.database.*
import com.lingomate.learning.LearningTrack
import com.lingomate.learning.content.*
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext

data class LearningCatalog(
    val lessons: List<LessonEntity> = emptyList(),
    val progress: List<LessonProgressEntity> = emptyList(),
    val sessions: List<LessonSessionEntity> = emptyList(),
) {
    fun completed(track: LearningTrack): Int {
        val ids = lessons.filter { it.track == track.name }.map { it.id }.toSet()
        return progress.count { it.lessonId in ids && it.completedAt != null }
    }
}

data class LessonRun(
    val session: LessonSessionEntity,
    val content: LessonContent,
    val attempts: List<ExerciseAttemptEntity>,
    val listeningModes: Map<String, ListeningMode> = emptyMap(),
) {
    val complete: Boolean get() = session.completedAt != null
    val correctCount: Int get() = attempts.count { it.correct }
    val scoredQuestionCount: Int get() = content.exercises.count { it.isScored }
    fun answeredIn(mode: ListeningMode): Int = attempts.count { listeningModes[it.exerciseId] == mode }
}

data class SavedParagraph(
    val exercise: Exercise.ParagraphWriting,
    val answer: ExerciseAnswer.ParagraphText,
    val savedAt: Long,
)

class LearningRepository(
    private val database: LingoMateDatabase,
    private val bundledPacks: () -> List<String>,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    private val dao get() = database.learningDao()
    val catalog: Flow<LearningCatalog> = combine(dao.observeLessons(), dao.observeProgress(), dao.observeSessions(), ::LearningCatalog)

    suspend fun installBundledContent() = withContext(Dispatchers.IO) {
        // Validate every bundled pack before the first write; installation is one transaction.
        val packs = bundledPacks().map(ContentCodec::parsePack)
        require(packs.map { it.id }.distinct().size == packs.size) { "Duplicate content pack" }
        database.withTransaction { packs.forEach { importValidated(it) } }
    }

    suspend fun importPack(source: String) = withContext(Dispatchers.IO) {
        val pack = ContentCodec.parsePack(source)
        database.withTransaction { importValidated(pack) }
    }

    private suspend fun importValidated(pack: ContentPack) {
        val existing = dao.pack(pack.id)
        if (existing != null && existing.version >= pack.version) return
        val previous = dao.packLessons(pack.id)
        val newIds = pack.units.flatMap { it.lessons }.map { it.id }.toSet()
        // Removing an old ID would strand progress. Future explicit retirement must preserve it.
        require(previous.all { it.id in newIds }) { "Pack update cannot remove existing lesson identifiers" }
        var position = 0
        val rows = pack.units.flatMap { unit -> unit.lessons.map { lesson ->
            val old = dao.lesson(lesson.id)
            require(old == null || old.packId == pack.id) { "Lesson identifier belongs to another pack" }
            require(old == null || (old.track == pack.track.name && old.level == pack.level.name)) {
                "Existing lesson track and level cannot change"
            }
            LessonEntity(
                id = lesson.id, packId = pack.id, track = pack.track.name, level = pack.level.name,
                position = position++, title = lesson.title,
                payload = ContentCodec.json.encodeToString(lesson), unitId = unit.id, unitTitle = unit.title,
                reviewStatus = pack.provenance.reviewStatus.name,
            )
        } }
        dao.upsertPack(ContentPackEntity(pack.id, pack.version, pack.schemaVersion,
            ContentCodec.json.encodeToString(pack.provenance), clock()))
        dao.upsertLessons(rows)
    }

    suspend fun lesson(id: String): LessonContent = withContext(Dispatchers.IO) {
        val row = requireNotNull(dao.lesson(id)) { "Lesson is unavailable" }
        ContentCodec.json.decodeFromString<LessonContent>(row.payload).also {
            ContentValidator.validateLesson(it, LearningTrack.valueOf(row.track))
        }
    }

    suspend fun beginOrResume(lessonId: String): LessonRun = withContext(Dispatchers.IO) {
        database.withTransaction {
            dao.activeSession(lessonId)?.let { return@withTransaction readRun(it.id) }
            val row = requireNotNull(dao.lesson(lessonId)) { "Lesson is unavailable" }
            val pack = requireNotNull(dao.pack(row.packId))
            val content = ContentCodec.json.decodeFromString<LessonContent>(row.payload)
            ContentValidator.validateLesson(content, LearningTrack.valueOf(row.track))
            val session = LessonSessionEntity(UUID.randomUUID().toString(), lessonId, row.payload,
                pack.version, row.track, clock(), null, lessonId)
            dao.insertSession(session)
            LessonRun(session, content, emptyList())
        }
    }

    suspend fun run(id: String): LessonRun = withContext(Dispatchers.IO) { database.withTransaction { readRun(id) } }

    suspend fun latestParagraph(lessonId: String, exerciseId: String): SavedParagraph? = withContext(Dispatchers.IO) {
        database.withTransaction {
            val attempt = dao.latestAttempt(lessonId, exerciseId) ?: return@withTransaction null
            val original = readRun(attempt.sessionId)
            val exercise = original.content.exercises.first { it.id == attempt.exerciseId } as? Exercise.ParagraphWriting
                ?: return@withTransaction null
            val answer = ContentCodec.json.decodeFromString<ExerciseAnswer>(attempt.answerPayload) as? ExerciseAnswer.ParagraphText
                ?: return@withTransaction null
            // Display the original prompt/checklist, never reinterpret old writing under new content.
            SavedParagraph(exercise, answer, attempt.answeredAt)
        }
    }

    /** Persist exposure before showing a transcript. Reading mode is irreversible within this run. */
    suspend fun prepareListening(sessionId: String, exerciseId: String, mode: ListeningMode): LessonRun = withContext(Dispatchers.IO) {
        database.withTransaction {
            val run = readRun(sessionId)
            require(!run.complete && run.content.exercises.getOrNull(run.attempts.size)?.id == exerciseId) {
                "Listening activity is not current"
            }
            require(run.content.exercises[run.attempts.size] is Exercise.ListeningChoice)
            val persisted = run.listeningModes[exerciseId]
            if (persisted != ListeningMode.READING_ALTERNATIVE) {
                dao.upsertListeningPresentation(ListeningPresentationEntity(sessionId, exerciseId, mode.name))
            }
            readRun(sessionId)
        }
    }

    suspend fun submit(sessionId: String, exerciseId: String, answer: ExerciseAnswer): LessonRun = withContext(Dispatchers.IO) {
        database.withTransaction {
            val run = readRun(sessionId)
            // Same persisted answer is authoritative on retry, even after process/network-style interruption.
            if (run.attempts.any { it.exerciseId == exerciseId }) return@withTransaction run
            require(!run.complete) { "Session is already complete" }
            val exercise = run.content.exercises.getOrNull(run.attempts.size)
            require(exercise?.id == exerciseId) { "Answer is out of sequence" }
            if (exercise is Exercise.ListeningChoice) {
                require(answer is ExerciseAnswer.ListeningSelection && run.listeningModes[exerciseId] == answer.mode) {
                    "Complete playback or choose the reading alternative before answering"
                }
            }
            val grade = ExerciseGrader.grade(requireNotNull(exercise), answer)
            require(grade.valid) { "Select or enter a complete valid answer" }
            val now = clock()
            dao.insertAttempt(ExerciseAttemptEntity(sessionId, exerciseId, run.attempts.size,
                ContentCodec.json.encodeToString<ExerciseAnswer>(answer), grade.scored && grade.correct, now))
            if (run.attempts.size + 1 == run.content.exercises.size) {
                check(dao.completeSession(sessionId, now) == 1)
                val count = run.correctCount + if (grade.scored && grade.correct) 1 else 0
                val total = run.scoredQuestionCount
                val old = dao.progress(run.session.lessonId)
                val improved = old == null || old.questionCount <= 0 || count * old.questionCount > old.bestCorrect * total
                dao.upsertProgress(LessonProgressEntity(
                    lessonId = run.session.lessonId, lastStudiedAt = now,
                    completedAt = old?.completedAt ?: now,
                    bestCorrect = if (improved) count else requireNotNull(old).bestCorrect,
                    questionCount = if (improved) total else requireNotNull(old).questionCount,
                    completedRuns = (old?.completedRuns ?: 0) + 1,
                ))
            }
            readRun(sessionId)
        }
    }

    private suspend fun readRun(id: String): LessonRun {
        val session = requireNotNull(dao.session(id)) { "Practice session is unavailable" }
        val content = ContentCodec.json.decodeFromString<LessonContent>(session.contentPayload)
        ContentValidator.validateLesson(content, LearningTrack.valueOf(session.track))
        val attempts = dao.attempts(id)
        check(attempts.map { it.exerciseId } == content.exercises.take(attempts.size).map { it.id }) { "Invalid saved answer sequence" }
        val modes = dao.listeningPresentations(id).associate { it.exerciseId to ListeningMode.valueOf(it.mode) }
        val listeningIds = content.exercises.filterIsInstance<Exercise.ListeningChoice>().map { it.id }.toSet()
        check(modes.keys.all { it in listeningIds }) { "Invalid saved listening activity" }
        attempts.forEach { attempt ->
            val answer = ContentCodec.json.decodeFromString<ExerciseAnswer>(attempt.answerPayload)
            if (attempt.exerciseId in listeningIds) {
                check(answer is ExerciseAnswer.ListeningSelection && answer.mode == modes[attempt.exerciseId]) {
                    "Saved listening answer does not match its presentation"
                }
            }
        }
        return LessonRun(session, content, attempts, modes)
    }
}
