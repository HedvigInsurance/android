package com.hedvig.android.feature.deleteaccount.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.hedvig.android.auth.MemberIdService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

internal class DeleteAccountRequestStorage(
  private val dataStore: DataStore<Preferences>,
  private val memberIdService: MemberIdService,
) {
  suspend fun storeTerminationRequest(memberId: String) {
    dataStore.edit { preferences ->
      val existingMemberIdsThatRequestedTermination =
        preferences[memberIdsThatRequestedTerminationPreferenceKey] ?: emptySet()
      preferences[memberIdsThatRequestedTerminationPreferenceKey] =
        existingMemberIdsThatRequestedTermination + memberId
    }
  }

  fun hasRequestedTermination(): Flow<Boolean> {
    return memberIdService.getMemberId().filterNotNull().flatMapLatest { memberId: String ->
      dataStore.data.map {
        it[memberIdsThatRequestedTerminationPreferenceKey]?.contains(memberId) ?: false
      }
    }
  }

  companion object {
    private val memberIdsThatRequestedTerminationPreferenceKey =
      stringSetPreferencesKey("com.hedvig.android.feature.deleteaccount.data.MemberIdsThatRequestedTermination")
  }
}
