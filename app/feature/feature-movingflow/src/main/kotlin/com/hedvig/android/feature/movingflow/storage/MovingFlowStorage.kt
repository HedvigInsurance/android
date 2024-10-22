package com.hedvig.android.feature.movingflow.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.hedvig.android.feature.movingflow.data.MovingFlowState
import kotlinx.coroutines.flow.Flow
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
   * Does nothing if there was no moving flow state to edit
   */
  suspend inline fun editMovingFlowState(crossinline block: (MovingFlowState?) -> MovingFlowState?) {
    dataStore.edit { preferences ->
      val oldState = preferences[movingFlowStateKey]?.let { Json.decodeFromString<MovingFlowState>(it) }
      val newState = block(oldState)
      if (newState != null) {
        preferences[movingFlowStateKey] = Json.encodeToString(newState)
      }
    }
  }

  companion object {
    private val movingFlowStateKey =
      stringPreferencesKey("com.hedvig.android.feature.movingflow.storage.movingFlowStateKey")
  }
}
