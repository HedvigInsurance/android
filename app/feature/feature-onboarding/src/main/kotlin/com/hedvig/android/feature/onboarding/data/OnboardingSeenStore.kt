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

  /** Debug affordance only: clears the flag so onboarding can be triggered again. */
  suspend fun resetOnboardingSeen(memberId: String)
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

  override suspend fun resetOnboardingSeen(memberId: String) {
    dataStore.edit { it.remove(seenKey(memberId)) }
  }

  private fun seenKey(memberId: String): Preferences.Key<Boolean> {
    return booleanPreferencesKey("com.hedvig.android.feature.onboarding.seen.$memberId")
  }
}
