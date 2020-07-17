package com.hedvig.app.feature.referrals.data

import com.apollographql.apollo.api.Response
import com.apollographql.apollo.api.cache.http.HttpCachePolicy
import com.apollographql.apollo.coroutines.toDeferred
import com.apollographql.apollo.coroutines.toFlow
import com.apollographql.apollo.fetcher.ApolloResponseFetchers
import com.hedvig.android.owldroid.graphql.ReferralsQuery
import com.hedvig.android.owldroid.graphql.UpdateReferralCampaignCodeMutation
import com.hedvig.app.ApolloClientWrapper

class ReferralsRepository(
    private val apolloClientWrapper: ApolloClientWrapper
) {
    private val referralsQuery = ReferralsQuery()

    fun referrals() = apolloClientWrapper
        .apolloClient
        .query(referralsQuery)
        .watcher()
        .toFlow()

    fun reloadReferralsAsync() = apolloClientWrapper
        .apolloClient
        .query(referralsQuery)
        .httpCachePolicy(HttpCachePolicy.NETWORK_ONLY)
        .responseFetcher(ApolloResponseFetchers.NETWORK_ONLY)
        .toDeferred()

    suspend fun updateCode(newCode: String): Response<UpdateReferralCampaignCodeMutation.Data> {
        val response = apolloClientWrapper
            .apolloClient
            .mutate(UpdateReferralCampaignCodeMutation(newCode))
            .toDeferred()
            .await()

        response.data?.updateReferralCampaignCode?.asSuccessfullyUpdatedCode?.code?.let { updatedCode ->
            val oldData = apolloClientWrapper
                .apolloClient
                .apolloStore
                .read(referralsQuery)
                .execute()

            val newData = oldData.copy(
                referralInformation = oldData.referralInformation.copy(
                    campaign = oldData.referralInformation.campaign.copy(
                        code = updatedCode
                    )
                )
            )

            apolloClientWrapper
                .apolloClient
                .apolloStore
                .writeAndPublish(referralsQuery, newData)
                .execute()
        }

        return response
    }
}
