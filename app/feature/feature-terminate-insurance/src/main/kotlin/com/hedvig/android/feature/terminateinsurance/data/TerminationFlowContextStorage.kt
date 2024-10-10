package com.hedvig.android.feature.terminateinsurance.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class TerminationFlowContextStorage(
  private val datastore: DataStore<Preferences>,
) {
  suspend fun getContext(): String {
    return datastore.data
      .map { preferences: Preferences ->
        preferences[TERMINATION_FLOW_CONTEXT_KEY]
      }
      .first()!!
  }

  suspend fun getContractId(): String {
    return datastore.data
      .map { preferences: Preferences ->
        preferences[TERMINATION_FLOW_CONTRACT_ID_KEY]
      }
      .first()!!
  }

  suspend fun saveContractId(contractId: String) {
    datastore.edit { preferences ->
      preferences[TERMINATION_FLOW_CONTRACT_ID_KEY] = contractId
    }
  }

  suspend fun saveContext(context: String) {
    datastore.edit { preferences ->
      preferences[TERMINATION_FLOW_CONTEXT_KEY] = context
    }
  }

  companion object {
    private val TERMINATION_FLOW_CONTEXT_KEY = stringPreferencesKey("CLAIM_FLOW_CONTEXT_KEY")
    private val TERMINATION_FLOW_CONTRACT_ID_KEY = stringPreferencesKey("TERMINATION_FLOW_CONTRACT_ID_KEY")
  }
}
