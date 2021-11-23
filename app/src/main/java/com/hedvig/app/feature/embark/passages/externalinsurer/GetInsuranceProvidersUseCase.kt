package com.hedvig.app.feature.embark.passages.externalinsurer

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.owldroid.graphql.InsuranceProvidersQuery
import com.hedvig.app.util.LocaleManager
import com.hedvig.app.util.apollo.QueryResult
import com.hedvig.app.util.apollo.safeQuery

interface GetInsuranceProvidersUseCase {
    suspend fun getInsuranceProviders(): InsuranceProvidersResult
}

sealed class InsuranceProvidersResult {
    data class Success(val providers: List<InsuranceProvider>) : InsuranceProvidersResult()
    sealed class Error : InsuranceProvidersResult() {
        object NetworkError : Error()
    }
}

data class InsuranceProvider(
    val id: String,
    val name: String
)

class GetInsuranceProvidersUseCaseImpl(
    private val apolloClient: ApolloClient,
    private val localeManager: LocaleManager
) : GetInsuranceProvidersUseCase {
    override suspend fun getInsuranceProviders(): InsuranceProvidersResult {
        val insuranceProviders = InsuranceProvidersQuery(localeManager.defaultLocale())
        return when (val result = apolloClient.query(insuranceProviders).safeQuery()) {
            is QueryResult.Success -> InsuranceProvidersResult.Success(
                result.data.insuranceProviders.map {
                    InsuranceProvider(
                        it.id,
                        it.name
                    )
                }
            )
            is QueryResult.Error -> InsuranceProvidersResult.Error.NetworkError
        }
    }
}
