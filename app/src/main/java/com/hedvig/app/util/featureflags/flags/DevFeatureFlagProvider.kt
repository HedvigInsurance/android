package com.hedvig.app.util.featureflags.flags

import com.hedvig.app.feature.settings.Market
import com.hedvig.app.feature.settings.MarketManager

class DevFeatureFlagProvider(
    private val marketManager: MarketManager,
) : FeatureFlagProvider {

    override suspend fun isFeatureEnabled(feature: Feature) = when (feature) {
        Feature.CONNECT_PAYMENT_AT_SIGN -> marketManager.market == Market.NO || marketManager.market == Market.DK
        Feature.EXTERNAL_DATA_COLLECTION -> marketManager.market == Market.SE
        Feature.FRANCE_MARKET -> true
        Feature.KEY_GEAR -> false
        Feature.MOVING_FLOW -> true
        Feature.QUOTE_CART -> false
        Feature.REFERRAL_CAMPAIGN -> false
        Feature.UPDATE_NECESSARY -> false
    }
}
