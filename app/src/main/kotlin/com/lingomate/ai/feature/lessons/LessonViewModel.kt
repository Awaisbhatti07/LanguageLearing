package com.lingomate.ai.feature.lessons

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lingomate.ai.data.LearningRepository
import com.lingomate.ai.data.LessonRun
import com.lingomate.ai.data.SavedParagraph
import com.lingomate.learning.content.Exercise
import com.lingomate.learning.content.ExerciseAnswer
import com.lingomate.learning.content.ListeningMode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class LessonUiState(
    val run: LessonRun? = null,
    val loading: Boolean = true,
    val saving: Boolean = false,
    val failed: Boolean = false,
    val writeFailed: Boolean = false,
    val practice: Boolean = false,
    val feedbackPosition: Int? = null,
    val savedParagraphs: List<SavedParagraph> = emptyList(),
)

class LessonViewModel(
    private val repository: LearningRepository,
    private val lessonId: String,
    private val saved: SavedStateHandle,
) : ViewModel() {
    private val _state = MutableStateFlow(LessonUiState())
    val state: StateFlow<LessonUiState> = _state
    init { load() }

    fun load() {
        if (_state.value.saving) return
        _state.value = _state.value.copy(loading = true, failed = false)
        viewModelScope.launch {
            try {
                val savedSession = saved.get<String>("session_id")
                val run = if (savedSession != null) repository.run(savedSession) else repository.beginOrResume(lessonId)
                saved["session_id"] = run.session.id
                val practice = saved.get<Boolean>("practice") == true || run.attempts.isNotEmpty()
                val paragraphs = run.content.exercises.filterIsInstance<Exercise.ParagraphWriting>().mapNotNull {
                    repository.latestParagraph(lessonId, it.id)
                }
                _state.value = LessonUiState(run, loading = false, practice = practice,
                    feedbackPosition = if (practice && !run.complete) run.attempts.lastIndex.takeIf { it >= 0 } else null,
                    savedParagraphs = paragraphs)
            } catch (error: CancellationException) { throw error }
            catch (_: Exception) { _state.value = _state.value.copy(loading = false, failed = true) }
        }
    }

    fun startPractice() {
        saved["practice"] = true
        _state.value = _state.value.copy(practice = true)
    }

    fun next() { _state.value = _state.value.copy(feedbackPosition = null) }
    fun dismissWriteError() { _state.value = _state.value.copy(writeFailed = false) }

    fun prepareListening(exerciseId: String, mode: ListeningMode) {
        val current = _state.value
        val run = current.run ?: return
        if (current.loading || current.saving || current.feedbackPosition != null || run.complete) return
        _state.value = current.copy(saving = true, writeFailed = false)
        viewModelScope.launch {
            try {
                val updated = repository.prepareListening(run.session.id, exerciseId, mode)
                _state.value = current.copy(run = updated, saving = false)
            } catch (error: CancellationException) { throw error }
            catch (_: Exception) { _state.value = current.copy(saving = false, writeFailed = true) }
        }
    }

    fun submit(exerciseId: String, answer: ExerciseAnswer) {
        val current = _state.value
        val run = current.run ?: return
        if (current.loading || current.saving || current.feedbackPosition != null || run.complete) return
        _state.value = current.copy(saving = true, writeFailed = false)
        viewModelScope.launch {
            try {
                val updated = repository.submit(run.session.id, exerciseId, answer)
                _state.value = current.copy(run = updated, saving = false,
                    feedbackPosition = updated.attempts.indexOfFirst { it.exerciseId == exerciseId }.takeIf { it >= 0 })
            } catch (error: CancellationException) { throw error }
            catch (_: Exception) { _state.value = current.copy(saving = false, writeFailed = true) }
        }
    }
}
