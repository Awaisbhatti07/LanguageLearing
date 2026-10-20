package com.lingomate.ai.feature.courses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lingomate.ai.data.LearningCatalog
import com.lingomate.ai.data.LearningRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class LearningUiState(val catalog: LearningCatalog = LearningCatalog(), val loading: Boolean = true, val failed: Boolean = false)

class LearningViewModel(private val repository: LearningRepository) : ViewModel() {
    private val _state = MutableStateFlow(LearningUiState())
    val state: StateFlow<LearningUiState> = _state
    private var job: Job? = null

    init { retry() }

    fun retry() {
        job?.cancel()
        _state.value = _state.value.copy(loading = true, failed = false)
        job = viewModelScope.launch {
            try {
                repository.installBundledContent()
                repository.catalog.collect { _state.value = LearningUiState(it, loading = false) }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                _state.value = _state.value.copy(loading = false, failed = true)
            }
        }
    }
}
