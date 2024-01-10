package com.hedvig.android.featureflags.flags

internal interface FeatureFlagProvider {
  suspend fun isFeatureEnabled(feature: Feature): Boolean
}
