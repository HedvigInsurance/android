package com.hedvig.android.featureflags.flags

import io.getunleash.UnleashClient

internal class UnleashFeatureFlagProvider(
  private val unleashClient: UnleashClient,
) : FeatureFlagProvider {
  override suspend fun isFeatureEnabled(feature: Feature): Boolean = when (feature) {
    Feature.DISABLE_CHAT -> unleashClient.isEnabled("disable_chat", false)
    Feature.MOVING_FLOW -> unleashClient.isEnabled("moving_flow", false)
    Feature.PAYMENT_SCREEN -> unleashClient.isEnabled("payment_screen", false)
    Feature.TERMINATION_FLOW -> unleashClient.isEnabled("termination_flow", false)
    Feature.UPDATE_NECESSARY -> unleashClient.isEnabled("update_necessary", false)
    Feature.EDIT_COINSURED -> unleashClient.isEnabled("edit_coinsured", false)
    Feature.HELP_CENTER -> unleashClient.isEnabled("help_center", false)
  }
}
