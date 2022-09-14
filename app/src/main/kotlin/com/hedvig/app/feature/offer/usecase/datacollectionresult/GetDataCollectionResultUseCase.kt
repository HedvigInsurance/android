package com.hedvig.app.feature.offer.usecase.datacollectionresult

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.OperationResult
import com.hedvig.android.apollo.graphql.DataCollectionResultQuery
import com.hedvig.android.apollo.safeExecute

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
      .safeExecute()
    return when (result) {
      is OperationResult.Error -> Result.Error(referenceUuid)
      is OperationResult.Success -> Result.Success(referenceUuid, DataCollectionResult.fromDto(result.data))
    }
  }
}
