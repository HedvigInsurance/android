package com.hedvig.app.feature.offer.usecase.datacollectionstatus

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.graphql.DataCollectionStatusSubscription
import com.hedvig.android.apollo.OperationResult
import com.hedvig.android.apollo.toSafeFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SubscribeToDataCollectionStatusUseCase(
  private val apolloClient: ApolloClient,
) {

  sealed class Status {
    abstract val referenceUuid: String

    data class Error(override val referenceUuid: String) : Status()
    data class Content(
      override val referenceUuid: String,
      val dataCollectionStatus: DataCollectionStatus,
    ) : Status()
  }

  operator fun invoke(referenceUuid: String): Flow<Status> {
    return apolloClient
      .subscription(DataCollectionStatusSubscription(referenceUuid))
      .toSafeFlow()
      .map { queryResult ->
        when (queryResult) {
          is OperationResult.Error -> Status.Error(referenceUuid)
          is OperationResult.Success -> Status.Content(
            referenceUuid,
            DataCollectionStatus.fromDto(queryResult.data),
          )
        }
      }
  }
}
