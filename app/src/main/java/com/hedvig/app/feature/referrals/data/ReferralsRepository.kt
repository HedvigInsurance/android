package com.hedvig.app.feature.referrals.data

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.cache.normalized.apolloStore
import com.hedvig.android.owldroid.graphql.ReferralsQuery
import com.hedvig.android.owldroid.graphql.UpdateReferralCampaignCodeMutation
import com.hedvig.app.util.apollo.QueryResult
import com.hedvig.app.util.apollo.safeWatch
import kotlinx.coroutines.flow.Flow

class ReferralsRepository(
  private val apolloClient: ApolloClient,
) {
  private val referralsQuery = ReferralsQuery()

  fun watchReferralsQueryData(): Flow<QueryResult<ReferralsQuery.Data>> = apolloClient
    .query(referralsQuery)
    .safeWatch()

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
            code = updatedCode,
          ),
        ),
      )

      apolloClient
        .apolloStore
        .writeOperation(referralsQuery, newData)
    }

    return response
  }
}
