package com.hedvig.android.feature.movingflow.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.flow.Flow

internal class MovingFlowStorage(
  private val dataStore: DataStore<Preferences>,
) {
  fun currentMovingFlowData(): Flow<MovingFlowData>

  fun initiateNewMovingFlow()
}
