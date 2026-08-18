package com.hedvig.android.core.demomode

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

interface DemoManager {
  fun isDemoMode(): Flow<Boolean>

  suspend fun setDemoMode(demoMode: Boolean)
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class DataStoreDemoManager(
  private val dataStore: DataStore<Preferences>,
) : DemoManager {
  override fun isDemoMode(): Flow<Boolean> {
    return dataStore.data.map {
      it[demoModeKey] ?: false
    }.distinctUntilChanged().onEach {
      logcat(LogPriority.DEBUG) { "DemoManager: isDemoMode:$it" }
    }
  }

  override suspend fun setDemoMode(demoMode: Boolean) {
    logcat { "DemoManager: setDemoMode:$demoMode" }
    dataStore.edit {
      it[demoModeKey] = demoMode
    }
  }

  companion object {
    private val demoModeKey = booleanPreferencesKey("com.hedvig.android.core.demomode.demo-mode")
  }
}
