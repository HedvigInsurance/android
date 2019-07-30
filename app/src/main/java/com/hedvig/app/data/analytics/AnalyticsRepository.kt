package com.hedvig.app.data.analytics

import com.apollographql.apollo.rx2.Rx2Apollo
import com.hedvig.android.owldroid.graphql.RegiserBranchCampaignMutation
import com.hedvig.android.owldroid.type.CampaignInput
import com.hedvig.app.ApolloClientWrapper

class AnalyticsRepository(private val apolloClientWrapper: ApolloClientWrapper) {

    fun registerBranchCampaign(campaignInput: CampaignInput) =
        Rx2Apollo.from(apolloClientWrapper.apolloClient.mutate(RegiserBranchCampaignMutation(campaignInput)))
}
