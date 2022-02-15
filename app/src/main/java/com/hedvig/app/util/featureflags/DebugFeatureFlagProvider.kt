package com.hedvig.app.util.featureflags

import com.hedvig.app.feature.settings.Market
import com.hedvig.app.feature.settings.MarketManager

class DebugFeatureFlagProvider(
    private val marketManager: MarketManager
) : FeatureFlagProvider {

    override val priority = DEBUG_PRIORITY

    override fun isFeatureEnabled(feature: Feature) = when (feature) {
        Feature.MOVING_FLOW -> marketManager.market == Market.SE || marketManager.market == Market.NO
        Feature.FRANCE_MARKET -> true
        Feature.ADDRESS_AUTO_COMPLETE -> true
        Feature.REFERRAL_CAMPAIGN -> true
        Feature.QUOTE_CART -> false
    }

    override fun hasFeature(feature: Feature) = true
}
