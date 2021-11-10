package com.hedvig.app.util.featureflags

import com.hedvig.app.feature.settings.Market

class ProductionFeatureFlagProvider : FeatureFlagProvider {

    override val priority = PRODUCTION_PRIORITY

    @Suppress("ComplexMethod")
    override fun isFeatureEnabled(feature: Feature, market: Market?): Boolean {
        return when (feature) {
            Feature.MOVING_FLOW -> market == Market.SE
            Feature.INSURELY_EMBARK -> false
            Feature.EMBARK_CLAIMS -> true
            Feature.CLAIMS_STATUS -> true
            Feature.FRANCE_MARKET -> false
        }
    }

    override fun hasFeature(feature: Feature): Boolean = true
}
