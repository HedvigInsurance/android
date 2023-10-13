package com.hedvig.android.hanalytics.featureflags.flags

internal interface FeatureFlagProvider {
  suspend fun isFeatureEnabled(feature: Feature): Boolean
}
