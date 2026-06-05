package com.hedvig.android.featureflags.flags

import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.HedvigUnleashClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

internal class UnleashFeatureFlagProvider(
  private val hedvigUnleashClient: HedvigUnleashClient,
) : FeatureManager {
  override fun isFeatureEnabled(feature: Feature): Flow<Boolean> {
    return hedvigUnleashClient.featureUpdatedFlow
      .map {
        val key = feature.unleashKey
        when (feature) {
          // Kill switches: the remote toggle being on means the feature is off.
          Feature.TERMINATION_FLOW,
          Feature.PUPPY_GUIDE,
          -> !hedvigUnleashClient.client.isEnabled(key)

          Feature.ALWAYS_AVAILABLE_INBOX_AND_NEW_CHAT,
          Feature.UPDATE_NECESSARY,
          Feature.TRAVEL_ADDON,
          Feature.ENABLE_VIDEO_PLAYER_IN_CHAT_MESSAGES,
          Feature.ENABLE_CLAIM_HISTORY,
          -> hedvigUnleashClient.client.isEnabled(key)
        }
      }.distinctUntilChanged()
  }
}
