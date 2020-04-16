package com.hedvig.app.feature.referrals

import com.apollographql.apollo.api.Response
import com.hedvig.android.owldroid.graphql.RedeemReferralCodeMutation
import com.hedvig.android.owldroid.graphql.ReferralCampaignMemberInformationQuery
import com.hedvig.app.ApolloClientWrapper
import com.hedvig.app.util.apollo.toDeferred

class ReferralRepository(private val apolloClientWrapper: ApolloClientWrapper) {
    suspend fun redeemReferralCode(code: String): Response<RedeemReferralCodeMutation.Data> {
        val redeemReferralCodeMutation = RedeemReferralCodeMutation(
            code = code
        )

        return apolloClientWrapper.apolloClient.mutate(redeemReferralCodeMutation).toDeferred()
            .await()
    }

    suspend fun fetchReferralCampaignMemberInformation(code: String): Response<ReferralCampaignMemberInformationQuery.Data> {
        val campaignMemberInformationQuery = ReferralCampaignMemberInformationQuery(
            code = code
        )

        return apolloClientWrapper.apolloClient.query(campaignMemberInformationQuery).toDeferred()
            .await()
    }
}
