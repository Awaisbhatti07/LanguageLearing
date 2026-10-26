package com.lingomate.ai.feature.lessons

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection

/** Direction belongs to the learning text, while controls and Roman Urdu keep their English layout. */
@Composable
internal fun LearningText(
    text: String,
    languageTag: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
) {
    val arabic = languageTag.substringBefore('-') == "ar"
    Text(text, modifier = modifier.fillMaxWidth(), style = style.copy(
        textDirection = if (arabic) TextDirection.Rtl else TextDirection.Ltr,
        textAlign = if (arabic) TextAlign.Right else TextAlign.Left,
    ))
}
