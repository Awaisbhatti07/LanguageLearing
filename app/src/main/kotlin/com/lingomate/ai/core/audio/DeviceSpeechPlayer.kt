package com.lingomate.ai.core.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class SpeechState(
    val initializing: Boolean = true,
    val playing: Boolean = false,
    val message: String? = null,
    val requestId: String? = null,
    val completedRequestId: String? = null,
)

interface SpeechPlayer : AutoCloseable {
    val state: StateFlow<SpeechState>
    /** Returns the unique playback request, or null when playback could not start. */
    fun speak(text: String, languageTag: String): String?
    fun stop()
}

/** Only installed offline voices. Completion is tied to a request, never an old/partial playback. */
class DeviceSpeechPlayer(context: Context) : SpeechPlayer {
    private val lock = Any()
    private val _state = MutableStateFlow(SpeechState())
    override val state: StateFlow<SpeechState> = _state
    private var closed = false
    private var ready = false
    private var activeRequest: String? = null
    private var engine: TextToSpeech? = null

    init {
        try {
            engine = TextToSpeech(context.applicationContext) { status ->
                synchronized(lock) {
                    if (!closed) {
                        ready = status == TextToSpeech.SUCCESS
                        _state.value = SpeechState(initializing = false,
                            message = if (ready) null else "Speech playback is unavailable on this device.")
                    }
                }
            }
            val listenerStatus = engine?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    synchronized(lock) {
                        if (!closed && utteranceId != null && utteranceId == activeRequest) {
                            _state.value = SpeechState(initializing = false, playing = true, requestId = utteranceId)
                        }
                    }
                }
                override fun onDone(utteranceId: String?) { finish(utteranceId, completed = true) }
                @Deprecated("Platform callback")
                override fun onError(utteranceId: String?) { onError(utteranceId, TextToSpeech.ERROR) }
                override fun onError(utteranceId: String?, errorCode: Int) {
                    finish(utteranceId, message = "Speech playback failed. Try another installed offline voice.")
                }
                override fun onStop(utteranceId: String?, interrupted: Boolean) {
                    finish(utteranceId, message = "Playback stopped before it finished. Try playing the audio again.")
                }
            })
            check(listenerStatus == TextToSpeech.SUCCESS) { "Playback listener is unavailable" }
        } catch (_: RuntimeException) {
            close()
            _state.value = SpeechState(initializing = false, message = "Speech playback is unavailable on this device.")
        }
    }

    private fun finish(request: String?, completed: Boolean = false, message: String? = null) = synchronized(lock) {
        if (!closed && request != null && request == activeRequest) {
            activeRequest = null
            _state.value = SpeechState(initializing = false, message = message,
                completedRequestId = request.takeIf { completed })
        }
    }

    override fun speak(text: String, languageTag: String): String? = synchronized(lock) {
        if (closed || !ready || text.isBlank()) return@synchronized null
        val tts = engine ?: return@synchronized null
        try {
            // Invalidate first: an old stop callback must not change the new request's state.
            activeRequest = null
            tts.stop()
            val language = Locale.forLanguageTag(languageTag)
            val voices = tts.voices.orEmpty().filter {
                !it.isNetworkConnectionRequired && it.locale.language == language.language &&
                    "notInstalled" !in it.features.orEmpty()
            }.sortedBy { it.name }
            val voice = voices.firstOrNull { it.locale.toLanguageTag() == languageTag } ?: voices.firstOrNull()
            if (voice == null) {
                _state.value = SpeechState(initializing = false,
                    message = "No installed offline voice for this language.")
                return@synchronized null
            }
            val request = UUID.randomUUID().toString()
            activeRequest = request
            _state.value = SpeechState(initializing = false, playing = true, requestId = request)
            if (tts.setVoice(voice) != TextToSpeech.SUCCESS ||
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, request) != TextToSpeech.SUCCESS) {
                activeRequest = null
                _state.value = SpeechState(initializing = false, message = "This offline voice could not play the audio.")
                return@synchronized null
            }
            request
        } catch (_: RuntimeException) {
            activeRequest = null
            _state.value = SpeechState(initializing = false,
                message = "Speech service unavailable. Try again when the service is available.")
            null
        }
    }

    override fun stop() = synchronized(lock) {
        activeRequest = null
        try { engine?.stop() } catch (_: RuntimeException) { /* A missing service must not break navigation. */ }
        if (!closed) _state.value = _state.value.copy(playing = false, requestId = null, completedRequestId = null)
    }

    override fun close() = synchronized(lock) {
        closed = true
        ready = false
        activeRequest = null
        try { engine?.stop() } catch (_: RuntimeException) { }
        try { engine?.shutdown() } catch (_: RuntimeException) { }
        engine = null
    }
}
