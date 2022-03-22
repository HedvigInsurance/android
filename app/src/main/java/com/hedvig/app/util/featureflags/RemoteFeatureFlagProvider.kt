package com.hedvig.app.util.featureflags

import com.hedvig.app.feature.settings.Market
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.service.RemoteConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RemoteFeatureFlagProvider(
    private val marketManager: MarketManager,
    private val remoteConfig: RemoteConfig,
) : FeatureFlagProvider {

    override val priority = REMOTE_PRIORITY

    private var seCampaignVisible: Boolean = false
    private var noCampaignVisible: Boolean = false
    private var dkCampaignVisible: Boolean = false

    init {
        CoroutineScope(Dispatchers.IO).launch {
            val remoteConfigData = remoteConfig.fetch()
            seCampaignVisible = remoteConfigData.seCampaignVisible
            noCampaignVisible = remoteConfigData.noCampaignVisible
            dkCampaignVisible = remoteConfigData.dkCampaignVisible
        }
    }

    override suspend fun isFeatureEnabled(feature: Feature) = when (feature) {
        Feature.MOVING_FLOW -> false
        Feature.FRANCE_MARKET -> false
        Feature.REFERRAL_CAMPAIGN -> {
            (marketManager.market == Market.SE && seCampaignVisible) ||
                (marketManager.market == Market.NO && noCampaignVisible) ||
                (marketManager.market == Market.DK && dkCampaignVisible)
        }
        Feature.QUOTE_CART -> false
        Feature.CONNECT_PAYMENT_AT_SIGN -> false
        else -> false
    }

    override fun hasFeature(feature: Feature) = when (feature) {
        Feature.MOVING_FLOW -> false
        Feature.FRANCE_MARKET -> false
        Feature.REFERRAL_CAMPAIGN -> true
        Feature.QUOTE_CART -> false
        Feature.CONNECT_PAYMENT_AT_SIGN -> false
        Feature.KEY_GEAR -> false
        Feature.EXTERNAL_DATA_COLLECTION -> false
    }
}
