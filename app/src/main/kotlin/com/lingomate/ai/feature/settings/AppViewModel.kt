package com.lingomate.ai.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lingomate.ai.core.preferences.ThemeMode
import com.lingomate.ai.core.preferences.UserPreferences
import com.lingomate.ai.core.preferences.UserSettings
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AppState(val settings: UserSettings? = null, val loadError: Boolean = false)

@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModel(private val preferences: UserPreferences) : ViewModel() {
    private val reload = MutableStateFlow(0)
    @Suppress("EXPERIMENTAL_API_USAGE")
    val state = reload.flatMapLatest {
        preferences.settings.map { AppState(settings = it) }
            .onStart { emit(AppState()) }
            .catch { error ->
                if (error is CancellationException) throw error
                emit(AppState(loadError = true))
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppState())

    private val _writeError = MutableStateFlow(false)
    val writeError: StateFlow<Boolean> = _writeError
    private val _saving = MutableStateFlow(false)
    val saving: StateFlow<Boolean> = _saving

    fun retry() { reload.value += 1 }
    fun dismissError() { _writeError.value = false }
    fun setTheme(theme: ThemeMode) = save { preferences.setTheme(theme) }
    fun setGoal(goal: Int) = save { preferences.setDailyGoal(goal) }
    fun finishOnboarding(goal: Int) = save { preferences.completeOnboarding(goal) }

    private fun save(block: suspend () -> Unit) {
        if (_saving.value) return
        _saving.value = true
        viewModelScope.launch {
            try {
                block()
            } catch (error: IOException) {
                _writeError.value = true
            } finally {
                _saving.value = false
            }
        }
    }
}
