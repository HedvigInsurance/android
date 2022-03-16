package com.hedvig.app.util.featureflags

import com.hedvig.app.feature.settings.Market
import com.hedvig.app.feature.settings.MarketManager

class ProductionFeatureFlagProvider(
    private val marketManager: MarketManager,
) : FeatureFlagProvider {

    override val priority = PRODUCTION_PRIORITY

    override fun isFeatureEnabled(feature: Feature) = when (feature) {
        Feature.FRANCE_MARKET -> false
        Feature.ADDRESS_AUTO_COMPLETE -> true
        Feature.CONNECT_PAYMENT_AT_SIGN -> marketManager.market == Market.NO || marketManager.market == Market.DK
        else -> false
    }

    override fun hasFeature(feature: Feature) = when (feature) {
        Feature.FRANCE_MARKET -> true
        Feature.ADDRESS_AUTO_COMPLETE -> true
        Feature.REFERRAL_CAMPAIGN -> false
        Feature.QUOTE_CART -> false
        Feature.CONNECT_PAYMENT_AT_SIGN -> true
    }
}
