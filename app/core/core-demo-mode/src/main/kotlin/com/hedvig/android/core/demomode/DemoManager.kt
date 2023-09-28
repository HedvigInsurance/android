package com.hedvig.android.core.demomode

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

interface DemoManager {
  suspend fun isDemoMode(): Boolean
  suspend fun setDemoMode(demoMode: Boolean)
}

class DataStoreDemoManager(
  private val dataStore: DataStore<Preferences>,
) : DemoManager {

  override suspend fun isDemoMode(): Boolean {
    return dataStore.data.map {
      it[demoModeKey] ?: false
    }.first()
  }

  override suspend fun setDemoMode(demoMode: Boolean) {
    dataStore.edit {
      it[demoModeKey] = demoMode
    }
  }

  companion object {
    private val demoModeKey = booleanPreferencesKey("demo-mode")
  }
}
