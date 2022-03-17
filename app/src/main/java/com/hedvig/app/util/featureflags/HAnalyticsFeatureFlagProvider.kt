package com.hedvig.app.util.featureflags

import com.hedvig.app.feature.tracking.HAnalyticsFacade

class HAnalyticsFeatureFlagProvider(
    private val hAnalyticsFacade: HAnalyticsFacade,
) : FeatureFlagProvider {

    override val priority = HANALYTICS_PRIORITY

    override suspend fun isFeatureEnabled(feature: Feature) = when (feature) {
        Feature.MOVING_FLOW -> hAnalyticsFacade.movingFlow()
        Feature.FRANCE_MARKET -> hAnalyticsFacade.frenchMarket()
        Feature.ADDRESS_AUTO_COMPLETE -> false
        Feature.REFERRAL_CAMPAIGN -> hAnalyticsFacade.foreverFebruaryCampaign()
        Feature.QUOTE_CART -> hAnalyticsFacade.useQuoteCart()
        Feature.HEDVIG_TYPE_FACE -> hAnalyticsFacade.useHedvigLettersFont()
        Feature.KEY_GEAR -> hAnalyticsFacade.keyGear()
        Feature.EXTERNAL_DATA_COLLECTION -> hAnalyticsFacade.allowExternalDataCollection()
    }

    override fun hasFeature(feature: Feature) = when (feature) {
        Feature.MOVING_FLOW -> true
        Feature.FRANCE_MARKET -> true
        Feature.ADDRESS_AUTO_COMPLETE -> false
        Feature.REFERRAL_CAMPAIGN -> true
        Feature.QUOTE_CART -> true
        Feature.HEDVIG_TYPE_FACE -> true
        Feature.KEY_GEAR -> true
        Feature.EXTERNAL_DATA_COLLECTION -> true
    }
}
