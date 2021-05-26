package com.hedvig.app.feature.insurance.data

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.coroutines.await
import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.app.util.LocaleManager
import com.hedvig.app.util.apollo.QueryResult
import com.hedvig.app.util.apollo.safeQuery

class InsuranceRepository(
    private val apolloClient: ApolloClient,
    private val localeManager: LocaleManager
) {
    suspend fun insurance() = apolloClient
        .query(InsuranceQuery(localeManager.defaultLocale()))
        .await()

    suspend operator fun invoke(): InsuranceResult {
        return when (
            val response =
                apolloClient.query(InsuranceQuery(localeManager.defaultLocale())).safeQuery()
        ) {
            is QueryResult.Success -> InsuranceResult.Insurance(response.data)
            is QueryResult.Error -> InsuranceResult.Error(response.message)
        }
    }

    sealed class InsuranceResult {
        data class Insurance(val insurance: InsuranceQuery.Data) : InsuranceResult()
        data class Error(val message: String?) : InsuranceResult()
    }
}
