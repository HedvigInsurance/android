package com.hedvig.android.hanalytics.featureflags.flags

import com.hedvig.android.market.MarketManager

internal class DevFeatureFlagProvider(
  private val marketManager: MarketManager,
) : FeatureFlagProvider {
  override suspend fun isFeatureEnabled(feature: Feature): Boolean {
    return when (feature) {
      Feature.DISABLE_CHAT -> false
      Feature.MOVING_FLOW -> true
      Feature.TERMINATION_FLOW -> true
      Feature.UPDATE_NECESSARY -> false
      Feature.EDIT_COINSURED -> true
      Feature.HELP_CENTER -> true
      Feature.PAYMENT_SCREEN -> true
    }
  }
}
