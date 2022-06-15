package com.hedvig.app.feature.referrals.data

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.cache.http.HttpFetchPolicy
import com.apollographql.apollo3.cache.http.httpFetchPolicy
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.apolloStore
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.apollographql.apollo3.cache.normalized.watch
import com.hedvig.android.owldroid.graphql.ReferralsQuery
import com.hedvig.android.owldroid.graphql.UpdateReferralCampaignCodeMutation
import kotlinx.coroutines.flow.Flow

class ReferralsRepository(
    private val apolloClient: ApolloClient,
) {
    private val referralsQuery = ReferralsQuery()

    fun referrals(): Flow<ApolloResponse<ReferralsQuery.Data>> = apolloClient
        .query(referralsQuery)
        .watch()

    suspend fun reloadReferrals(): ApolloResponse<ReferralsQuery.Data> = apolloClient
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
                .readOperation(referralsQuery)

            val newData = oldData.copy(
                referralInformation = oldData.referralInformation.copy(
                    campaign = oldData.referralInformation.campaign.copy(
                        code = updatedCode
                    )
                )
            )

            apolloClient
                .apolloStore
                .writeOperation(referralsQuery, newData)
        }

        return response
    }
}
