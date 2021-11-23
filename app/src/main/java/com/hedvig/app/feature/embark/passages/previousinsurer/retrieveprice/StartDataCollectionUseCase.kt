package com.hedvig.app.feature.embark.passages.previousinsurer.retrieveprice

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.owldroid.graphql.ExternalInsuranceProviderV2Query
import com.hedvig.android.owldroid.graphql.InitiateDataCollectionNOMutation
import com.hedvig.android.owldroid.graphql.InitiateDataCollectionSEMutation
import com.hedvig.app.feature.settings.Market
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.util.apollo.QueryResult
import com.hedvig.app.util.apollo.safeQuery
import java.lang.IllegalArgumentException
import java.util.UUID

interface StartDataCollectionUseCase {
    suspend fun startDataCollectionAndGetCollectionStatus(
        personalNumber: String,
        insuranceProvider: String,
    ): DataCollectionResult
}

class StartDataCollectionUseCaseImpl(
    val apolloClient: ApolloClient,
    val marketManager: MarketManager
) : StartDataCollectionUseCase {

    override suspend fun startDataCollectionAndGetCollectionStatus(
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
            is QueryResult.Success -> getCollectionStatus(reference)
            is QueryResult.Error -> DataCollectionResult.Error.NetworkError(result.message)
        }
    }

    private suspend fun getCollectionStatus(
        reference: String
    ): DataCollectionResult {
        val query = ExternalInsuranceProviderV2Query(reference)
        return when (val result = apolloClient.query(query).safeQuery()) {
            is QueryResult.Success -> {
                val extraInfo = result.data?.externalInsuranceProvider?.dataCollectionStatusV2?.extraInformation
                val swedishAutoStartToken = extraInfo?.asSwedishBankIdExtraInfo?.autoStartToken
                val norwegianBankIdWords = extraInfo?.asNorwegianBankIdExtraInfo?.norwegianBankIdWords
                return when {
                    swedishAutoStartToken != null -> DataCollectionResult.Success.SwedishBankId(swedishAutoStartToken)
                    norwegianBankIdWords != null -> DataCollectionResult.Success.NorwegianBankId(norwegianBankIdWords)
                    else -> DataCollectionResult.Error.NoData
                }
            }
            is QueryResult.Error -> DataCollectionResult.Error.QueryError
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
    sealed class Success : DataCollectionResult() {
        data class SwedishBankId(
            val autoStartToken: String
        ) : Success()

        data class NorwegianBankId(
            val norwegianBankIdWords: String
        ) : Success()
    }

    sealed class Error : DataCollectionResult() {
        data class NetworkError(val message: String?) : Error()
        object QueryError : Error()
        object NoData : Error()
    }
}
