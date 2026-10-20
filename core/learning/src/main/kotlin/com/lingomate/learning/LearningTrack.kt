package com.lingomate.learning

import kotlinx.serialization.Serializable

@Serializable
enum class LearningTrack(val title: String, val languageTag: String) {
    ENGLISH("English", "en"),
    FUSHA("Arabic Fusha", "ar"),
    QURANIC("Quranic Arabic", "ar"),
}

@Serializable
enum class CefrLevel { A0, A1, A2, B1 }
