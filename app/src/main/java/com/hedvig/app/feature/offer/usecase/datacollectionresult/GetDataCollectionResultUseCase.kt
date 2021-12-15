package com.hedvig.app.feature.offer.usecase.datacollectionresult

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.owldroid.graphql.DataCollectionResultQuery
import com.hedvig.app.util.apollo.QueryResult
import com.hedvig.app.util.apollo.safeQuery

class GetDataCollectionResultUseCase(
    private val apolloClient: ApolloClient,
) {
    sealed class Result {
        abstract val referenceUuid: String

        data class Success(override val referenceUuid: String, val data: DataCollectionResult) : Result()
        data class Error(override val referenceUuid: String) : Result()
    }

    suspend operator fun invoke(referenceUuid: String): Result {
        val result = apolloClient
            .query(DataCollectionResultQuery(referenceUuid))
            .safeQuery()
        return when (result) {
            is QueryResult.Error -> Result.Error(referenceUuid)
            is QueryResult.Success -> Result.Success(referenceUuid, DataCollectionResult.fromDto(result.data))
        }
    }
}
