package com.hedvig.app.feature.referrals

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.rx2.Rx2Apollo
import com.hedvig.android.owldroid.graphql.RedeemReferralCodeMutation
import com.hedvig.android.owldroid.graphql.ReferralCampaignMemberInformationQuery
import com.hedvig.app.ApolloClientWrapper
import io.reactivex.Observable

class ReferralRepository(private val apolloClientWrapper: ApolloClientWrapper) {
    fun redeemReferralCode(code: String): Observable<Response<RedeemReferralCodeMutation.Data>> {
        val redeemReferralCodeMutation = RedeemReferralCodeMutation
            .builder()
            .code(code)
            .build()

        return Rx2Apollo
            .from(apolloClientWrapper.apolloClient.mutate(redeemReferralCodeMutation))
    }

    fun fetchReferralCampaignMemberInformation(code: String): Observable<Response<ReferralCampaignMemberInformationQuery.Data>> {
        val campaignMemberInformationQuery = ReferralCampaignMemberInformationQuery
            .builder()
            .code(code)
            .build()

        return Rx2Apollo
            .from(apolloClientWrapper.apolloClient.query(campaignMemberInformationQuery))
    }
}
