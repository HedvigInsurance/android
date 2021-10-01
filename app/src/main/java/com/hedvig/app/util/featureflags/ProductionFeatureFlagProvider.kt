package com.hedvig.app.util.featureflags

import com.hedvig.app.feature.settings.Market
import com.hedvig.app.feature.settings.MarketManager

class ProductionFeatureFlagProvider(
    private val marketManager: MarketManager
) : FeatureFlagProvider {

    override val priority = PRODUCTION_PRIORITY

    @Suppress("ComplexMethod")
    override fun isFeatureEnabled(feature: Feature): Boolean {
        return when (feature) {
            Feature.MOVING_FLOW -> marketManager.market == Market.SE
            Feature.INSURELY_EMBARK -> false
        }
    }

    override fun hasFeature(feature: Feature): Boolean = true
}
