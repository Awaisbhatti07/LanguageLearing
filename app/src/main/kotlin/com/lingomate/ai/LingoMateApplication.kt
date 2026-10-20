package com.lingomate.ai

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.lingomate.ai.core.database.LingoMateDatabase
import com.lingomate.ai.core.preferences.UserPreferences
import com.lingomate.ai.data.LearningRepository
import com.lingomate.learning.content.ContentCodec

private val Context.settingsStore by preferencesDataStore(name = "user_settings")

class AppContainer(context: Context) {
    val preferences = UserPreferences(context.settingsStore)
    val database: LingoMateDatabase by lazy { LingoMateDatabase.open(context) }
    val learning: LearningRepository by lazy {
        LearningRepository(database, bundledPacks = {
            context.assets.list("content").orEmpty().filter { it.endsWith(".json") }.sorted().map { file ->
                context.assets.open("content/$file").use { stream ->
                    val buffer = ByteArray(ContentCodec.MAX_PACK_BYTES + 1)
                    var count = 0
                    while (count < buffer.size) {
                        val read = stream.read(buffer, count, buffer.size - count)
                        if (read < 0) break
                        count += read
                    }
                    require(count <= ContentCodec.MAX_PACK_BYTES) { "Bundled pack exceeds size limit" }
                    String(buffer, 0, count, Charsets.UTF_8)
                }
            }
        })
    }
}

class LingoMateApplication : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}
