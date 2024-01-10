package com.hedvig.android.featureflags.flags

internal class DevFeatureFlagProvider : FeatureFlagProvider {
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
