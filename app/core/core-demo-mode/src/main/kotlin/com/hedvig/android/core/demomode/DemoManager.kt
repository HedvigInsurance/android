package com.hedvig.android.core.demomode

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

interface DemoManager {
  fun isDemoMode(): Flow<Boolean>

  suspend fun setDemoMode(demoMode: Boolean)
}

internal class DataStoreDemoManager(
  private val dataStore: DataStore<Preferences>,
) : DemoManager {
  override fun isDemoMode(): Flow<Boolean> {
    return dataStore.data.map {
      it[demoModeKey] ?: false
    }.distinctUntilChanged()
  }

  override suspend fun setDemoMode(demoMode: Boolean) {
    dataStore.edit {
      it[demoModeKey] = demoMode
    }
  }

  companion object {
    private val demoModeKey = booleanPreferencesKey("com.hedvig.android.core.demomode.demo-mode")
  }
}
