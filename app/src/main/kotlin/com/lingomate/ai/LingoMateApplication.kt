package com.lingomate.ai

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.lingomate.ai.core.database.LingoMateDatabase
import com.lingomate.ai.core.preferences.UserPreferences

private val Context.settingsStore by preferencesDataStore(name = "user_settings")

class AppContainer(context: Context) {
    val preferences = UserPreferences(context.settingsStore)
    val database: LingoMateDatabase by lazy { LingoMateDatabase.open(context) }
}

class LingoMateApplication : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}
