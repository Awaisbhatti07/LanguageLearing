package com.lingomate.ai

import android.os.Bundle
import android.graphics.Color as AndroidColor
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.lingomate.ai.core.designsystem.LingoMateTheme
import com.lingomate.ai.core.designsystem.useDarkTheme
import com.lingomate.ai.core.preferences.ThemeMode
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
            val mode = state.settings?.theme ?: ThemeMode.DARK
            val dark = useDarkTheme(mode, isSystemInDarkTheme())
            SideEffect {
                // The selected app theme can differ from the device theme used by the default style.
                val barStyle = SystemBarStyle.auto(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT) { dark }
                enableEdgeToEdge(statusBarStyle = barStyle, navigationBarStyle = barStyle)
            }
            LingoMateTheme(mode = mode) {
                LingoMateRoot(state, model, container.learning)
            }
        }
    }
}
