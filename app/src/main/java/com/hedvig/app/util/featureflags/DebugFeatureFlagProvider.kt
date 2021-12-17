package com.hedvig.app.util.featureflags

import com.hedvig.app.feature.settings.Market
import com.hedvig.app.feature.settings.MarketManager

class DebugFeatureFlagProvider(
    private val marketManager: MarketManager
) : FeatureFlagProvider {

    override val priority = DEBUG_PRIORITY

    override fun isFeatureEnabled(feature: Feature): Boolean {
        return when (feature) {
            Feature.MOVING_FLOW -> marketManager.market == Market.SE || marketManager.market == Market.NO
            Feature.INSURELY_EMBARK -> true
            Feature.EMBARK_CLAIMS -> true
            Feature.CLAIMS_STATUS -> true
            Feature.FRANCE_MARKET -> true
            Feature.SE_EMBARK_ONBOARDING -> true
            Feature.CONNECT_PAYMENT_AT_SIGN -> true
        }
    }

    override fun hasFeature(feature: Feature): Boolean = true
}
