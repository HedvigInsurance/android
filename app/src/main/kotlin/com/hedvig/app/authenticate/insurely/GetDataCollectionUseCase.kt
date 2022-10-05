package com.hedvig.app.authenticate.insurely

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.market.MarketManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class GetDataCollectionUseCase(
  val apolloClient: ApolloClient,
  val marketManager: MarketManager,
) {

  // Implementation removed since schema changed, and this functionality is not
  // used in the apps. Keeping the UseCase if we want to implement later.
  fun getCollectionStatus(reference: String): Flow<DataCollectionResult> {
    return flowOf(DataCollectionResult.Error.QueryError)
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
