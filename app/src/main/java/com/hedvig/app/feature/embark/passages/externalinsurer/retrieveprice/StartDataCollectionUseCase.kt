package com.hedvig.app.feature.embark.passages.externalinsurer.retrieveprice

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.owldroid.graphql.InitiateDataCollectionNOMutation
import com.hedvig.android.owldroid.graphql.InitiateDataCollectionSEMutation
import com.hedvig.app.feature.settings.Market
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.util.apollo.QueryResult
import com.hedvig.app.util.apollo.safeQuery
import java.lang.IllegalArgumentException
import java.util.UUID

class StartDataCollectionUseCase(
    val apolloClient: ApolloClient,
    val marketManager: MarketManager
) {

    suspend fun startDataCollection(
        personalNumber: String,
        insuranceProvider: String,
    ): DataCollectionResult {
        val reference = UUID.randomUUID().toString()

        val mutation = createMutation(
            reference = reference,
            personalNumber = personalNumber,
            insuranceProvider = insuranceProvider
        )

        return when (val result = apolloClient.mutate(mutation).safeQuery()) {
            is QueryResult.Success -> DataCollectionResult.Success(reference)
            is QueryResult.Error -> DataCollectionResult.Error.NetworkError(result.message)
        }
    }

    private fun createMutation(
        reference: String,
        personalNumber: String,
        insuranceProvider: String
    ) = when (marketManager.market) {
        Market.NO -> InitiateDataCollectionNOMutation(
            reference = reference,
            insuranceProvider = insuranceProvider,
            personalNumber = personalNumber
        )
        null,
        Market.SE -> InitiateDataCollectionSEMutation(
            reference = reference,
            insuranceProvider = insuranceProvider,
            personalNumber = personalNumber
        )
        Market.DK,
        Market.FR -> throw IllegalArgumentException("Can not start data collection for ${marketManager.market}")
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
