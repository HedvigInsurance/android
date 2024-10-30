package com.hedvig.android.feature.movingflow.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.hedvig.android.feature.movingflow.data.MovingFlowState
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal class MovingFlowStorage(
  private val dataStore: DataStore<Preferences>,
) {
  fun getMovingFlowState(): Flow<MovingFlowState?> {
    return dataStore.data.map { preferences ->
      preferences[movingFlowStateKey]?.let { Json.decodeFromString<MovingFlowState>(it) }
    }
  }

  suspend fun setMovingFlowState(movingFlowState: MovingFlowState) {
    dataStore.edit { preferences ->
      preferences[movingFlowStateKey] = Json.encodeToString(movingFlowState)
    }
  }

  /**
   * Does nothing if there was no moving flow state to edit.
   * Returns the edited [MovingFlowState]
   */
  suspend inline fun editMovingFlowState(crossinline block: (MovingFlowState) -> MovingFlowState): MovingFlowState? {
    dataStore.edit { preferences ->
      val oldState = preferences[movingFlowStateKey]?.let { Json.decodeFromString<MovingFlowState>(it) }
      if (oldState != null) {
        val newState = block(oldState)
        preferences[movingFlowStateKey] = Json.encodeToString(newState)
      } else {
        logcat(LogPriority.ERROR) { "Trying to `editMovingFlowState` a non-existing moving flow state" }
      }
    }
    return getMovingFlowState().first()
  }

  companion object {
    private val movingFlowStateKey =
      stringPreferencesKey("com.hedvig.android.feature.movingflow.storage.movingFlowStateKey")
  }
}
