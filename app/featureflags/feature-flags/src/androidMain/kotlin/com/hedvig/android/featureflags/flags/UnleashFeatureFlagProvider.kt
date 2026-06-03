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

          Feature.MOVING_FLOW -> hedvigUnleashClient.client.isEnabled("moving_flow")

          Feature.PAYMENT_SCREEN -> hedvigUnleashClient.client.isEnabled("payment_screen")

          Feature.TERMINATION_FLOW -> !hedvigUnleashClient.client.isEnabled("disable_termination_flow")

          Feature.UPDATE_NECESSARY -> hedvigUnleashClient.client.isEnabled("update_necessary")

          Feature.EDIT_COINSURED -> hedvigUnleashClient.client.isEnabled("edit_coinsured")

          Feature.HELP_CENTER -> !hedvigUnleashClient.client.isEnabled("disable_help_center")

          Feature.TRAVEL_ADDON -> hedvigUnleashClient.client.isEnabled("enable_addons")

          Feature.ENABLE_VIDEO_PLAYER_IN_CHAT_MESSAGES -> hedvigUnleashClient.client.isEnabled(
            "enable_video_player_in_chat_messages",
          )

          Feature.DISABLE_REDEEM_CAMPAIGN -> hedvigUnleashClient.client.isEnabled("disable_redeem_campaign")

          Feature.ENABLE_CLAIM_HISTORY -> hedvigUnleashClient.client.isEnabled("enable_claim_history")
        }
      }.distinctUntilChanged()
  }
}
