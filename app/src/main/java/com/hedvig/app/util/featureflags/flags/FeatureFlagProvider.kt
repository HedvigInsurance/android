package com.hedvig.app.util.featureflags.flags

interface FeatureFlagProvider {
    suspend fun isFeatureEnabled(feature: Feature): Boolean
}
