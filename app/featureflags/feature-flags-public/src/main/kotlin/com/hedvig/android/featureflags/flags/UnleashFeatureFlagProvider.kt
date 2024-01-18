package com.hedvig.android.featureflags.flags

import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.UnleashClientProvider

internal class UnleashFeatureFlagProvider(
  private val unleashClientProvider: UnleashClientProvider,
) : FeatureManager {
  override suspend fun isFeatureEnabled(feature: Feature): Boolean = when (feature) {
    Feature.DISABLE_CHAT -> unleashClientProvider.provideUnleashClient().isEnabled("disable_chat", false)
    Feature.MOVING_FLOW -> unleashClientProvider.provideUnleashClient().isEnabled("moving_flow", false)
    Feature.PAYMENT_SCREEN -> unleashClientProvider.provideUnleashClient().isEnabled("payment_screen", false)
    Feature.TERMINATION_FLOW -> unleashClientProvider.provideUnleashClient().isEnabled("termination_flow", false)
    Feature.UPDATE_NECESSARY -> unleashClientProvider.provideUnleashClient().isEnabled("update_necessary", false)
    Feature.EDIT_COINSURED -> unleashClientProvider.provideUnleashClient().isEnabled("edit_coinsured", false)
    Feature.HELP_CENTER -> unleashClientProvider.provideUnleashClient().isEnabled("help_center", false)
  }
}
