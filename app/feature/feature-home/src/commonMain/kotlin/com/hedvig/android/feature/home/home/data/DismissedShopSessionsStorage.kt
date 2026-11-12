package com.hedvig.android.feature.home.home.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * Remembers, per install, which ongoing shop sessions the member closed on the home screen. A
 * dismissed session stays hidden for as long as the backend keeps returning it.
 */
internal interface DismissedShopSessionsStorage {
  fun observeDismissedSessionIds(): Flow<Set<String>>

  suspend fun dismiss(sessionId: String)
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class DismissedShopSessionsStorageImpl(
  private val dataStore: DataStore<Preferences>,
) : DismissedShopSessionsStorage {
  override fun observeDismissedSessionIds(): Flow<Set<String>> = dataStore.data
    .map { it[KEY].orEmpty() }
    .catch { error ->
      // Home must still render if this local-only preference can't be read.
      logcat(LogPriority.ERROR, error) { "Reading dismissed shop sessions failed; treating none as dismissed" }
      emit(emptySet())
    }

  override suspend fun dismiss(sessionId: String) {
    dataStore.edit { preferences ->
      preferences[KEY] = preferences[KEY].orEmpty() + sessionId
    }
  }

  companion object {
    internal const val KEY_NAME = "com.hedvig.android.feature.home.dismissed_shop_session_ids"
    private val KEY = stringSetPreferencesKey(KEY_NAME)
  }
}
