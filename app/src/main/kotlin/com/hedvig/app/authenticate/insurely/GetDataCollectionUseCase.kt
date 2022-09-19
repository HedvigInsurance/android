package com.hedvig.app.authenticate.insurely

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.OperationResult
import com.hedvig.android.apollo.graphql.ExternalInsuranceProviderV2Subscription
import com.hedvig.android.apollo.graphql.type.DataCollectionStatus
import com.hedvig.android.apollo.toSafeFlow
import com.hedvig.android.market.MarketManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetDataCollectionUseCase(
  val apolloClient: ApolloClient,
  val marketManager: MarketManager,
) {

  fun getCollectionStatus(
    reference: String,
  ): Flow<DataCollectionResult> {
    val subscription = ExternalInsuranceProviderV2Subscription(reference)
    return apolloClient.subscription(subscription).toSafeFlow().map { result ->
      when (result) {
        is OperationResult.Success -> {
          val extraInfo = result.data?.dataCollectionStatusV2?.extraInformation
          val swedishAutoStartToken = extraInfo?.asSwedishBankIdExtraInfo?.autoStartToken
          val norwegianBankIdWords = extraInfo?.asNorwegianBankIdExtraInfo?.norwegianBankIdWords
          val collectionStatus = result.data.dataCollectionStatusV2.status.toCollectionStatus()
          when {
            swedishAutoStartToken != null -> DataCollectionResult.Success.SwedishBankId(
              swedishAutoStartToken,
              collectionStatus,
            )
            norwegianBankIdWords != null -> DataCollectionResult.Success.NorwegianBankId(
              norwegianBankIdWords,
              collectionStatus,
            )
            else -> DataCollectionResult.Success.SwedishBankId(
              null,
              collectionStatus,
            )
          }
        }
        is OperationResult.Error -> DataCollectionResult.Error.QueryError
      }
    }
  }

  private fun DataCollectionStatus.toCollectionStatus() = when (this) {
    DataCollectionStatus.RUNNING -> DataCollectionResult.Success.CollectionStatus.NONE
    DataCollectionStatus.LOGIN -> DataCollectionResult.Success.CollectionStatus.LOGIN
    DataCollectionStatus.COLLECTING -> DataCollectionResult.Success.CollectionStatus.COLLECTING
    DataCollectionStatus.COMPLETED,
    DataCollectionStatus.COMPLETED_PARTIAL,
    -> DataCollectionResult.Success.CollectionStatus.COMPLETED
    DataCollectionStatus.COMPLETED_EMPTY,
    DataCollectionStatus.WAITING_FOR_AUTHENTICATION,
    DataCollectionStatus.FAILED,
    -> DataCollectionResult.Success.CollectionStatus.FAILED
    DataCollectionStatus.USER_INPUT,
    DataCollectionStatus.UNKNOWN__,
    -> DataCollectionResult.Success.CollectionStatus.UNKNOWN
  }
}

sealed class DataCollectionResult {
  sealed class Success : DataCollectionResult() {
    data class SwedishBankId(
      val autoStartToken: String?,
      val status: CollectionStatus,
    ) : Success()

    data class NorwegianBankId(
      val norwegianBankIdWords: String,
      val status: CollectionStatus,
    ) : Success()

    enum class CollectionStatus {
      LOGIN, COLLECTING, COMPLETED, FAILED, NONE, UNKNOWN
    }
  }

  sealed class Error : DataCollectionResult() {
    data class NetworkError(val message: String?) : Error()
    object QueryError : Error()
  }
}
