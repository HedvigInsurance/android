package com.hedvig.app.util.featureflags

class ProductionFeatureFlagProvider : FeatureFlagProvider {

    override val priority = PRODUCTION_PRIORITY

    override fun isFeatureEnabled(feature: Feature) = when (feature) {
        Feature.MOVING_FLOW -> true
        Feature.FRANCE_MARKET -> false
        Feature.ADDRESS_AUTO_COMPLETE -> true
        else -> false
    }

    override fun hasFeature(feature: Feature) = when (feature) {
        Feature.MOVING_FLOW -> true
        Feature.FRANCE_MARKET -> true
        Feature.ADDRESS_AUTO_COMPLETE -> true
        Feature.REFERRAL_CAMPAIGN -> false
        Feature.QUOTE_CART -> false
    }
}
