package com.hedvig.app.util.featureflags

import com.hedvig.app.feature.settings.Market
import com.hedvig.app.feature.settings.MarketManager

class ProductionFeatureFlagProvider(
    private val marketManager: MarketManager
) : FeatureFlagProvider {

    override val priority = PRODUCTION_PRIORITY

    override suspend fun isFeatureEnabled(feature: Feature) = when (feature) {
        Feature.MOVING_FLOW -> marketManager.market == Market.SE || marketManager.market == Market.NO
        Feature.FRANCE_MARKET -> false
        else -> false
    }

    override fun hasFeature(feature: Feature) = when (feature) {
        Feature.MOVING_FLOW -> true
        Feature.FRANCE_MARKET -> true
        Feature.REFERRAL_CAMPAIGN -> false
        Feature.QUOTE_CART -> false
        Feature.KEY_GEAR -> false
        Feature.EXTERNAL_DATA_COLLECTION -> false
    }
}
