package com.hedvig.app.feature.claimstatus.usecase

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.owldroid.graphql.ClaimStatusDetailsQuery
import com.hedvig.app.util.apollo.QueryResult
import com.hedvig.app.util.apollo.safeQuery
import e

class GetClaimStatusDetailsUseCase(
    private val apolloClient: ApolloClient,
) {
    suspend operator fun invoke(
        claimId: String,
    ): ClaimStatusDetailsQuery.ClaimStatusDetail? {
        val result = apolloClient
            .query(ClaimStatusDetailsQuery())
            .safeQuery()
        return when (result) {
            is QueryResult.Success -> {
                // TODO look into filtering on the query itself if possible instead of fetching everything for no reason
                result.data.claimStatusDetails.first { it.id == claimId }
            }
            is QueryResult.Error -> {
                e { "Error when loading claim status details: ${result.message}" }
                null
            }
        }
    }
}
