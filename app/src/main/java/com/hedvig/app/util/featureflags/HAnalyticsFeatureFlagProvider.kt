package com.hedvig.app.util.featureflags

import com.hedvig.app.feature.tracking.HAnalyticsFacade

class HAnalyticsFeatureFlagProvider(
    private val hAnalyticsFacade: HAnalyticsFacade,
) : FeatureFlagProvider {

    override val priority = HANALYTICS_PRIORITY

    // TODO: remove suspend from protected abstract suspend fun getExperiment(name: String): HAnalyticsExperiment
    override suspend fun isFeatureEnabled(feature: Feature) = when (feature) {
        Feature.MOVING_FLOW -> hAnalyticsFacade.movingFlow()
        Feature.FRANCE_MARKET -> hAnalyticsFacade.frenchMarket()
        Feature.ADDRESS_AUTO_COMPLETE -> false
        Feature.REFERRAL_CAMPAIGN -> hAnalyticsFacade.foreverFebruaryCampaign()
        Feature.QUOTE_CART -> false
    }

    override fun hasFeature(feature: Feature): Boolean {
        TODO("Not yet implemented")
    }
}
