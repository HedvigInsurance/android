package com.hedvig.app.util.featureflags

import com.hedvig.app.feature.settings.Market

class DebugFeatureFlagProvider : FeatureFlagProvider {

    override val priority = DEBUG_PRIORITY

    override fun isFeatureEnabled(feature: Feature, market: Market?): Boolean {
        return when (feature) {
            Feature.MOVING_FLOW -> market == Market.SE
            Feature.INSURELY_EMBARK -> true
            Feature.EMBARK_CLAIMS -> true
            Feature.CLAIMS_STATUS -> true
            Feature.FRANCE_MARKET -> true
        }
    }

    override fun hasFeature(feature: Feature): Boolean = true
}
