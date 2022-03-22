package com.hedvig.app.util.featureflags

import com.hedvig.hanalytics.HAnalytics

class HAnalyticsFeatureFlagProvider(
    private val hAnalytics: HAnalytics,
) : FeatureFlagProvider {

    override val priority = HANALYTICS_PRIORITY

    override suspend fun isFeatureEnabled(feature: Feature) = when (feature) {
        Feature.MOVING_FLOW -> hAnalytics.movingFlow()
        Feature.FRANCE_MARKET -> hAnalytics.frenchMarket()
        Feature.REFERRAL_CAMPAIGN -> hAnalytics.foreverFebruaryCampaign()
        Feature.QUOTE_CART -> hAnalytics.useQuoteCart()
        Feature.KEY_GEAR -> hAnalytics.keyGear()
        Feature.EXTERNAL_DATA_COLLECTION -> hAnalytics.allowExternalDataCollection()
        Feature.CONNECT_PAYMENT_AT_SIGN -> false
    }

    override fun hasFeature(feature: Feature) = when (feature) {
        Feature.MOVING_FLOW -> true
        Feature.FRANCE_MARKET -> true
        Feature.REFERRAL_CAMPAIGN -> true
        Feature.QUOTE_CART -> true
        Feature.KEY_GEAR -> true
        Feature.EXTERNAL_DATA_COLLECTION -> true
        Feature.CONNECT_PAYMENT_AT_SIGN -> false
    }
}
