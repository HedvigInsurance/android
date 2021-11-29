package com.hedvig.app.feature.offer.usecase

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.owldroid.graphql.DataCollectionResultQuery
import com.hedvig.app.util.apollo.QueryResult
import com.hedvig.app.util.apollo.safeQuery

class ExternalInsuranceDataCollectionUseCase(
    private val apolloClient: ApolloClient,
) {
    sealed class Result {
        data class Success(val data: DataCollectionResultQuery.Data) : Result()
        object Error : Result()
    }

    suspend operator fun invoke(referenceUUID: String): Result {
        val result = apolloClient
            .query(DataCollectionResultQuery(referenceUUID))
            .safeQuery()
        return when (result) {
            is QueryResult.Error -> Result.Error
            is QueryResult.Success -> Result.Success(result.data)
        }
    }
}
