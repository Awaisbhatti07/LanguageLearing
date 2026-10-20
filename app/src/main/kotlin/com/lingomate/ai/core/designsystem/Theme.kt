package com.lingomate.ai.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.lingomate.ai.core.preferences.ThemeMode

private val DarkColors = darkColorScheme(
    primary = Color(0xFFB7E87C), onPrimary = Color(0xFF193800),
    primaryContainer = Color(0xFF2E4720), onPrimaryContainer = Color(0xFFD3F7B1),
    secondary = Color(0xFFB8CCC0), background = Color(0xFF101410),
    surface = Color(0xFF101410), surfaceContainer = Color(0xFF1B211B),
    surfaceContainerHigh = Color(0xFF252C24), onSurface = Color(0xFFE2E8DD),
    onSurfaceVariant = Color(0xFFC1C9BA), outline = Color(0xFF8C9586),
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF426821), onPrimary = Color.White,
    primaryContainer = Color(0xFFD3F7B1), onPrimaryContainer = Color(0xFF102000),
    secondary = Color(0xFF526356), background = Color(0xFFF7FAF1),
    surface = Color(0xFFF7FAF1), surfaceContainer = Color(0xFFEBEFE5),
    surfaceContainerHigh = Color(0xFFE5E9DF), onSurface = Color(0xFF191D17),
    onSurfaceVariant = Color(0xFF454C40), outline = Color(0xFF747D6C),
)

fun useDarkTheme(mode: ThemeMode, systemDark: Boolean): Boolean = when (mode) {
    ThemeMode.DARK -> true
    ThemeMode.LIGHT -> false
    ThemeMode.SYSTEM -> systemDark
}

@Composable
fun LingoMateTheme(mode: ThemeMode = ThemeMode.DARK, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (useDarkTheme(mode, isSystemInDarkTheme())) DarkColors else LightColors,
        content = content,
    )
}
