package com.hedvig.android.data.claimflow

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class ClaimFlowContextStorage(
  private val datastore: DataStore<Preferences>,
) {
  suspend fun getContext(): String {
    return datastore.data
      .map { preferences: Preferences ->
        preferences[CLAIM_FLOW_CONTEXT_KEY]
      }
      .first()!!
  }

  suspend fun saveContext(context: String) {
    datastore.edit { preferences ->
      preferences[CLAIM_FLOW_CONTEXT_KEY] = context
    }
  }

  companion object {
    private val CLAIM_FLOW_CONTEXT_KEY = stringPreferencesKey("CLAIM_FLOW_CONTEXT_KEY")
  }
}
