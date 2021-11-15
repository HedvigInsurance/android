package com.hedvig.app.util.featureflags

interface FeatureFlagProvider {
    val priority: Int
    fun isFeatureEnabled(feature: Feature): Boolean
    fun hasFeature(feature: Feature): Boolean
}

const val DEBUG_PRIORITY = 1
const val PRODUCTION_PRIORITY = 2
