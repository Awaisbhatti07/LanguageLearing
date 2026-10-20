package com.lingomate.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.lingomate.ai.core.designsystem.LingoMateTheme
import com.lingomate.ai.core.navigation.LingoMateRoot
import com.lingomate.ai.feature.settings.AppViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as LingoMateApplication).container
        setContent {
            val model: AppViewModel = viewModel(factory = viewModelFactory {
                initializer { AppViewModel(container.preferences) }
            })
            val state by model.state.collectAsStateWithLifecycle()
            LingoMateTheme(mode = state.settings?.theme ?: com.lingomate.ai.core.preferences.ThemeMode.DARK) {
                LingoMateRoot(state, model)
            }
        }
    }
}
