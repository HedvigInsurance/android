package com.hedvig.app.feature.referrals.ui.tab

import com.hedvig.app.service.RemoteConfig

class CampaignUseCase(
    private val remoteConfig: RemoteConfig
) {
    suspend fun shouldShowCampaign(): Boolean {
        val data = remoteConfig.fetch()
        return data.campaignVisible
    }
}
