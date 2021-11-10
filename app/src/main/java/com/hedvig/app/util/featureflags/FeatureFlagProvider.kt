package com.hedvig.app.util.featureflags

import com.hedvig.app.feature.settings.Market

interface FeatureFlagProvider {
    val priority: Int
    fun isFeatureEnabled(feature: Feature, market: Market?): Boolean
    fun hasFeature(feature: Feature): Boolean
}

const val DEBUG_PRIORITY = 1
const val PRODUCTION_PRIORITY = 2
