package com.hedvig.android.feature.onboarding.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.hedvig.android.core.common.di.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Remembers, per member and per install, that onboarding was shown. Set when the member completes
 * the flow or dismisses it with the close button; once set, onboarding never appears again for
 * that member on this device.
 */
internal interface OnboardingSeenStore {
  suspend fun hasSeenOnboarding(memberId: String): Boolean

  suspend fun markOnboardingSeen(memberId: String)
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class DataStoreOnboardingSeenStore(
  private val dataStore: DataStore<Preferences>,
) : OnboardingSeenStore {
  override suspend fun hasSeenOnboarding(memberId: String): Boolean {
    return dataStore.data.map { it[seenKey(memberId)] ?: false }.first()
  }

  override suspend fun markOnboardingSeen(memberId: String) {
    dataStore.edit { it[seenKey(memberId)] = true }
  }

  private fun seenKey(memberId: String): Preferences.Key<Boolean> {
    return booleanPreferencesKey("com.hedvig.android.feature.onboarding.seen.$memberId")
  }
}
