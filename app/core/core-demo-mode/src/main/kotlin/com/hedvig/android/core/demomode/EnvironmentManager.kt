package com.hedvig.android.core.demomode

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.hedvig.android.logger.logcat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

/**
 * Manages the environment preference for staging builds.
 * Allows switching between staging and production environments at runtime.
 * Only functional in staging builds - production builds always use production environment.
 */
interface EnvironmentManager {
  /**
   * Observes whether production environment is selected.
   * Defaults to false (staging) if no preference has been set.
   */
  fun isProductionEnvironment(): Flow<Boolean>

  /**
   * Sets the environment preference.
   * App restart is required for changes to take effect.
   */
  suspend fun setProductionEnvironment(isProduction: Boolean)
}

internal class DataStoreEnvironmentManager(
  private val dataStore: DataStore<Preferences>,
) : EnvironmentManager {
  override fun isProductionEnvironment(): Flow<Boolean> {
    return dataStore.data.map {
      it[environmentKey] ?: false
    }.distinctUntilChanged().onEach {
      logcat { "EnvironmentManager: isProductionEnvironment:$it" }
    }
  }

  override suspend fun setProductionEnvironment(isProduction: Boolean) {
    logcat { "EnvironmentManager: setProductionEnvironment:$isProduction" }
    dataStore.edit {
      it[environmentKey] = isProduction
    }
  }

  companion object {
    private val environmentKey = booleanPreferencesKey("com.hedvig.android.core.demomode.is-production-environment")
  }
}
