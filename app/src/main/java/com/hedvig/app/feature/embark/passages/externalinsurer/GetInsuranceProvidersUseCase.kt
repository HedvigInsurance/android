package com.hedvig.app.feature.embark.passages.externalinsurer

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.owldroid.graphql.InsuranceProvidersQuery
import com.hedvig.app.isDebug
import com.hedvig.app.util.LocaleManager
import com.hedvig.app.util.apollo.QueryResult
import com.hedvig.app.util.apollo.safeQuery

sealed class InsuranceProvidersResult {
    data class Success(val providers: List<InsuranceProvider>) : InsuranceProvidersResult()
    sealed class Error : InsuranceProvidersResult() {
        object NetworkError : Error()
    }
}

data class InsuranceProvider(
    val id: String,
    val name: String,
    val collectionId: String? = null,
)

class GetInsuranceProvidersUseCase(
    private val apolloClient: ApolloClient,
    private val localeManager: LocaleManager
) {
    suspend fun getInsuranceProviders(): InsuranceProvidersResult {
        val insuranceProviders = InsuranceProvidersQuery(localeManager.defaultLocale())
        return when (val result = apolloClient.query(insuranceProviders).safeQuery()) {
            is QueryResult.Success -> createSuccessResult(result)
            is QueryResult.Error -> InsuranceProvidersResult.Error.NetworkError
        }
    }

    private fun createSuccessResult(
        result: QueryResult.Success<InsuranceProvidersQuery.Data>
    ): InsuranceProvidersResult.Success {
        var providers = result.data.insuranceProviders.map {
            InsuranceProvider(
                it.id,
                it.name,
                it.externalCollectionId
            )
        }
        if (isDebug()) {
            providers = providers + InsuranceProvider(
                "se-demo",
                "Demo",
                "se-demo"
            )
        }
        return InsuranceProvidersResult.Success(providers)
    }
}
