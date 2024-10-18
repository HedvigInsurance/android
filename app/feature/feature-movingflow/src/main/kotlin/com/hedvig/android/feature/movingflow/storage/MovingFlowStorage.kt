package com.hedvig.android.feature.movingflow.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.hedvig.android.feature.movingflow.data.HousingType
import com.hedvig.android.feature.movingflow.data.MovingFlowState
import com.hedvig.android.feature.movingflow.data.fromFragments
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import octopus.feature.movingflow.fragment.MoveIntentFragment
import octopus.feature.movingflow.fragment.MoveIntentQuotesFragment

internal class MovingFlowStorage(
  private val dataStore: DataStore<Preferences>,
) {
  fun movingFlowState(): Flow<MovingFlowState?> {
    return dataStore.data.map { preferences ->
      preferences[movingFlowStateKey]?.let { Json.decodeFromString<MovingFlowState>(it) }
    }
  }

  suspend fun initiateNewMovingFlow(moveIntent: MoveIntentFragment, housingType: HousingType) {
    dataStore.edit { preferences ->
      val movingFlowState = MovingFlowState.fromFragments(moveIntent, null, housingType)
      preferences[movingFlowStateKey] = Json.encodeToString(movingFlowState)
    }
  }

  suspend fun updateMoveIntentAfterRequest(
    moveIntentFragment: MoveIntentFragment,
    moveIntentQuotesFragment: MoveIntentQuotesFragment,
    housingType: HousingType,
  ) {
    dataStore.edit { preferences ->
      val movingFlowState = MovingFlowState.fromFragments(moveIntentFragment, moveIntentQuotesFragment, housingType)
      preferences[movingFlowStateKey] = Json.encodeToString<MovingFlowState>(movingFlowState)
    }
  }

  companion object {
    private val movingFlowStateKey =
      stringPreferencesKey("com.hedvig.android.feature.movingflow.storage.movingFlowStateKey")
  }
}
