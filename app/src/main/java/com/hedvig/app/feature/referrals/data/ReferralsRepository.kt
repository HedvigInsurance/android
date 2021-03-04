package com.hedvig.app.feature.referrals.data

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.api.cache.http.HttpCachePolicy
import com.apollographql.apollo.coroutines.await
import com.apollographql.apollo.coroutines.toFlow
import com.apollographql.apollo.fetcher.ApolloResponseFetchers
import com.hedvig.android.owldroid.graphql.ReferralsQuery
import com.hedvig.android.owldroid.graphql.UpdateReferralCampaignCodeMutation

class ReferralsRepository(
    private val apolloClient: ApolloClient,
) {
    private val referralsQuery = ReferralsQuery()

    fun referrals() = apolloClient
        .query(referralsQuery)
        .watcher()
        .toFlow()

    suspend fun reloadReferrals() = apolloClient
        .query(referralsQuery)
        .toBuilder()
        .httpCachePolicy(HttpCachePolicy.NETWORK_ONLY)
        .responseFetcher(ApolloResponseFetchers.NETWORK_ONLY)
        .build()
        .await()

    suspend fun updateCode(newCode: String): Response<UpdateReferralCampaignCodeMutation.Data> {
        val response = apolloClient
            .mutate(UpdateReferralCampaignCodeMutation(newCode))
            .await()

        response.data?.updateReferralCampaignCode?.asSuccessfullyUpdatedCode?.code?.let { updatedCode ->
            val oldData = apolloClient
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


            apolloClient
                .apolloStore
                .writeAndPublish(referralsQuery, newData)
                .execute()
        }

        return response
    }
}
