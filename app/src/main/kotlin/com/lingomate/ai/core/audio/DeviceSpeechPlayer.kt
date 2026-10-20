package com.lingomate.ai.core.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class SpeechState(val initializing: Boolean = true, val playing: Boolean = false, val message: String? = null)

interface SpeechPlayer : AutoCloseable {
    val state: StateFlow<SpeechState>
    fun speak(text: String, languageTag: String)
    fun stop()
}

/** Uses only an installed offline voice; no network permission or paid provider is required. */
class DeviceSpeechPlayer(context: Context) : SpeechPlayer {
    private val _state = MutableStateFlow(SpeechState())
    override val state: StateFlow<SpeechState> = _state
    private var closed = false
    private var engine: TextToSpeech? = TextToSpeech(context.applicationContext) { status ->
        if (!closed) _state.value = if (status == TextToSpeech.SUCCESS) SpeechState(initializing = false)
        else SpeechState(initializing = false, message = "Speech playback is unavailable on this device.")
    }

    init {
        engine?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) { if (!closed) _state.value = SpeechState(initializing = false, playing = true) }
            override fun onDone(utteranceId: String?) { if (!closed) _state.value = SpeechState(initializing = false) }
            @Deprecated("Platform callback")
            override fun onError(utteranceId: String?) { onError(utteranceId, TextToSpeech.ERROR) }
            override fun onError(utteranceId: String?, errorCode: Int) {
                if (!closed) _state.value = SpeechState(initializing = false, message = "Speech playback failed. Try another installed offline voice.")
            }
        })
    }

    override fun speak(text: String, languageTag: String) {
        if (closed || _state.value.initializing || text.isBlank()) return
        val tts = engine ?: return
        try {
            val language = Locale.forLanguageTag(languageTag)
            val voices = tts.voices.orEmpty().filter {
                !it.isNetworkConnectionRequired && it.locale.language == language.language &&
                    "notInstalled" !in it.features.orEmpty()
            }
            val voice = voices.sortedBy { it.name }.firstOrNull { it.locale.toLanguageTag() == languageTag }
                ?: voices.sortedBy { it.name }.firstOrNull()
            if (voice == null) {
                _state.value = SpeechState(initializing = false, message = "No installed offline voice for this language. Reading and practice still work without audio.")
                return
            }
            stop()
            if (tts.setVoice(voice) != TextToSpeech.SUCCESS ||
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "lesson-example") != TextToSpeech.SUCCESS) {
                _state.value = SpeechState(initializing = false, message = "This offline voice could not play the example.")
            }
        } catch (_: RuntimeException) {
            _state.value = SpeechState(initializing = false, message = "Speech service unavailable. You can keep learning without audio.")
        }
    }

    override fun stop() {
        engine?.stop()
        if (!closed) _state.value = _state.value.copy(playing = false)
    }

    override fun close() {
        closed = true
        engine?.stop()
        engine?.shutdown()
        engine = null
    }
}
