package com.hedvig.app.feature.referrals.data

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.cache.http.HttpFetchPolicy
import com.apollographql.apollo3.cache.http.httpFetchPolicy
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.apolloStore
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.apollographql.apollo3.coroutines.toFlow
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
        .httpFetchPolicy(HttpFetchPolicy.NetworkOnly)
        .fetchPolicy(FetchPolicy.NetworkOnly)
        .execute()

    suspend fun updateCode(newCode: String): ApolloResponse<UpdateReferralCampaignCodeMutation.Data> {
        val response = apolloClient
            .mutation(UpdateReferralCampaignCodeMutation(newCode))
            .execute()

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
