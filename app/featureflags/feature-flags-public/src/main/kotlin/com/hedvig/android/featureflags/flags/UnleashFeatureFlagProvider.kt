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
        when (feature) {
          Feature.DISABLE_CHAT -> hedvigUnleashClient.client.isEnabled("disable_chat", false)
          Feature.MOVING_FLOW -> hedvigUnleashClient.client.isEnabled("moving_flow", false)
          Feature.PAYMENT_SCREEN -> hedvigUnleashClient.client.isEnabled("payment_screen", false)
          Feature.TERMINATION_FLOW -> hedvigUnleashClient.client.isEnabled("termination_flow", true)
          Feature.UPDATE_NECESSARY -> hedvigUnleashClient.client.isEnabled("update_necessary", false)
          Feature.EDIT_COINSURED -> hedvigUnleashClient.client.isEnabled("edit_coinsured", false)
          Feature.HELP_CENTER -> hedvigUnleashClient.client.isEnabled("help_center", true)
          Feature.TRAVEL_ADDON -> hedvigUnleashClient.client.isEnabled("enable_addons", false)
          Feature.ENABLE_VIDEO_PLAYER_IN_CHAT_MESSAGES -> hedvigUnleashClient.client.isEnabled(
            "enable_video_player_in_chat_messages",
            false,
          )
          Feature.DISABLE_REDEEM_CAMPAIGN -> hedvigUnleashClient.client.isEnabled("disable_redeem_campaign", false)
          Feature.ENABLE_CLAIM_HISTORY -> hedvigUnleashClient.client.isEnabled("enable_claim_history", false)
        }
      }.distinctUntilChanged()
  }
}
