package com.hedvig.app.feature.referrals

import com.apollographql.apollo.api.Response
import com.hedvig.android.owldroid.graphql.RedeemReferralCodeMutation
import com.hedvig.android.owldroid.graphql.ReferralCampaignMemberInformationQuery
import com.hedvig.app.ApolloClientWrapper
import com.hedvig.app.util.apollo.toDeferred
import kotlinx.coroutines.Deferred

class ReferralRepository(private val apolloClientWrapper: ApolloClientWrapper) {
    fun redeemReferralCodeAsync(code: String): Deferred<Response<RedeemReferralCodeMutation.Data>> {
        val redeemReferralCodeMutation = RedeemReferralCodeMutation(
            code = code
        )

        return apolloClientWrapper.apolloClient.mutate(redeemReferralCodeMutation).toDeferred()
    }

    fun fetchReferralCampaignMemberInformationAsync(code: String): Deferred<Response<ReferralCampaignMemberInformationQuery.Data>> {
        val campaignMemberInformationQuery = ReferralCampaignMemberInformationQuery(
            code = code
        )

        return apolloClientWrapper.apolloClient.query(campaignMemberInformationQuery).toDeferred()
    }
}
