package com.hedvig.android.core.appreview

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

internal interface SelfServiceCompletedEventStore {
  suspend fun onSelfServiceCompleted()

  suspend fun resetSelfServiceCompletions()

  fun observeNumberOfCompletedSelfServices(): Flow<Int>
}

internal class SelfServiceCompletedEventDataStore(
  private val dataStore: DataStore<Preferences>,
) : SelfServiceCompletedEventStore {
  override suspend fun onSelfServiceCompleted() {
    dataStore.edit {
      it[SELF_SERVE_COMPLETED_COUNTER] = (it[SELF_SERVE_COMPLETED_COUNTER] ?: 0) + 1
    }
  }

  override suspend fun resetSelfServiceCompletions() {
    dataStore.edit {
      it[SELF_SERVE_COMPLETED_COUNTER] = 0
    }
  }

  override fun observeNumberOfCompletedSelfServices() = dataStore.data
    .catch { logcat(LogPriority.ERROR, it) { "observeChatClosedCounter failed" } }
    .map { it[SELF_SERVE_COMPLETED_COUNTER] ?: 0 }

  companion object {
    private val SELF_SERVE_COMPLETED_COUNTER =
      intPreferencesKey("com.hedvig.android.core.appreview.SELF_SERVE_COMPLETED_COUNTER")
  }
}
