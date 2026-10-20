package com.lingomate.ai

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.lifecycle.ViewModelStore
import com.lingomate.ai.core.preferences.UserPreferences
import com.lingomate.ai.feature.settings.AppViewModel
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModelTest {
    private val models = ViewModelStore()
    @Before fun prepare() { Dispatchers.setMain(StandardTestDispatcher()) }
    @After fun close() { models.clear(); Dispatchers.resetMain() }

    @Test fun failedSettingsReadCanBeRetriedWithoutResettingTheStore() = runTest {
        val store = FailingStore().apply { failReads = true }
        val model = AppViewModel(UserPreferences(store)).also { models.put("app", it) }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { model.state.collect() }
        runCurrent()
        assertTrue(model.state.value.loadError)
        assertEquals(0, store.writes)
        store.failReads = false
        model.retry()
        runCurrent()
        assertNotNull(model.state.value.settings)
        assertFalse(model.state.value.loadError)
        assertEquals(0, store.writes)
    }

    @Test fun failedWritePreservesSettingsAndAllowsRetry() = runTest {
        val store = FailingStore().apply { failWrites = true }
        val preferences = UserPreferences(store)
        val model = AppViewModel(preferences).also { models.put("app", it) }
        model.setGoal(5)
        runCurrent()
        assertTrue(model.writeError.value)
        assertFalse(model.saving.value)
        assertEquals(3, preferences.settings.first().dailyGoal)
        store.failWrites = false
        model.dismissError()
        model.setGoal(5)
        runCurrent()
        assertEquals(5, preferences.settings.first().dailyGoal)
        assertFalse(model.writeError.value)
    }

    private class FailingStore : DataStore<Preferences> {
        var failReads = false
        var failWrites = false
        var writes = 0
        private val values = MutableStateFlow(emptyPreferences())
        override val data: Flow<Preferences> = flow {
            if (failReads) throw IOException("Simulated read failure")
            emitAll(values)
        }
        override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences {
            if (failWrites) throw IOException("Simulated write failure")
            writes += 1
            return transform(values.value).also { values.value = it }
        }
    }
}
