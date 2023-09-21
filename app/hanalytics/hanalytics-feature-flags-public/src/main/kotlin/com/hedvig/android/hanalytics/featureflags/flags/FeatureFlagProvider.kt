package com.hedvig.android.hanalytics.featureflags.flags

interface FeatureFlagProvider {
  suspend fun isFeatureEnabled(feature: Feature): Boolean
}
