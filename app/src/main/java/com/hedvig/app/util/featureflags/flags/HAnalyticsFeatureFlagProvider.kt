package com.hedvig.app.util.featureflags.flags

import com.hedvig.hanalytics.HAnalytics

class HAnalyticsFeatureFlagProvider(
    private val hAnalytics: HAnalytics,
) : FeatureFlagProvider {
    // todo remember to reflect these options inside unleashed for the Android client
    //  Feature.MOVING_FLOW -> marketManager.market == Market.SE || marketManager.market == Market.NO
    //  Feature.CONNECT_PAYMENT_AT_SIGN -> marketManager.market == Market.NO || marketManager.market == Market.DK
    override suspend fun isFeatureEnabled(feature: Feature) = when (feature) {
        Feature.EXTERNAL_DATA_COLLECTION -> hAnalytics.allowExternalDataCollection()
        Feature.FRANCE_MARKET -> hAnalytics.frenchMarket()
        Feature.KEY_GEAR -> hAnalytics.keyGear()
        Feature.MOVING_FLOW -> hAnalytics.movingFlow()
        Feature.CONNECT_PAYMENT_AT_SIGN -> hAnalytics.postOnboardingShowPaymentStep()
        Feature.UPDATE_NECESSARY -> hAnalytics.updateNecessary()
        Feature.REFERRAL_CAMPAIGN -> hAnalytics.foreverFebruaryCampaign()
        Feature.QUOTE_CART -> hAnalytics.useQuoteCart()
    }
}
