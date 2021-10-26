package com.hedvig.app.feature.insurance.data

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.app.util.LocaleManager
import com.hedvig.app.util.apollo.QueryResult
import com.hedvig.app.util.apollo.safeQuery

class GetContractsUseCase(
    private val apolloClient: ApolloClient,
    private val localeManager: LocaleManager
) {
    suspend operator fun invoke(): InsuranceResult {
        return when (val response = apolloClient.query(InsuranceQuery(localeManager.defaultLocale())).safeQuery()) {
            is QueryResult.Error -> InsuranceResult.Error(response.message)
            is QueryResult.Success -> InsuranceResult.Insurance(response.data)
        }
    }

    sealed class InsuranceResult {
        data class Insurance(val insurance: InsuranceQuery.Data) : InsuranceResult()
        data class Error(val message: String?) : InsuranceResult()
    }
}
