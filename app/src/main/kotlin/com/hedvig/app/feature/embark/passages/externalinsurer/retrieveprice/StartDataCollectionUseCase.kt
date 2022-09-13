package com.hedvig.app.feature.embark.passages.externalinsurer.retrieveprice

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.graphql.InitiateDataCollectionNOMutation
import com.hedvig.android.apollo.graphql.InitiateDataCollectionSEMutation
import com.hedvig.android.market.Market
import com.hedvig.android.market.MarketManager
import com.hedvig.android.apollo.OperationResult
import com.hedvig.android.apollo.safeExecute
import java.util.UUID

class StartDataCollectionUseCase(
  val apolloClient: ApolloClient,
  val marketManager: MarketManager,
) {

  suspend fun startDataCollection(
    personalNumber: String,
    insuranceProvider: String,
  ): DataCollectionResult {
    val reference = UUID.randomUUID().toString()

    val mutation = createMutation(
      reference = reference,
      personalNumber = personalNumber,
      insuranceProvider = insuranceProvider,
    )

    return when (val result = apolloClient.mutation(mutation).safeExecute()) {
      is OperationResult.Success -> DataCollectionResult.Success(reference)
      is OperationResult.Error -> DataCollectionResult.Error.NetworkError(result.message)
    }
  }

  private fun createMutation(
    reference: String,
    personalNumber: String,
    insuranceProvider: String,
  ) = when (marketManager.market) {
    Market.NO -> InitiateDataCollectionNOMutation(
      reference = reference,
      insuranceProvider = insuranceProvider,
      personalNumber = personalNumber,
    )
    null,
    Market.SE,
    -> InitiateDataCollectionSEMutation(
      reference = reference,
      insuranceProvider = insuranceProvider,
      personalNumber = personalNumber,
    )
    Market.DK,
    Market.FR,
    -> throw IllegalArgumentException("Can not start data collection for ${marketManager.market}")
  }
}

sealed class DataCollectionResult {
  data class Success(val reference: String) : DataCollectionResult()

  sealed class Error : DataCollectionResult() {
    data class NetworkError(val message: String?) : Error()
    object QueryError : Error()
    object NoData : Error()
  }
}
