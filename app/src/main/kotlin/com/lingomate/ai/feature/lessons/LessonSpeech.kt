package com.lingomate.ai.feature.lessons

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.lingomate.ai.core.audio.DeviceSpeechPlayer
import com.lingomate.ai.core.audio.SpeechPlayer

@Composable
internal fun rememberLessonSpeechPlayer(): SpeechPlayer {
    val context = LocalContext.current
    val player = remember(context) { DeviceSpeechPlayer(context) }
    DisposableEffect(player) { onDispose { player.close() } }
    return player
}

@Composable
internal fun StopSpeechWhenInactive(player: SpeechPlayer, contentKey: String? = null) {
    val owner = LocalLifecycleOwner.current
    DisposableEffect(player, owner, contentKey) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_STOP) player.stop() }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer); player.stop() }
    }
}
